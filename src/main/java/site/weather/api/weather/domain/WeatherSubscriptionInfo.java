package site.weather.api.weather.domain;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeatherSubscriptionInfo {
	private final String city;
	private final Set<String> sessionIds;

	private WeatherSubscriptionInfo(String city, Set<String> sessionIds) {
		this.city = city;
		this.sessionIds = sessionIds;
	}

	public static WeatherSubscriptionInfo empty(String city) {
		return new WeatherSubscriptionInfo(city, ConcurrentHashMap.newKeySet());
	}

	public void addSessionId(String sessionId) {
		sessionIds.add(sessionId);
		log.info("add sessionId: {}", sessionId);
	}

	public void removeSessionId(String sessionId) {
		sessionIds.remove(sessionId);
	}

	public boolean isEmptySessionIds() {
		return sessionIds.isEmpty();
	}

	@Override
	public String toString() {
		return String.format("city=%s, sessionIds=%s", city, sessionIds);
	}
}
