package site.weather.api.weather.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import site.weather.api.weather.domain.Units;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.error.exception.BadWebClientRequestException;

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
			.onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new BadWebClientRequestException(
				response.statusCode().value(),
				String.format(
					"An external request error with a 4xx status code. statusCode: %s, response:%s, header: %s",
					response.statusCode().value(), response.bodyToMono(String.class),
					response.headers().asHttpHeaders())
			)))
			.bodyToMono(WeatherResponse.class)
			.log();
	}
}
