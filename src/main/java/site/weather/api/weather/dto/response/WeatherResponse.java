package site.weather.api.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class WeatherResponse {
	@JsonProperty("name")
	private final String name;

	@JsonCreator
	public WeatherResponse(@JsonProperty("name") String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("(name=%s", name);
	}
}
