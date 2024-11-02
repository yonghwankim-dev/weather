package site.weather.api.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherRestController {

	private final WeatherService service;

	@GetMapping
	public ResponseEntity<WeatherResponse> getWeather(@RequestParam String city) {
		WeatherResponse response = service.fetchWeatherByCity(city);
		return ResponseEntity.ok(response);
	}
}
