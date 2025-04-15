package com.dementor.global.security;

import com.dementor.global.config.MultipartJackson2HttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter;

	public WebConfig(MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter) {
		this.multipartJackson2HttpMessageConverter = multipartJackson2HttpMessageConverter;
	}

//	@Override
//	public void addCorsMappings(CorsRegistry registry) {
//		registry.addMapping("/**")
//			.allowedOrigins(
//				"https://www.dementor.site",
//				"https://api.dementor.site",
//				"https://admin.dementor.site",
//				"https://local.dementor.site:5173",
//				"https://admin-local.dementor.site:5174"
//			)
//			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
//			.allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
//				"Access-Control-Request-Method", "Access-Control-Request-Headers")
//			.exposedHeaders("Authorization")
//			.allowCredentials(true)
//			.maxAge(3600);
//	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(multipartJackson2HttpMessageConverter);
	}
}
