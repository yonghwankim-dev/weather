package site.weather.api.weather.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
public class WeatherController {

	private final WeatherService service;

	@MessageMapping("/weather")
	@SendTo("/topic/weather")
	public Mono<WeatherResponse> subscribeWeather(String city) {
		return service.fetchWeatherByCity(city);
	}
}
