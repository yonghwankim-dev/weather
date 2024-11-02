package site.weather.api.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import site.weather.api.weather.service.WeatherWebClient;

@Configuration
public class WeatherConfig {

	@Bean
	public WeatherWebClient weatherWebClient(
		@Value("${weather.base-url}") String baseUrl,
		@Value("${weather.appid}") String appid) {
		WebClient webClient = WebClient.builder()
			.baseUrl(baseUrl)
			.build();
		return new WeatherWebClient(webClient, appid);
	}
}
