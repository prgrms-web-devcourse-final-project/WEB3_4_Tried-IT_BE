package com.dementor.global.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {
    // 멘토링 클래스용 기본 페이지네이션
    public static Pageable getMentoringClassPageable(Pageable pageable) {
        if (pageable.getSort().isSorted()) // 정렬 조건이 있는 경우
            return pageable;
        else // 없으면 config 설정대로
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
    }

    // TODO : 다른 도메인 페이지네이션 추가 (멘토링 클래스 참고)
}
