package site.weather.api.weather.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.weather.api.weather.repository.WeatherSubscriptionInfoRepository;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

	private final WeatherService service;
	private final SimpMessagingTemplate messagingTemplate;
	private final WeatherSubscriptionInfoRepository repository;

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		repository.computeIfAbsent(city).sendOrFetchWeather(messagingTemplate, city, service::subscribeWeatherByCity);
	}

	@Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
	public void fetchAndBroadcastByCity() {
		repository.findAllCities().forEach(service::subscribeWeatherByCity);
	}
}
