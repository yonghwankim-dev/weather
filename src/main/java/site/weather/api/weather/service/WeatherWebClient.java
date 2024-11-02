package site.weather.api.weather.service;

import org.springframework.web.reactive.function.client.WebClient;

public class WeatherWebClient {
	private final WebClient webClient;
	private final String appid;

	public WeatherWebClient(WebClient webClient, String appid) {
		this.webClient = webClient;
		this.appid = appid;
	}

	public String fetchWeatherByCity(String city){
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
