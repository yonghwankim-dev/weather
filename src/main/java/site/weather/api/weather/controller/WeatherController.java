package site.weather.api.weather.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;
import site.weather.api.weather.domain.WeatherSubscriptionInfo;
import site.weather.api.weather.repository.WeatherSubscriptionInfoRepository;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

	private final WeatherService service;

	private final SimpMessagingTemplate messagingTemplate;

	// 도시별 구독 정보
	private final Map<String, WeatherSubscriptionInfo> weatherSubscriptionInfoMap = new ConcurrentHashMap<>();
	private final WeatherSubscriptionInfoRepository repository;

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		WeatherSubscriptionInfo weatherSubscriptionInfo = repository.subscribeWeather(city);
		weatherSubscriptionInfo.sendOrFetchWeather(messagingTemplate, city, this::fetchWeatherByCity);
	}

	private void fetchWeatherByCity(String city) {
		service.fetchWeatherByCity(city)
			.publishOn(Schedulers.boundedElastic())
			.log()
			.subscribe(response -> {
				repository.changeWeatherResponse(city, response);
				messagingTemplate.convertAndSend("/topic/weather/" + city, response);
			});
	}

	@Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
	public void fetchAndBroadcastByCity() {
		repository.findAllCities().forEach(this::fetchWeatherByCity);
	}
}
