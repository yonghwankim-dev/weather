package site.weather.api.weather.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import site.weather.api.weather.domain.Units;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.error.exception.BadWebClientRequestException;
import site.weather.api.weather.error.exception.WebClientResponseException;

@Slf4j
public class WeatherWebClient {
	private final WebClient webClient;
	private final String appid;
	private final ObjectMapper objectMapper;

	public WeatherWebClient(WebClient webClient, String appid, ObjectMapper objectMapper) {
		this.webClient = webClient;
		this.appid = appid;
		this.objectMapper = objectMapper;
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
			.onStatus(HttpStatusCode::is4xxClientError, response ->
				response.bodyToMono(String.class)
					.flatMap(errorBody -> {
						log.info("errorBody: {}", errorBody);
						return Mono.error(new BadWebClientRequestException(
							response.statusCode().value(),
							parseMessage(errorBody),
							String.format(
								"An external request error with a 4xx status code. statusCode: %s, response:%s, header: %s",
								response.statusCode().value(),
								response.bodyToMono(String.class),
								response.headers().asHttpHeaders()
							)
						));
					}))
			.onStatus(HttpStatusCode::is5xxServerError, response ->
				response.bodyToMono(String.class)
					.flatMap(errorBody -> {
						log.info("errorBody: {}", errorBody);
						return Mono.error(new WebClientResponseException(
							response.statusCode().value(),
							parseMessage(errorBody),
							String.format("5xx external system error. statusCode: %s, response: %s, header: %s",
								response.statusCode().value(),
								response.bodyToMono(String.class),
								response.headers().asHttpHeaders())
						));
					}))
			.bodyToMono(WeatherResponse.class)
			.log();
	}

	private String parseMessage(String body) {
		String rawMessage;
		try {
			JsonNode jsonNode = objectMapper.readTree(body);
			rawMessage = jsonNode.path("message").asText("Unknown Error");
		} catch (JsonProcessingException e) {
			rawMessage = "Error parsing response message";
		}
		return rawMessage;
	}
}
