package site.weather.api.weather.error.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import site.weather.api.weather.error.dto.WeatherErrorResponse;

@Getter
public class BadWebClientRequestException extends RuntimeException {
	private final int statusCode;
	private final String rawMessage;

	public BadWebClientRequestException(int statusCode, String rawMessage, String message) {
		super(message);
		this.statusCode = statusCode;
		this.rawMessage = rawMessage;
	}

	public boolean hasStatusCode(HttpStatus httpStatus) {
		return statusCode == httpStatus.value();
	}

	public WeatherErrorResponse toErrorResponse() {
		return new WeatherErrorResponse(statusCode, rawMessage);
	}

	@Override
	public String toString() {
		return String.format("(statusCode=%s, rawMessage=%s, message=%s)", statusCode, rawMessage, getMessage());
	}
}
