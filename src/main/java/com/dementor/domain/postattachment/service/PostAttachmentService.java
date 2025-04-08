package com.dementor.domain.postattachment.service;

import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.postattachment.dto.response.FileResponse.FileInfoDto;
import com.dementor.domain.postattachment.entity.PostAttachment;
import com.dementor.domain.postattachment.entity.PostAttachment.ImageType;
import com.dementor.domain.postattachment.exception.PostAttachmentErrorCode;
import com.dementor.domain.postattachment.exception.PostAttachmentException;
import com.dementor.domain.postattachment.repository.PostAttachmentRepository;
import com.dementor.firebase.service.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostAttachmentService {

    private final PostAttachmentRepository postAttachmentRepository;
    private final MemberRepository memberRepository;
    private final MentorRepository mentorRepository;
    private final FirebaseStorageService firebaseStorageService;

    @Value("${file.max-size}")
    private long maxFileSize; // 바이트 단위 (5MB = 5 * 1024 * 1024)

    @Value("${file.max-per-user}")
    private int maxFilesPerUser; // 사용자당 최대 파일 수

    //요청한 사용자가 파일의 소유자인지 확인
    public boolean isFileOwner(Long fileId, Long userId) {
        if (fileId == null || userId == null) {
            return false; // null 값이 있으면 소유자가 아님
        }
        return postAttachmentRepository.findById(fileId)
                .map(file -> file.getMember().getId().equals(userId))
                .orElse(false);
    }

    // 요청한 사용자가 마크다운 이미지의 소유자인지 확인
    public boolean isMarkdownImageOwner(String uniqueIdentifier, Long userId) {
        return postAttachmentRepository.findByUniqueIdentifier(uniqueIdentifier)
                .map(file -> file.getMember().getId().equals(userId))
                .orElse(false);
    }

    //파일 업로드 처리
    @Transactional
    public List<FileInfoDto> uploadFiles(List<MultipartFile> files, ImageType imageType, Long memberId, String markdownText) {
        // 멤버 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.UNAUTHORIZED_ACCESS,
                        "사용자를 찾을 수 없습니다."));

        // 멘토 정보 (있는 경우)
        Mentor mentor = mentorRepository.findByMemberId(memberId).orElse(null);

        // 파일 개수 제한 확인
        long currentFileCount = postAttachmentRepository.countByMemberId(memberId);
        if (currentFileCount + files.size() > maxFilesPerUser) {
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED,
                    "파일 업로드 제한을 초과했습니다. 최대 " + maxFilesPerUser + "개까지 가능합니다.");
        }

        List<FileInfoDto> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            // 파일 존재 확인
            if (file.isEmpty()) {
                throw new PostAttachmentException(PostAttachmentErrorCode.FILE_REQUIRED);
            }

            // 파일 크기 확인
            if (file.getSize() > maxFileSize) {
                throw new PostAttachmentException(PostAttachmentErrorCode.FILE_SIZE_EXCEEDED,
                        "파일 크기가 허용 범위를 초과했습니다. 최대 " + (maxFileSize / (1024 * 1024)) + "MB까지 가능합니다.");
            }

            try {
                // 원본 파일명 추출
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

                // Firebase Storage에 파일 업로드
                String directory = imageType.toString().toLowerCase();
                String fileUrl = firebaseStorageService.uploadFile(file, directory);

                // 마크다운용 고유 식별자 (필요한 경우)
                String uniqueIdentifier = null;
                if (imageType != ImageType.NORMAL) {
                    uniqueIdentifier = UUID.randomUUID().toString();
                }

                // DB 저장
                PostAttachment attachment = PostAttachment.builder()
                        .filename(UUID.randomUUID().toString() + "_" + originalFilename)
                        .originalFilename(originalFilename)
                        .storeFilePath(fileUrl) // Firebase URL 저장
                        .fileSize(file.getSize())
                        .member(member)
                        .mentor(mentor)
                        .imageType(imageType)
                        .uniqueIdentifier(uniqueIdentifier)
                        .build();

                postAttachmentRepository.save(attachment);

                FileInfoDto fileInfo;

                if (imageType != ImageType.NORMAL && uniqueIdentifier != null) { // 마크다운 이미지인 경우 URL 및 식별자 추가
                    fileInfo = FileInfoDto.builder()
                            .attachmentId(attachment.getId())
                            .originalFilename(originalFilename)
                            .fileSize(file.getSize())
                            .fileUrl("/api/files/markdown-images/" + uniqueIdentifier)
                            .uniqueIdentifier(uniqueIdentifier)
                            .build();
                } else { // 파일 정보 DTO 생성
                    fileInfo = FileInfoDto.builder()
                            .attachmentId(attachment.getId())
                            .originalFilename(originalFilename)
                            .fileSize(file.getSize())
                            .build();
                }

                uploadedFiles.add(fileInfo);
                log.info("Firebase에 파일 업로드 완료: {}", originalFilename);

            } catch (Exception e) {
                log.error("파일 업로드 실패", e);
                throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
                        "파일 저장 중 오류가 발생했습니다: " + e.getMessage());
            }
        }

        // 마크다운 텍스트가 제공된 경우, 이미지 참조 처리
        if (markdownText != null && !markdownText.isEmpty()) {
            List<PostAttachment> markdownAttachments = processMarkdownImages(markdownText, member, mentor, imageType);

            // 처리된 마크다운 이미지를 응답에 추가
            for (PostAttachment attachment : markdownAttachments) {
                FileInfoDto fileInfo = FileInfoDto.builder()
                        .attachmentId(attachment.getId())
                        .originalFilename(attachment.getOriginalFilename())
                        .fileSize(attachment.getFileSize())
                        .fileUrl("/api/files/markdown-images/" + attachment.getUniqueIdentifier())
                        .uniqueIdentifier(attachment.getUniqueIdentifier())
                        .build();

                uploadedFiles.add(fileInfo);
            }
        }

        return uploadedFiles;
    }

    //마크다운 텍스트에서 이미지 참조를 처리하는 메서드
    private List<PostAttachment> processMarkdownImages(String markdownText, Member member, Mentor mentor, ImageType imageType) {
        List<PostAttachment> processedAttachments = new ArrayList<>();

        // 마크다운에서 이미지 패턴 찾기
        Pattern pattern = Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(markdownText);

        while (matcher.find()) {
            String altText = matcher.group(1);
            String imageUrl = matcher.group(2);

            // 내부 이미지 참조 처리
            if (imageUrl.contains("/api/files/markdown-images/")) {
                // URL에서 uniqueIdentifier 추출
                String uniqueIdentifier = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

                // 해당 식별자로 첨부 파일 찾기
                Optional<PostAttachment> attachment = postAttachmentRepository.findByUniqueIdentifier(uniqueIdentifier);

                if (attachment.isPresent()) {
                    PostAttachment existingAttachment = attachment.get();

                    // 이미 처리된 이미지인지 확인
                    if (!processedAttachments.contains(existingAttachment)) {
                        processedAttachments.add(existingAttachment);
                        log.info("처리된 이미지 목록에 추가");
                    } else {
                        log.info("이미 처리된 이미지 - 중복 제외");
                    }
                } else {
                    log.warn("이미지를 찾을 수 없음 - uniqueIdentifier: {}", uniqueIdentifier);
                }
            }
            // 새로운 이미지 업로드가 필요한 경우 (data:image/... 형식의 base64 인코딩 이미지)
            else if (imageUrl.startsWith("data:image/")) {
                try {
                    log.info("Base64 이미지 감지 - 이미지 처리 시작");
                    // Base64 이미지를 Firebase에 업로드하고 URL로 변환
                    PostAttachment newImageAttachment = saveBase64Image(imageUrl, altText, member, mentor, imageType);
                    processedAttachments.add(newImageAttachment);

                    // 마크다운 텍스트에서 Base64 이미지를 Firebase URL로 대체할 수 있지만,
                    // 여기서는 DB에만 저장하고 클라이언트에서 처리하도록 함
                    log.info("Base64 이미지를 Firebase URL로 변환 완료 - ID: {}", newImageAttachment.getId());
                } catch (Exception e) {
                    log.error("Base64 이미지 처리 중 오류 발생", e);
                }
            }

            // GitHub 및 외부 URL 처리
            else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                try {
                    // 이미지 데이터 직접 다운로드
                    byte[] imageData = downloadImageFromUrl(imageUrl);

                    // 확장자 추출
                    String extension = determineImageExtension(imageUrl);

                    // 고유 식별자 생성
                    String uniqueIdentifier = UUID.randomUUID().toString();

                    // 이미지 유효성 검사
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
                    if (bufferedImage == null) {
                        log.warn("유효하지 않은 이미지: {}", imageUrl);
                        continue;
                    }

                    // Firebase Storage에 업로드
                    String contentType = determineContentTypeByFilename("image" + extension);
                    String directory = imageType.toString().toLowerCase();
                    String fileUrl = firebaseStorageService.uploadFile(
                            imageData,
                            uniqueIdentifier + extension,
                            contentType,
                            directory
                    );

                    // DB 저장
                    PostAttachment attachment = PostAttachment.builder()
                            .filename(uniqueIdentifier + extension)
                            .originalFilename(altText + extension)
                            .storeFilePath(fileUrl)
                            .fileSize((long) imageData.length)
                            .member(member)
                            .mentor(mentor)
                            .imageType(imageType)
                            .uniqueIdentifier(uniqueIdentifier)
                            .build();

                    PostAttachment savedAttachment = postAttachmentRepository.save(attachment);
                    processedAttachments.add(savedAttachment);
                    log.info("외부 URL 이미지 성공적으로 저장: {}", imageUrl);
                } catch (Exception e) {
                    log.error("외부 URL 이미지 처리 실패: {}", imageUrl, e);
                }
            }
        }

        return processedAttachments;
    }

    // 확장자 결정 메서드 추가
    private String determineImageExtension(String url) {
        String lowercaseUrl = url.toLowerCase();
        if (lowercaseUrl.contains(".gif")) return ".gif";
        if (lowercaseUrl.contains(".png")) return ".png";
        if (lowercaseUrl.contains(".jpg") || lowercaseUrl.contains(".jpeg")) return ".jpg";
        if (lowercaseUrl.contains(".webp")) return ".webp";
        return ".jpg"; // 기본값
    }

    //Base64 인코딩된 이미지를 디코딩하고 Firebase에 저장하는 메서드
    private PostAttachment saveBase64Image(String base64Image, String altText, Member member, Mentor mentor, ImageType imageType) {
        try {
            log.info("Base64 이미지 처리 시작");

            // "data:image/jpeg;base64," 형식에서 데이터 부분만 추출
            String[] parts = base64Image.split(",");
            String imageData = parts.length > 1 ? parts[1] : parts[0];

            // MIME 타입 추출 (예: "image/jpeg")
            String mimeType = "image/jpeg"; // 기본값
            if (parts.length > 1 && parts[0].contains("image/")) {
                mimeType = parts[0].substring(parts[0].indexOf("image/"), parts[0].indexOf(";base64"));
            }

            // 확장자 결정
            String extension = ".jpg"; // 기본값
            if (mimeType.equals("image/png")) extension = ".png";
            else if (mimeType.equals("image/gif")) extension = ".gif";
            else if (mimeType.equals("image/svg+xml")) extension = ".svg";

            // Base64 문자열 정리 (공백, 개행 문자 제거)
            imageData = imageData.trim().replaceAll("\\s", "");

            // Base64 디코딩
            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(imageData);
                log.info("Base64 디코딩 성공: {} 바이트", imageBytes.length);

                // 이미지 유효성 검사 추가
                try {
                    BufferedImage testImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    if (testImage == null) {
                        log.warn("Base64에서 디코딩된 데이터가 유효한 이미지가 아닙니다");
                    }
                } catch (Exception e) {
                    log.warn("Base64 이미지 유효성 검사 실패: {}", e.getMessage());
                }
            } catch (IllegalArgumentException e) {
                log.error("Base64 디코딩 실패", e);
                throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
                        "올바르지 않은 Base64 인코딩 형식입니다: " + e.getMessage());
            }

            // 원본 파일명 생성
            String originalFilename = (altText.isEmpty() ? "image" : altText) + extension;

            // Firebase에 이미지 업로드
            String directory = imageType.toString().toLowerCase();
            String fileUrl = firebaseStorageService.uploadFile(
                    imageBytes,
                    originalFilename,
                    mimeType,
                    directory
            );
            log.info("Firebase에 Base64 이미지 업로드 완료: {}", fileUrl);

            // 마크다운용 고유 식별자 생성
            String uniqueIdentifier = UUID.randomUUID().toString();

            // DB 저장
            PostAttachment attachment = PostAttachment.builder()
                    .filename(UUID.randomUUID().toString() + extension)
                    .originalFilename(altText + extension) // 대체 텍스트를 원본 파일명으로 사용
                    .storeFilePath(fileUrl) // Firebase URL 저장
                    .fileSize((long) imageBytes.length)
                    .member(member)
                    .mentor(mentor)
                    .imageType(imageType)
                    .uniqueIdentifier(uniqueIdentifier)
                    .build();

            PostAttachment savedAttachment = postAttachmentRepository.save(attachment);
            log.info("Base64 이미지 정보 DB 저장 완료 - ID: {}", savedAttachment.getId());

            return savedAttachment;
        } catch (PostAttachmentException e) {
            throw e; // 이미 처리된 예외는 그대로 던짐
        } catch (Exception e) {
            log.error("Base64 이미지 처리 중 예외 발생", e);
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
                    "이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 마크다운 전용 처리 메서드
    @Transactional
    public List<FileInfoDto> processMarkdownOnly(String markdownText, ImageType imageType, Long memberId) {
        if (markdownText == null || markdownText.isEmpty()) {
            return new ArrayList<>();
        }

        // 멤버 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.UNAUTHORIZED_ACCESS,
                        "사용자를 찾을 수 없습니다."));

        // 멘토 정보 (있는 경우)
        Mentor mentor = mentorRepository.findByMemberId(memberId).orElse(null);

        List<FileInfoDto> result = new ArrayList<>();

        // 마크다운에서 이미지 추출 및 처리
        List<PostAttachment> processedAttachments = processMarkdownImages(markdownText, member, mentor, imageType);

        // 이미지가 없는 경우 빈 리스트 반환
        if (processedAttachments.isEmpty()) {
            return result;
        }

        // 파일 개수 제한 확인
        long currentFileCount = postAttachmentRepository.countByMemberId(memberId);
        if (currentFileCount + processedAttachments.size() > maxFilesPerUser) {
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED,
                    "파일 업로드 제한을 초과했습니다. 최대 " + maxFilesPerUser + "개까지 가능합니다.");
        }

        // 추출된 이미지들을 DTO로 변환
        for (PostAttachment attachment : processedAttachments) {
            FileInfoDto fileInfo = FileInfoDto.builder()
                    .attachmentId(attachment.getId())
                    .originalFilename(attachment.getOriginalFilename())
                    .fileSize(attachment.getFileSize())
                    .fileUrl("/api/files/markdown-images/" + attachment.getUniqueIdentifier())
                    .uniqueIdentifier(attachment.getUniqueIdentifier())
                    .build();

            result.add(fileInfo);
        }

        return result;
    }

    //파일 삭제 처리
    @Transactional
    public void deleteFile(Long attachmentId, Long memberId) {
        // 파일 존재 확인
        PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.FILE_NOT_FOUND));

        // 권한 확인 (본인 파일만 삭제 가능)
        if (!attachment.getMember().getId().equals(memberId)) {
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_DELETE_NO_PERMISSION);
        }

        try {
            // Firebase URL인 경우에만 Storage에서 삭제
            if (attachment.getStoreFilePath().contains("firebasestorage.googleapis.com")) {
                // Firebase Storage에서 파일 삭제
                firebaseStorageService.deleteFile(attachment.getStoreFilePath());
                log.info("Firebase Storage에서 파일 삭제 완료: {}", attachment.getStoreFilePath());
            }

            // DB 레코드 삭제
            postAttachmentRepository.delete(attachment);
            log.info("DB에서 파일 기록 삭제 완료: ID={}", attachmentId);

        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생", e);
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
                    "파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    //일반 첨부 파일 다운로드
    public Map<String, Object> downloadFile(Long attachmentId, Long memberId) {
        // 파일 존재 확인
        PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.FILE_NOT_FOUND));

        Mentor mentor = mentorRepository.findByMemberId(memberId).orElse(null);

        if (mentor == null) {
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_ACCESS_DENIED,
                    "해당 파일에 접근할 권한이 없습니다. 멘토 또는 관리자만 접근 가능합니다.");
        }

        try {
            // 파일 URL에서 파일 다운로드
            URL url = new URL(attachment.getStoreFilePath());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
                        "파일에 접근할 수 없습니다. 응답 코드: " + responseCode);
            }

            // 컨텐츠 타입 확인
            String contentType = connection.getContentType();
            if (contentType == null) {
                contentType = determineContentTypeByFilename(attachment.getOriginalFilename());
            }

            // 파일 데이터 읽기
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            final byte[] fileData = outputStream.toByteArray();
            outputStream.close();

            // ByteArrayResource 생성
            ByteArrayResource resource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() {
                    return attachment.getOriginalFilename();
                }
            };

            // 다운로드용 파일 정보 반환
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("resource", resource);
            fileInfo.put("contentType", contentType);
            fileInfo.put("fileName", attachment.getOriginalFilename());

            return fileInfo;
        } catch (IOException e) {
            log.error("파일 다운로드 중 오류 발생", e);
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
                    "파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    //마크다운 이미지 다운로드
    public Map<String, Object> downloadMarkdownImage(String uniqueIdentifier, Integer width, Integer height) {
        // 식별자로 파일 조회
        PostAttachment attachment = postAttachmentRepository.findByUniqueIdentifier(uniqueIdentifier)
                .orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.FILE_NOT_FOUND, "이미지를 찾을 수 없습니다."));

        try {
            byte[] imageBytes = null;
            String contentType = null;
            String storedPath = attachment.getStoreFilePath();
            log.info("마크다운 이미지 다운로드 시작: {}", storedPath);
            boolean isValidImage = false;

            // Base64 데이터 URI인 경우 바로 처리
            if (storedPath.startsWith("data:")) {
                log.info("Base64 데이터 URI 직접 처리");
                String[] parts = storedPath.split(",");
                String metadataPart = parts[0];
                String base64Data = parts.length > 1 ? parts[1] : "";

                contentType = metadataPart.substring(metadataPart.indexOf(":") + 1, metadataPart.indexOf(";"));
                imageBytes = Base64.getDecoder().decode(base64Data);
                log.info("Base64 직접 디코딩 완료: {} 바이트", imageBytes.length);

                // 디코딩된 이미지 유효성 검사
                isValidImage = validateImage(imageBytes);
            } else {
                // Firebase URL에서 데이터 가져오기
                URL url = new URL(storedPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
                            "이미지에 접근할 수 없습니다. 응답 코드: " + responseCode);
                }

                // 콘텐츠 타입 확인
                contentType = connection.getContentType();
                if (contentType == null) {
                    contentType = determineContentTypeByFilename(attachment.getOriginalFilename());
                    log.info("콘텐츠 타입 추정: {}", contentType);
                } else {
                    log.info("콘텐츠 타입 확인: {}", contentType);
                }

                // 응답 데이터 읽기
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                byte[] responseData = outputStream.toByteArray();
                log.info("Firebase에서 데이터 다운로드 완료: {} 바이트", responseData.length);

                // 다운로드한 데이터가 JSON인 경우 Base64 이미지 추출 시도
                if (contentType != null && contentType.contains("application/json")) {
                    imageBytes = extractImageFromJson(responseData);
                    if (imageBytes != null) {
                        // Data URI를 그대로 반환하는 경우 콘텐츠 타입을 text/plain으로 설정
                        contentType = "text/plain";
                        // Data URI는 유효한 이미지가 아니므로 유효성 검사 건너뛰기
                        isValidImage = false;
                        log.info("Base64 Data URI를 직접 반환합니다. 리사이징 건너뜀");
                    } else {
                        // JSON에서 이미지를 추출하지 못한 경우 원본 데이터 사용
                        imageBytes = responseData;
                    }
                } else {
                    // JSON이 아닌 경우 원본 데이터 사용
                    imageBytes = responseData;
                    // 일반 이미지 유효성 검사
                    isValidImage = validateImage(imageBytes);
                }
            }

            // 유효한 이미지인 경우에만 리사이징 시도
            if (isValidImage && width != null && height != null && imageBytes.length > 300) {
                try {
                    log.info("이미지 리사이징 시도: {}x{}", width, height);
                    imageBytes = resizeImage(imageBytes, width, height, contentType);
                    log.info("이미지 리사이징 성공: {} 바이트", imageBytes.length);
                } catch (Exception e) {
                    log.warn("이미지 리사이징 실패, 원본 이미지 사용: {}", e.getMessage());
                }
            } else if (!isValidImage) {
                log.warn("유효한 이미지가 아니므로 리사이징을 건너뜁니다");
            }

            // 결과 반환
            ByteArrayResource resource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return attachment.getOriginalFilename() != null ?
                            attachment.getOriginalFilename() : "image.jpg";
                }
            };

            Map<String, Object> imageInfo = new HashMap<>();
            imageInfo.put("resource", resource);
            imageInfo.put("contentType", contentType);
            imageInfo.put("fileName", attachment.getOriginalFilename());

            return imageInfo;
        } catch (Exception e) {
            log.error("이미지 다운로드 중 오류 발생", e);
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
                    "이미지 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 다양한 User-Agent 설정으로 차단 방지
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

        // 리다이렉트 허용
        connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);

        int responseCode = connection.getResponseCode();

        // 리다이렉트 처리
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
            String redirectUrl = connection.getHeaderField("Location");
            log.info("리다이렉트된 URL: {}", redirectUrl);
            connection = (HttpURLConnection) new URL(redirectUrl).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            responseCode = connection.getResponseCode();
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download image. Response Code: " + responseCode);
        }

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    // JSON 응답에서 Base64 이미지 데이터 추출하는 메소드
    private byte[] extractImageFromJson(byte[] jsonData) {
        try {
            // JSON 문자열로 변환
            String jsonContent = new String(jsonData, StandardCharsets.UTF_8).trim();

            // 큰따옴표 제거 (JSON 문자열인 경우)
            if (jsonContent.startsWith("\"") && jsonContent.endsWith("\"")) {
                jsonContent = jsonContent.substring(1, jsonContent.length() - 1);
            }

            // Base64 이미지 데이터인지 확인
            if (jsonContent.startsWith("data:image/")) {
                log.info("JSON에서 Base64 이미지 데이터 발견");

                // 방법 3: 원본 Base64 데이터를 그대로 Data URI로 반환
                log.info("Base64 데이터를 Data URI로 직접 반환");
                return jsonContent.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("JSON 파싱 및 이미지 데이터 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    // 이미지 유효성 검증 메소드
    private boolean validateImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return false;
        }

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bis);
            if (image != null) {
                log.info("유효한 이미지 확인됨: {}x{}", image.getWidth(), image.getHeight());
                return true;
            } else {
                log.warn("유효하지 않은 이미지 데이터");
                return false;
            }
        } catch (Exception e) {
            log.warn("이미지 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }

    // 이미지 리사이징 메소드
    private byte[] resizeImage(byte[] imageData, int width, int height, String contentType) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage originalImage = ImageIO.read(bis);

        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        // 고품질 리사이징을 위한 설정
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String formatName = getFormatName(contentType);
        ImageIO.write(resizedImage, formatName, bos);

        return bos.toByteArray();
    }

    // 컨텐츠 타입에서 포맷 이름 추출 (Thumbnailator용)
    private String getFormatName(String contentType) {
        if (contentType.contains("png")) return "png";
        else if (contentType.contains("gif")) return "gif";
        else if (contentType.contains("bmp")) return "bmp";
        else return "jpeg"; // 기본값
    }

    // 파일명으로 컨텐츠 타입 추정하는 유틸리티 메서드
    private String determineContentTypeByFilename(String filename) {
        if (filename == null) return "application/octet-stream";

        String lowercaseFilename = filename.toLowerCase();
        if (lowercaseFilename.endsWith(".png")) return "image/png";
        if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) return "image/jpeg";
        if (lowercaseFilename.endsWith(".gif")) return "image/gif";
        if (lowercaseFilename.endsWith(".svg")) return "image/svg+xml";
        if (lowercaseFilename.endsWith(".pdf")) return "application/pdf";
        if (lowercaseFilename.endsWith(".txt")) return "text/plain";
        if (lowercaseFilename.endsWith(".html")) return "text/html";
        if (lowercaseFilename.endsWith(".css")) return "text/css";
        if (lowercaseFilename.endsWith(".js")) return "application/javascript";
        if (lowercaseFilename.endsWith(".json")) return "application/json";
        if (lowercaseFilename.endsWith(".xml")) return "application/xml";
        if (lowercaseFilename.endsWith(".zip")) return "application/zip";
        if (lowercaseFilename.endsWith(".doc")) return "application/msword";
        if (lowercaseFilename.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lowercaseFilename.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lowercaseFilename.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lowercaseFilename.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lowercaseFilename.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

        // 기본값
        return "application/octet-stream";
    }

    //붙여넣기한 이미지를 Firebase에 업로드하고 URL을 반환하는 메서드
    @Transactional
    public FileInfoDto uploadPastedImage(byte[] imageData, String contentType, Long memberId) {
        // 멤버 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.UNAUTHORIZED_ACCESS,
                        "사용자를 찾을 수 없습니다."));

        // 멘토 정보 (있는 경우)
        Mentor mentor = mentorRepository.findByMemberId(memberId).orElse(null);

        // 이미지 크기 확인
        if (imageData.length > maxFileSize) {
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_SIZE_EXCEEDED,
                    "이미지 크기가 허용 범위를 초과했습니다. 최대 " + (maxFileSize / (1024 * 1024)) + "MB까지 가능합니다.");
        }

        try {
            // 확장자 결정
            String extension = ".jpg"; // 기본값
            if (contentType.equals("image/png")) extension = ".png";
            else if (contentType.equals("image/gif")) extension = ".gif";
            else if (contentType.equals("image/svg+xml")) extension = ".svg";

            // 파일명 생성 (UUID)
            String filename = UUID.randomUUID().toString() + extension;

            // Firebase에 업로드
            String directory = "pasted_images";
            String fileUrl = firebaseStorageService.uploadFile(imageData, filename, contentType, directory);
            log.info("붙여넣기 이미지 Firebase 업로드 완료: {}", filename);

            // 마크다운용 고유 식별자 생성
            String uniqueIdentifier = UUID.randomUUID().toString();

            // DB 저장
            PostAttachment attachment = PostAttachment.builder()
                    .filename(filename)
                    .originalFilename("pasted_image" + extension)
                    .storeFilePath(fileUrl)
                    .fileSize((long) imageData.length)
                    .member(member)
                    .mentor(mentor)
                    .imageType(ImageType.MARKDOWN_SELF_INTRODUCTION) // 또는 다른 적절한 타입
                    .uniqueIdentifier(uniqueIdentifier)
                    .build();

            PostAttachment savedAttachment = postAttachmentRepository.save(attachment);
            log.info("붙여넣기 이미지 DB 저장 완료 - ID: {}", savedAttachment.getId());

            // 응답 DTO 생성
            return FileInfoDto.builder()
                    .attachmentId(savedAttachment.getId())
                    .originalFilename(savedAttachment.getOriginalFilename())
                    .fileSize(savedAttachment.getFileSize())
                    .fileUrl("/api/files/markdown-images/" + uniqueIdentifier)
                    .uniqueIdentifier(uniqueIdentifier)
                    .build();

        } catch (Exception e) {
            log.error("붙여넣기 이미지 처리 중 오류 발생", e);
            throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
                    "이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}