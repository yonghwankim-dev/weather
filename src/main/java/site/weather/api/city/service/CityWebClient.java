package site.weather.api.city.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import site.weather.api.city.domain.City;
import site.weather.api.weather.error.exception.BadWebClientRequestException;
import site.weather.api.weather.error.exception.WebClientResponseException;

@Slf4j
public class CityWebClient {
	private final WebClient webClient;
	private final String appid;
	private final ObjectMapper objectMapper;

	public CityWebClient(WebClient webClient, String appid, ObjectMapper objectMapper) {
		this.webClient = webClient;
		this.appid = appid;
		this.objectMapper = objectMapper;
	}

	public Mono<List<City>> fetchCityBy(String city) {
		log.info("city: {}", city);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/geo/1.0/direct")
				.queryParam("q", city)
				.queryParam("limit", 5)
				.queryParam("appid", appid)
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
			.bodyToMono(new ParameterizedTypeReference<List<City>>() {
			})
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
