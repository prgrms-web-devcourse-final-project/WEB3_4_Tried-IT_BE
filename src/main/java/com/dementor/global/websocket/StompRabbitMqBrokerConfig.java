package com.dementor.global.websocket;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompRabbitMqBrokerConfig implements WebSocketMessageBrokerConfigurer {

	@Value("${spring.rabbitmq.host}")
	private String rabbitmqHost;

	@Value("${spring.rabbitmq.username}")
	private String rabbitmqUsername;

	@Value("${spring.rabbitmq.password}")
	private String rabbitmqPassword;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry
				.addEndpoint("/ws")
				.setAllowedOrigins(
						"https://www.dementor.site",
						"https://api.dementor.site",
						"https://admin.dementor.site",
						"https://local.dementor.site:5173",
						"https://localhost:5173",
						"https://admin-local.dementor.site:5174",
						"https://cdpn.io"
				)
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry
			.setApplicationDestinationPrefixes("/app")// 클-> 서버
			.enableStompBrokerRelay("/topic") //서버->클 브로드캐스트 경로
			.setRelayHost(rabbitmqHost)
			.setRelayPort(61613)
			.setClientLogin(rabbitmqUsername)
			.setClientPasscode(rabbitmqPassword)
			.setSystemLogin(rabbitmqUsername)
			.setSystemPasscode(rabbitmqPassword);
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}