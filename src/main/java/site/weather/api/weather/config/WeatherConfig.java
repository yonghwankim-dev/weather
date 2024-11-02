package site.weather.api.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import site.weather.api.weather.service.WeatherWebClient;

@Configuration
@RequiredArgsConstructor
public class WeatherConfig {

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
}
