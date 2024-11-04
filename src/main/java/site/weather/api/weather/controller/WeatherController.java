package site.weather.api.weather.controller;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

	private final WeatherService service;

	private final SimpMessagingTemplate messagingTemplate;

	private final Set<String> subscribers = ConcurrentHashMap.newKeySet();

	private WeatherResponse cachedWeatherResponse;

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		subscribers.add(city);

		if (cachedWeatherResponse == null) {
			fetchWeatherByCity(city);
		}
	}

	private void fetchWeatherByCity(String city) {
		service.fetchWeatherByCity(city)
			.publishOn(Schedulers.boundedElastic())
			.log()
			.subscribe(response -> {
				cachedWeatherResponse = response;
				messagingTemplate.convertAndSend("/topic/weather/" + city, response);
			});
	}

	@Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
	public void fetchAndBroadcastByCity() {
		subscribers.forEach(this::fetchWeatherByCity);
	}
}
