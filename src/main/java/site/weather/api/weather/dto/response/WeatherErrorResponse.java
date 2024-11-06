package site.weather.api.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WeatherErrorResponse {
	@JsonProperty("error")
	private final String error;

	public WeatherErrorResponse(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return String.format("(error=%s)", error);
	}
}
