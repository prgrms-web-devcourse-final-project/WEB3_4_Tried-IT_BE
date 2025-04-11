package com.dementor.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@OpenAPIDefinition(servers = {
	@Server(url = "https://api.dementor.site/", description = "Production Server"),
	@Server(url = "http://localhost:8080/", description = "Development Server"),
})
@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		String jwt = "JWT";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
		Components components = new Components()
			.addSecuritySchemes(jwt, new SecurityScheme()
				.name(jwt)
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
			);

		return new OpenAPI()
			.info(new Info()
				.title("API 문서")
				.description("API 명세서")
				.version("1.0.0"))
			.addSecurityItem(securityRequirement)
			.components(components);
	}
}