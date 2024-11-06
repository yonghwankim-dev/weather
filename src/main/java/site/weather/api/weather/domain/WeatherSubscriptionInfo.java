package site.weather.api.weather.domain;

import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeatherSubscriptionInfo {
	private final Set<String> sessionIds;

	public WeatherSubscriptionInfo() {
		this(new HashSet<>());
	}

	public WeatherSubscriptionInfo(Set<String> sessionIds) {
		this.sessionIds = sessionIds;
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
}
