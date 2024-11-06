package site.weather.api.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class WeatherResponse {
	@JsonProperty("name")
	private final String name;
	@JsonProperty("temperature")
	private final double temperature;
	@JsonProperty("status")
	private final String status;

	private WeatherResponse(String name, double temperature, String status) {
		this.name = name;
		this.temperature = temperature;
		this.status = status;
	}

	public static WeatherResponse ok(String name, double temperature) {
		return new WeatherResponse(name, temperature, "OK");
	}

	public static WeatherResponse error(String name) {
		return new WeatherResponse(name, 0.0, "NA");
	}

	@Override
	public String toString() {
		return String.format("(name=%s, temperature=%.2f", name, temperature);
	}
}
