package site.weather.api.weather.service;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.error.dto.WeatherErrorResponse;
import site.weather.api.weather.error.exception.BadWebClientRequestException;
import site.weather.api.weather.repository.WeatherSubscriptionInfoRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

	private static final String TOPIC_PREFIX = "/topic/weather/";
	private final WeatherWebClient client;
	private final WeatherSubscriptionInfoRepository repository;
	private final SimpMessagingTemplate messagingTemplate;

	public Mono<WeatherResponse> fetchWeatherByCity(String city) {
		return client.fetchWeatherByCity(city);
	}

	public void subscribeWeatherByCity(String city) {
		client.fetchWeatherByCity(city)
			.publishOn(Schedulers.boundedElastic())
			.doOnError(this::isNotFoundCityResponse, throwable -> repository.removeCity(city))
			.subscribe(response -> messagingTemplate.convertAndSend(destination(city), response),
				throwable -> messagingTemplate.convertAndSend(destination(city), WeatherErrorResponse.from(throwable)));
	}

	private boolean isNotFoundCityResponse(Throwable throwable) {
		return throwable instanceof BadWebClientRequestException exception
			&& exception.hasStatusCode(HttpStatus.NOT_FOUND);
	}

	private String destination(String city) {
		return TOPIC_PREFIX + city;
	}

	public Set<String> findAllCities() {
		return repository.findAllCities();
	}

	public void addSubscription(String city, String sessionId) {
		repository.addSubscription(city, sessionId);
	}

	public void removeCityIfNoSubscribers(String sessionId) {
		repository.removeCityIfNoSubscribers(sessionId);
	}
}
