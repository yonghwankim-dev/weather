package site.weather.api.city.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import site.weather.api.city.domain.City;

@Service
@RequiredArgsConstructor
public class CityService {
	private final CityWebClient client;

	public Mono<List<City>> fetchCityBy(String city) {
		return client.fetchCityBy(city);
	}
}
