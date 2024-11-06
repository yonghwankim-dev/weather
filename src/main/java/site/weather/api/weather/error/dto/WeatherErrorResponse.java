package site.weather.api.weather.error.dto;

import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.annotation.JsonProperty;

import site.weather.api.weather.error.exception.WeatherException;

public class WeatherErrorResponse {
	@JsonProperty("httpStatusCode")
	private final HttpStatusCode httpStatusCode;

	@JsonProperty("error")
	private final String error;

	public WeatherErrorResponse(HttpStatusCode httpStatusCode, String error) {
		this.httpStatusCode = httpStatusCode;
		this.error = error;
	}

	public static WeatherErrorResponse from(Throwable throwable) {
		if (throwable instanceof WeatherException exception) {
			return exception.toResponse();
		}
		return new WeatherErrorResponse(null, throwable.getMessage());
	}

	@Override
	public String toString() {
		return String.format("(httpStatusCode=%s, error=%s)", httpStatusCode, error);
	}
}
