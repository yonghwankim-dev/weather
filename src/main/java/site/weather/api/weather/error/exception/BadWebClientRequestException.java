package site.weather.api.weather.error.exception;

import org.springframework.http.HttpStatus;

import site.weather.api.weather.error.dto.WeatherErrorResponse;

public class BadWebClientRequestException extends RuntimeException {
	private final int statusCode;

	public BadWebClientRequestException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public boolean hasStatusCode(HttpStatus httpStatus) {
		return statusCode == httpStatus.value();
	}

	public WeatherErrorResponse toErrorResponse() {
		return new WeatherErrorResponse(statusCode, getMessage());
	}

	@Override
	public String toString() {
		return String.format("(statusCode=%s, message=%s)", statusCode, getMessage());
	}
}
