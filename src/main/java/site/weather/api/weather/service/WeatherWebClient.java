package site.weather.api.weather.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import site.weather.api.weather.domain.Units;
import site.weather.api.weather.dto.response.WeatherResponse;

public class WeatherWebClient {
	private final WebClient webClient;
	private final String appid;

	public WeatherWebClient(WebClient webClient, String appid) {
		this.webClient = webClient;
		this.appid = appid;
	}

	@Cacheable(value = "weatherCache", key = "#city")
	public Mono<WeatherResponse> fetchWeatherByCity(String city) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/data/2.5/weather")
				.queryParam("q", city)
				.queryParam("appid", appid)
				.queryParam("units", Units.METRIC)
				.build())
			.retrieve()
			.bodyToMono(WeatherResponse.class)
			.log();
	}
}
