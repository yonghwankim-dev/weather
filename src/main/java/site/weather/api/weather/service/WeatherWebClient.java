package site.weather.api.weather.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import site.weather.api.weather.domain.Units;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.error.exception.WeatherException;

@Slf4j
public class WeatherWebClient {
	private final WebClient webClient;
	private final String appid;

	public WeatherWebClient(WebClient webClient, String appid) {
		this.webClient = webClient;
		this.appid = appid;
	}

	@Cacheable(value = "weatherCache", key = "#city")
	public Mono<WeatherResponse> fetchWeatherByCity(String city) {
		log.info("call fetchWeatherByCity, city={}", city);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/data/2.5/weather")
				.queryParam("q", city)
				.queryParam("appid", appid)
				.queryParam("units", Units.METRIC)
				.build())
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, response -> {
				if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
					return Mono.error(new WeatherException("invalid API key"));
				}
				if (response.statusCode() == HttpStatus.NOT_FOUND) {
					return Mono.error(new WeatherException("not found city " + city));
				}
				if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
					return Mono.error(new WeatherException("too many request"));
				}
				return response.createError();
			})
			.bodyToMono(WeatherResponse.class)
			.log();
	}
}
