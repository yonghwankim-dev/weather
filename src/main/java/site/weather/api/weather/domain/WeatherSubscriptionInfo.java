package site.weather.api.weather.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.extern.slf4j.Slf4j;
import site.weather.api.weather.dto.response.WeatherResponse;

@Slf4j
public class WeatherSubscriptionInfo {
	private final Set<String> sessionIds;
	private WeatherResponse weatherResponse;

	public WeatherSubscriptionInfo() {
		this(new HashSet<>());
	}

	public WeatherSubscriptionInfo(Set<String> sessionIds) {
		this(sessionIds, null);
	}

	public WeatherSubscriptionInfo(Set<String> sessionIds, WeatherResponse weatherResponse) {
		this.sessionIds = sessionIds;
		this.weatherResponse = weatherResponse;
	}

	public void sendMessage(SimpMessagingTemplate messagingTemplate, String city) {
		messagingTemplate.convertAndSend("/topic/weather/" + city, weatherResponse);
	}

	public void changeWeatherResponse(WeatherResponse weatherResponse) {
		this.weatherResponse = weatherResponse;
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

	public boolean hasWeatherResponse() {
		return weatherResponse != null;
	}

	public void sendOrFetchWeather(SimpMessagingTemplate messagingTemplate, String city,
		Consumer<String> fetchWeatherByCity) {
		if (hasWeatherResponse()) {
			sendMessage(messagingTemplate, city);
		} else {
			fetchWeatherByCity.accept(city);
		}
	}
}
