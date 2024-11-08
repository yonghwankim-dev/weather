package site.weather.api.city.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import site.weather.api.city.domain.City;
import site.weather.api.city.service.CityService;

@RestController
@RequestMapping("/api/city")
@RequiredArgsConstructor
public class CityRestController {

	private final CityService service;

	@GetMapping
	public Mono<ResponseEntity<List<City>>> fetchCity(@RequestParam String search) {
		return service.fetchCityBy(search)
			.map(ResponseEntity::ok);
	}
}
