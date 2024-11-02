package site.weather.api.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherRestController {

	@GetMapping
	public ResponseEntity<String> getWeather() {
		return ResponseEntity.ok("weather");
	}
}
