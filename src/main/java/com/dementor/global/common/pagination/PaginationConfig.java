package com.dementor.global.common.pagination;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 도메인별로 설정 코드를 다르게 할 수 있음 (페이지 사이즈, 최대 페이지 크기 등)
public class PaginationConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolverList) {
		// 멘토링 클래스용 resolver
		resolverList.add(mentoringClassPageableResolver());
		resolverList.add(mentorEditPageableResolver());

		// TODO : 다른 도메인 resolver 생성
		//        resolverList.add(다른 도메인 resolver 메소드());
	}

	@Bean // 멘토링 클래스용 resolver 등록
	public PageableHandlerMethodArgumentResolver mentoringClassPageableResolver() {
		// HandlerMethodArgumentResolver 얘가 request param의 page size 이런거를 알아서 처리해줌
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

		resolver.setOneIndexedParameters(true); // 1부터 시작하는 페이지 번호 (false는 시작 페이지가 0)
		resolver.setMaxPageSize(50); // 최대 페이지 크기 50으로 제한
		resolver.setPageParameterName("page"); // 페이지 파라미터 이름
		resolver.setSizeParameterName("size"); // 크기 파라미터 이름

		resolver.setFallbackPageable(
			PageRequest.of(
				0,
				10, // 10개로 전체 제한
				Sort.by(Sort.Direction.DESC, "createdAt") // 특별히 정렬 기준을 안보낼때 createdAt을 기준으로 정렬
			));

		return resolver;
	}

	@Bean // 멘토 정보수정 resolver 등록
	public PageableHandlerMethodArgumentResolver mentorEditPageableResolver() {
		// HandlerMethodArgumentResolver 얘가 request param의 page size 이런거를 알아서 처리해줌
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

		resolver.setOneIndexedParameters(true); // 1부터 시작하는 페이지 번호 (false는 시작 페이지가 0)
		resolver.setMaxPageSize(50); // 최대 페이지 크기 50으로 제한
		resolver.setPageParameterName("page"); // 페이지 파라미터 이름
		resolver.setSizeParameterName("size"); // 크기 파라미터 이름

		resolver.setFallbackPageable(
			PageRequest.of(
				0,
				10, // 10개로 전체 제한
				Sort.by(Sort.Direction.DESC, "createdAt") // 특별히 정렬 기준을 안보낼때 createdAt을 기준으로 정렬
			));

		return resolver;
	}

	// 다른 도메인 Bean 생성 가능 ( 위 참고 )
}
