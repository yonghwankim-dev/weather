package site.weather.api.weather.error.exception;

public class WeatherException extends RuntimeException {
	private final String message;

	public WeatherException(String message) {
		super(message);
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("(message=%s)", message);
	}
}
