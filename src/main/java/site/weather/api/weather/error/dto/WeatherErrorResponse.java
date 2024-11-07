package site.weather.api.weather.error.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import site.weather.api.weather.error.exception.BadWebClientRequestException;
import site.weather.api.weather.error.exception.WebClientResponseException;

public class WeatherErrorResponse {
	@JsonProperty("httpStatusCode")
	private final int httpStatusCode;

	@JsonProperty("error")
	private final String error;

	public WeatherErrorResponse(int httpStatusCode, String error) {
		this.httpStatusCode = httpStatusCode;
		this.error = error;
	}

	public static WeatherErrorResponse from(Throwable throwable) {
		if (throwable instanceof BadWebClientRequestException requestException) {
			return requestException.toErrorResponse();
		} else if (throwable instanceof WebClientResponseException responseException) {
			return responseException.toErrorResponse();
		}
		return new WeatherErrorResponse(0, throwable.getMessage());
	}

	@Override
	public String toString() {
		return String.format("(httpStatusCode=%s, error=%s)", httpStatusCode, error);
	}
}
