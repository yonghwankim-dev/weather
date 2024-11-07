package site.weather.api.weather.error.exception;

import site.weather.api.weather.error.dto.WeatherErrorResponse;

public class WebClientResponseException extends RuntimeException {
	private final int statusCode;
	private final String rawMessage;

	public WebClientResponseException(int statusCode, String rawMessage, String message) {
		super(message);
		this.statusCode = statusCode;
		this.rawMessage = rawMessage;
	}

	public WeatherErrorResponse toErrorResponse() {
		return new WeatherErrorResponse(statusCode, rawMessage);
	}

	@Override
	public String toString() {
		return String.format("(statusCode=%s, rawMessage=%s, message=%s)", statusCode, rawMessage, getMessage());
	}
}
