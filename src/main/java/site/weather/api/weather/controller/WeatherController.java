package site.weather.api.weather.controller;

import java.time.Duration;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

	private final WeatherService service;

	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		Flux.interval(Duration.ofSeconds(5))
			.flatMap(tick -> service.fetchWeatherByCity(city))
			.take(Duration.ofSeconds(30))
			.publishOn(Schedulers.boundedElastic())
			.subscribe(response -> messagingTemplate.convertAndSend("/topic/weather", response));
	}
}
