package site.weather.api.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class WeatherResponse {
	@JsonProperty("name")
	private final String name;
	@JsonProperty("temperature")
	private final double temperature;

	public WeatherResponse(String name, double temperature) {
		this.name = name;
		this.temperature = temperature;
	}

	@Override
	public String toString() {
		return String.format("(name=%s, temperature=%.2f", name, temperature);
	}
}
