package site.weather.api.weather.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import site.weather.api.weather.error.dto.WeatherErrorResponse;

public class WeatherException extends RuntimeException {
	private final HttpStatusCode httpStatusCode;
	private final String message;

	public WeatherException(HttpStatusCode httpStatusCode, String message) {
		super(message);
		this.httpStatusCode = httpStatusCode;
		this.message = message;
	}

	public boolean is404Error() {
		return this.httpStatusCode == HttpStatus.NOT_FOUND;
	}

	public WeatherErrorResponse toResponse() {
		return new WeatherErrorResponse(httpStatusCode, message);
	}

	@Override
	public String toString() {
		return String.format("(httpStatusCode=%s, message=%s)", httpStatusCode, message);
	}
}
