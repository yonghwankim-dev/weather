package site.weather.api.weather.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

	private final WeatherWebClient client;

	public String fetchWeatherByCity(String city) {
		return client.fetchWeatherByCity(city);
	}
}
