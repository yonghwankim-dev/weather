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
import site.weather.api.city.service.CityWebClient;

@RestController
@RequestMapping("/api/city")
@RequiredArgsConstructor
public class CityRestController {

	private final CityWebClient client;

	@GetMapping
	public Mono<ResponseEntity<List<City>>> fetchCity(@RequestParam String search) {
		return client.fetchCityBy(search)
			.map(ResponseEntity::ok);
	}
}
