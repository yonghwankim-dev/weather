package site.weather.api.weather.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

	private final WeatherService service;

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		service.subscribeWeatherByCity(city);
	}

	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
	public void fetchAndBroadcastByCity() {
		service.findAllSubscribedCities().forEach(service::subscribeWeatherByCity);
	}
}
