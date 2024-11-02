package site.weather.api.weather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeatherService {

	private final String appid;

	public WeatherService(@Value("${weather.appid}") String appid) {
		this.appid = appid;
	}

	public String fetchWeatherByCity(String city) {
		WebClient webClient = WebClient.builder()
			.baseUrl("https://api.openweathermap.org")
			.build();
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/data/2.5/weather")
				.queryParam("q", city)
				.queryParam("appid", appid)
				.queryParam("units", "metric")
				.build())
			.retrieve()
			.bodyToMono(String.class)
			.block();
	}
}
