package site.weather.api.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import site.weather.api.weather.service.WeatherWebClient;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@EnableScheduling
public class WeatherConfig implements WebSocketMessageBrokerConfigurer {

	private final ObjectMapper objectMapper;

	@Bean
	public WeatherWebClient weatherWebClient(
		@Value("${weather.base-url}") String baseUrl,
		@Value("${weather.appid}") String appid) {
		WebClient webClient = WebClient.builder()
			.baseUrl(baseUrl)
			.codecs(configurer -> {
				configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
				configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
			})
			.build();
		return new WeatherWebClient(webClient, appid);
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/weather")
			.setAllowedOrigins("http://localhost:8080")
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}
}
