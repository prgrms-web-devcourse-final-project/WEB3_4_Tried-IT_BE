package com.dementor.domain.postattachment.service;

import com.dementor.domain.member.entity.Member;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.postattachment.dto.response.FileResponse.FileInfoDto;
import com.dementor.domain.postattachment.entity.PostAttachment;
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
	private final FirebaseStorageService firebaseStorageService;

	@Value("${file.max-size}")
	private long maxFileSize;

	@Value("${file.max-per-user}")
	private int maxFilesPerUser;

	public boolean isFileOwner(Long fileId, Long userId) {
		if (fileId == null || userId == null) {
			return false;
		}
		return postAttachmentRepository.findById(fileId)
			.map(file -> {
				Member member = file.getMember();
				return member != null && member.getId().equals(userId);
			})
			.orElse(false);
	}

	//멘토 지원서용 파일 업로드 메소드
	@Transactional
	public void uploadFilesApply(
		List<MultipartFile> files,
		MentorApplyProposal applyProposal) {

		if (files == null || files.isEmpty()) {
			return;
		}

		Long memberId = applyProposal.getMember().getId();
		// 파일 개수 체크
		long currentFileCount = postAttachmentRepository.countByMemberId(memberId);
		if (currentFileCount + files.size() > maxFilesPerUser) {
			throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED,
				"파일 업로드 제한을 초과했습니다. 최대 " + maxFilesPerUser + "개까지 가능합니다.");
		}

		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_REQUIRED);
			}

			if (file.getSize() > maxFileSize) {
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_SIZE_EXCEEDED,
					"파일 크기가 허용 범위를 초과했습니다. 최대 " + (maxFileSize / (1024 * 1024)) + "MB까지 가능합니다.");
			}

			try {
				String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
				String directory = "apply";
				String fileUrl = firebaseStorageService.uploadFile(file, directory);

				PostAttachment attachment = PostAttachment.builder()
					.filename(UUID.randomUUID().toString() + "_" + originalFilename)
					.originalFilename(originalFilename)
					.storeFilePath(fileUrl)
					.fileSize(file.getSize())
					.mentorApplyProposal(applyProposal)
					.mentorEditProposal(null)
					.build();

				postAttachmentRepository.save(attachment);
			} catch (Exception e) {
				log.error("파일 업로드 실패: {}", e.getMessage());
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
					"파일 저장 중 오류가 발생했습니다: " + e.getMessage());
			}
		}

	}

	//멘토 정보 수정용 파일 업로드 메소드
	@Transactional
	public void uploadFilesEdit(
		List<MultipartFile> files,
		MentorEditProposal editProposal) {

		if (files == null || files.isEmpty()) {
			return;
		}

		// 실제 업로드 가능한 파일이 있는지 확인 (빈 MultipartFile 객체는 있지만 내용이 없는 경우 처리)
		boolean hasRealFiles = files.stream().anyMatch(file -> !file.isEmpty());
		if (!hasRealFiles) {
			// 실제 업로드할 파일이 없으면 기존 파일 유지
			return;
		}

		Long editProposalId = editProposal.getId();

		// 기존 파일 삭제 로직 추가
		List<PostAttachment> existingAttachments = postAttachmentRepository.findByMentorEditProposalId(editProposalId);
		for (PostAttachment attachment : existingAttachments) {
			// Firebase에서 물리적 파일 삭제
			try {
				String filePath = attachment.getStoreFilePath();
				if (filePath != null && !filePath.isEmpty()) {
					firebaseStorageService.deleteFile(filePath);
				}
			} catch (Exception e) {
				log.warn("파일 삭제 중 오류 발생: {}", e.getMessage());
				// 파일 삭제 실패해도 계속 진행
			}

			// DB에서 첨부파일 레코드 삭제
			postAttachmentRepository.delete(attachment);
		}

		Long memberId = editProposal.getMember().getId();
		// 파일 개수 체크
		long currentFileCount = postAttachmentRepository.countByMemberId(memberId);
		if (currentFileCount + files.size() > maxFilesPerUser) {
			throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED,
				"파일 업로드 제한을 초과했습니다. 최대 " + maxFilesPerUser + "개까지 가능합니다.");
		}

		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_REQUIRED);
			}

			if (file.getSize() > maxFileSize) {
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_SIZE_EXCEEDED,
					"파일 크기가 허용 범위를 초과했습니다. 최대 " + (maxFileSize / (1024 * 1024)) + "MB까지 가능합니다.");
			}

			try {
				String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
				String directory = "edit";
				String fileUrl = firebaseStorageService.uploadFile(file, directory);

				PostAttachment attachment = PostAttachment.builder()
					.filename(UUID.randomUUID().toString() + "_" + originalFilename)
					.originalFilename(originalFilename)
					.storeFilePath(fileUrl)
					.fileSize(file.getSize())
					.mentorApplyProposal(null)
					.mentorEditProposal(editProposal)
					.build();

				postAttachmentRepository.save(attachment);
			} catch (Exception e) {
				log.error("파일 업로드 실패: {}", e.getMessage());
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
					"파일 저장 중 오류가 발생했습니다: " + e.getMessage());
			}
		}

	}

	//마크다운용 이미지 파일 업로드 메소드
	@Transactional
	public List<FileInfoDto> uploadMarkdownImages(List<MultipartFile> images) {
		if (images == null || images.isEmpty()) {
			throw new PostAttachmentException(PostAttachmentErrorCode.FILE_REQUIRED);
		}

		List<FileInfoDto> uploadedFiles = new ArrayList<>();

		for (MultipartFile image : images) {
			if (image.isEmpty()) {
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_REQUIRED);
			}

			if (image.getSize() > maxFileSize) {
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_SIZE_EXCEEDED,
						"파일 크기가 허용 범위를 초과했습니다. 최대 " + (maxFileSize / (1024 * 1024)) + "MB까지 가능합니다.");
			}

			try {
				String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
				String directory = "markdown";
				String fileUrl = firebaseStorageService.uploadFile(image, directory);
				String uniqueIdentifier = UUID.randomUUID().toString();

				PostAttachment attachment = PostAttachment.builder()
						.filename(UUID.randomUUID().toString() + "_" + originalFilename)
						.originalFilename(originalFilename)
						.storeFilePath(fileUrl)
						.fileSize(image.getSize())
						.uniqueIdentifier(uniqueIdentifier)
						.build();

				PostAttachment savedAttachment = postAttachmentRepository.save(attachment);

				FileInfoDto fileInfoDto = FileInfoDto.builder()
						.attachmentId(savedAttachment.getId())
						.originalFilename(savedAttachment.getOriginalFilename())
						.fileSize(savedAttachment.getFileSize())
						.fileUrl("/api/files/markdown-images/" + savedAttachment.getUniqueIdentifier())
						.uniqueIdentifier(savedAttachment.getUniqueIdentifier())
						.build();

				uploadedFiles.add(fileInfoDto);
			} catch (Exception e) {
				log.error("파일 업로드 실패: {}", e.getMessage());
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
						"파일 저장 중 오류가 발생했습니다: " + e.getMessage());
			}
		}

		return uploadedFiles;
	}

	// 파일 접근 권한 확인 메서드
	private boolean hasAccessPermission(Long memberId, PostAttachment attachment) {
		// 파일 소유자인 경우 접근 허용
		Member owner = attachment.getMember();
		return owner != null && owner.getId().equals(memberId);
	}

	// 마크다운 이미지 처리
	private List<PostAttachment> processMarkdownImages(
		String markdownText,
		MentorApplyProposal applyProposal,
		MentorEditProposal editProposal) {

		List<PostAttachment> processedAttachments = new ArrayList<>();
		Pattern pattern = Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");
		Matcher matcher = pattern.matcher(markdownText);
		boolean isApplyProposal = (applyProposal != null);

		while (matcher.find()) {
			String altText = matcher.group(1);
			String imageUrl = matcher.group(2);

			// GitHub 이미지 URL 감지
			if (imageUrl.contains("github.com/user-attachments") ||
				imageUrl.contains("githubusercontent.com") ||
				imageUrl.contains("github-production-user-asset") ||
				imageUrl.contains("github.io") && imageUrl.contains("assets")) {

				log.warn("GitHub 이미지는 직접 액세스할 수 없습니다. 처리를 건너뜁니다: {}", imageUrl);
				continue; // GitHub 이미지는 건너뛰기
			}

			if (imageUrl.contains("/api/files/markdown-images/")) {
				String uniqueIdentifier = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
				Optional<PostAttachment> attachment = postAttachmentRepository.findByUniqueIdentifier(uniqueIdentifier);

				if (attachment.isPresent()) {
					PostAttachment existingAttachment = attachment.get();
					if (!processedAttachments.contains(existingAttachment)) {
						processedAttachments.add(existingAttachment);
					}
				}
			} else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
				try {
					byte[] imageData = downloadImageFromUrl(imageUrl);
					String extension = determineImageExtension(imageUrl);
					String uniqueIdentifier = UUID.randomUUID().toString();

					BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
					if (bufferedImage == null) {
						continue;
					}

					String contentType = determineContentTypeByFilename("image" + extension);
					String directory = "markdown";
					String fileUrl = firebaseStorageService.uploadFile(
						imageData,
						uniqueIdentifier + extension,
						contentType,
						directory
					);

					PostAttachment attachment = PostAttachment.builder()
						.filename(uniqueIdentifier + extension)
						.originalFilename(altText + extension)
						.storeFilePath(fileUrl)
						.fileSize((long)imageData.length)
						.mentorApplyProposal(isApplyProposal ? applyProposal : null)
						.mentorEditProposal(isApplyProposal ? null : editProposal)
						.uniqueIdentifier(uniqueIdentifier)
						.build();

					PostAttachment savedAttachment = postAttachmentRepository.save(attachment);
					processedAttachments.add(savedAttachment);
				} catch (Exception e) {
					log.error("외부 URL 이미지 처리 실패: {}", e.getMessage());
				}
			}
		}

		return processedAttachments;
	}

	private String determineImageExtension(String url) {
		String lowercaseUrl = url.toLowerCase();
		if (lowercaseUrl.contains(".gif"))
			return ".gif";
		if (lowercaseUrl.contains(".png"))
			return ".png";
		if (lowercaseUrl.contains(".jpg") || lowercaseUrl.contains(".jpeg"))
			return ".jpg";
		if (lowercaseUrl.contains(".webp"))
			return ".webp";
		return ".jpg";
	}

	@Transactional
	public void deleteFile(Long attachmentId, Long memberId) {
		PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
			.orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.FILE_NOT_FOUND));

		if (!attachment.getMember().getId().equals(memberId)) {
			throw new PostAttachmentException(PostAttachmentErrorCode.FILE_DELETE_NO_PERMISSION);
		}

		try {
			if (attachment.getStoreFilePath().contains("firebasestorage.googleapis.com")) {
				firebaseStorageService.deleteFile(attachment.getStoreFilePath());
			}

			postAttachmentRepository.delete(attachment);
		} catch (Exception e) {
			log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
			throw new PostAttachmentException(PostAttachmentErrorCode.FILE_UPLOAD_ERROR,
				"파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	public Map<String, Object> downloadFile(Long attachmentId, Long memberId) {
		PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
			.orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.FILE_NOT_FOUND));

		try {
			URL url = new URL(attachment.getStoreFilePath());
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);

			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
					"파일에 접근할 수 없습니다. 응답 코드: " + responseCode);
			}

			String contentType = connection.getContentType();
			if (contentType == null) {
				contentType = determineContentTypeByFilename(attachment.getOriginalFilename());
			}

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

			ByteArrayResource resource = new ByteArrayResource(fileData) {
				@Override
				public String getFilename() {
					return attachment.getOriginalFilename();
				}
			};

			Map<String, Object> fileInfo = new HashMap<>();
			fileInfo.put("resource", resource);
			fileInfo.put("contentType", contentType);
			fileInfo.put("fileName", attachment.getOriginalFilename());

			return fileInfo;
		} catch (IOException e) {
			log.error("파일 다운로드 중 오류 발생: {}", e.getMessage());
			throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
				"파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	public Map<String, Object> downloadMarkdownImage(String uniqueIdentifier, Integer width, Integer height) {
		PostAttachment attachment = postAttachmentRepository.findByUniqueIdentifier(uniqueIdentifier)
			.orElseThrow(() -> new PostAttachmentException(PostAttachmentErrorCode.FILE_NOT_FOUND, "이미지를 찾을 수 없습니다."));

		try {
			byte[] imageBytes;
			String contentType;
			String storedPath = attachment.getStoreFilePath();
			boolean isValidImage = false;

			if (storedPath.startsWith("data:")) {
				String[] parts = storedPath.split(",");
				String metadataPart = parts[0];
				String base64Data = parts.length > 1 ? parts[1] : "";

				contentType = metadataPart.substring(metadataPart.indexOf(":") + 1, metadataPart.indexOf(";"));
				imageBytes = Base64.getDecoder().decode(base64Data);
				isValidImage = false;
			} else {
				URL url = new URL(storedPath);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", "Mozilla/5.0");
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);

				int responseCode = connection.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
						"이미지에 접근할 수 없습니다. 응답 코드: " + responseCode);
				}

				contentType = connection.getContentType();
				if (contentType == null) {
					contentType = determineContentTypeByFilename(attachment.getOriginalFilename());
				}

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try (InputStream inputStream = connection.getInputStream()) {
					byte[] buffer = new byte[8192];
					int bytesRead;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
				}
				byte[] responseData = outputStream.toByteArray();

				if (contentType != null && contentType.contains("application/json")) {
					imageBytes = extractImageFromJson(responseData);
					if (imageBytes != null) {
						contentType = "text/plain";
						isValidImage = false;
					} else {
						imageBytes = responseData;
						isValidImage = validateImage(imageBytes);
					}
				} else {
					imageBytes = responseData;
					isValidImage = validateImage(imageBytes);
				}
			}

			if (isValidImage && width != null && height != null && imageBytes.length > 300) {
				try {
					imageBytes = resizeImage(imageBytes, width, height, contentType);
				} catch (Exception e) {
					// 리사이징 실패 시 원본 이미지 사용
				}
			}

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
			log.error("이미지 다운로드 중 오류 발생: {}", e.getMessage());
			throw new PostAttachmentException(PostAttachmentErrorCode.FILE_READ_ERROR,
				"이미지 다운로드 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
		URL url = new URL(imageUrl);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();

		connection.setRequestProperty("User-Agent",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
		connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

		connection.setInstanceFollowRedirects(true);
		HttpURLConnection.setFollowRedirects(true);

		int responseCode = connection.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
			responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
			responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
			String redirectUrl = connection.getHeaderField("Location");
			connection = (HttpURLConnection)new URL(redirectUrl).openConnection();
			connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
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

	private byte[] extractImageFromJson(byte[] jsonData) {
		try {
			String jsonContent = new String(jsonData, StandardCharsets.UTF_8).trim();

			if (jsonContent.startsWith("\"") && jsonContent.endsWith("\"")) {
				jsonContent = jsonContent.substring(1, jsonContent.length() - 1);
			}

			if (jsonContent.startsWith("data:image/")) {
				return jsonContent.getBytes(StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			// 실패 시 null 반환하여 원본 데이터 사용하도록 함
		}
		return null;
	}

	private boolean validateImage(byte[] imageData) {
		if (imageData == null || imageData.length == 0) {
			return false;
		}

		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
			BufferedImage image = ImageIO.read(bis);
			return image != null;
		} catch (Exception e) {
			return false;
		}
	}

	private byte[] resizeImage(byte[] imageData, int width, int height, String contentType) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
		BufferedImage originalImage = ImageIO.read(bis);

		if (originalImage == null) {
			throw new IOException("이미지 데이터를 읽을 수 없습니다");
		}

		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String formatName = getFormatName(contentType);
		ImageIO.write(resizedImage, formatName, bos);

		return bos.toByteArray();
	}

	private String getFormatName(String contentType) {
		if (contentType == null)
			return "jpeg";
		if (contentType.contains("png"))
			return "png";
		if (contentType.contains("gif"))
			return "gif";
		if (contentType.contains("bmp"))
			return "bmp";
		return "jpeg";
	}

	private String determineContentTypeByFilename(String filename) {
		if (filename == null)
			return "application/octet-stream";

		String lowercaseFilename = filename.toLowerCase();
		if (lowercaseFilename.endsWith(".png"))
			return "image/png";
		if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg"))
			return "image/jpeg";
		if (lowercaseFilename.endsWith(".gif"))
			return "image/gif";
		if (lowercaseFilename.endsWith(".svg"))
			return "image/svg+xml";
		if (lowercaseFilename.endsWith(".pdf"))
			return "application/pdf";
		if (lowercaseFilename.endsWith(".txt"))
			return "text/plain";
		if (lowercaseFilename.endsWith(".html"))
			return "text/html";
		if (lowercaseFilename.endsWith(".css"))
			return "text/css";
		if (lowercaseFilename.endsWith(".js"))
			return "application/javascript";
		if (lowercaseFilename.endsWith(".json"))
			return "application/json";
		if (lowercaseFilename.endsWith(".xml"))
			return "application/xml";
		if (lowercaseFilename.endsWith(".zip"))
			return "application/zip";
		if (lowercaseFilename.endsWith(".doc"))
			return "application/msword";
		if (lowercaseFilename.endsWith(".docx"))
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		if (lowercaseFilename.endsWith(".xls"))
			return "application/vnd.ms-excel";
		if (lowercaseFilename.endsWith(".xlsx"))
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		if (lowercaseFilename.endsWith(".ppt"))
			return "application/vnd.ms-powerpoint";
		if (lowercaseFilename.endsWith(".pptx"))
			return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

		return "application/octet-stream";
	}
}