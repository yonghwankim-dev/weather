package site.weather.api.weather.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.weather.api.weather.dto.response.WeatherResponse;

@Service
@RequiredArgsConstructor
public class WeatherService {

	private final WeatherWebClient client;

	public WeatherResponse fetchWeatherByCity(String city) {
		return client.fetchWeatherByCity(city).block();
	}
}
