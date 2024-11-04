package site.weather.api.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@Getter
public class WeatherResponse {
	@JsonProperty("name")
	private String name;
	@JsonProperty("temperature")
	private double temperature;

	public WeatherResponse(String name, double temperature) {
		this.name = name;
		this.temperature = temperature;
	}

	@Override
	public String toString() {
		return String.format("(name=%s, temperature=%.2f", name, temperature);
	}
}
