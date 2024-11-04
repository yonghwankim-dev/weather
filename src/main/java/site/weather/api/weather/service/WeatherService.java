package site.weather.api.weather.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import site.weather.api.weather.domain.WeatherSubscriptionInfo;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.repository.WeatherSubscriptionInfoRepository;

@Service
@RequiredArgsConstructor
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
			.log()
			.subscribe(response -> {
				repository.changeWeatherResponse(city, response);
				messagingTemplate.convertAndSend("/topic/weather/" + city, response);
			});
	}

	public WeatherSubscriptionInfo computeIfAbsent(String city) {
		return repository.computeIfAbsent(city);
	}
}
