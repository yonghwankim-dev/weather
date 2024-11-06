package site.weather.api.weather.service;

import java.util.Set;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import site.weather.api.weather.dto.response.WeatherErrorResponse;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.repository.WeatherSubscriptionInfoRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

	private final WeatherWebClient client;
	private final WeatherSubscriptionInfoRepository repository;
	private final SimpMessagingTemplate messagingTemplate;

	public Mono<WeatherResponse> fetchWeatherByCity(String city) {
		return client.fetchWeatherByCity(city);
	}

	public void subscribeWeatherByCity(String city) {
		client.fetchWeatherByCity(city)
			.publishOn(Schedulers.boundedElastic())
			.subscribe(response -> messagingTemplate.convertAndSend("/topic/weather/" + city, response), throwable -> {
				WeatherErrorResponse errorResponse = new WeatherErrorResponse(throwable.getMessage());
				messagingTemplate.convertAndSend("/topic/weather/" + city, errorResponse);
			});
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
