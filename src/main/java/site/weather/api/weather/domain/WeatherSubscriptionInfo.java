package site.weather.api.weather.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeatherSubscriptionInfo {
	private final String city;
	private final String sessionId;

	private WeatherSubscriptionInfo(String city, String sessionId) {
		this.city = city;
		this.sessionId = sessionId;
	}

	public static WeatherSubscriptionInfo create(String city, String sessionId) {
		return new WeatherSubscriptionInfo(city, sessionId);
	}

	@Override
	public String toString() {
		return String.format("city=%s, sessionId=%s", city, sessionId);
	}
}
