package site.weather.api.weather.repository;

import java.util.Set;

import site.weather.api.weather.domain.WeatherSubscriptionInfo;
import site.weather.api.weather.dto.response.WeatherResponse;

public interface WeatherSubscriptionInfoRepository {
	WeatherSubscriptionInfo computeIfAbsent(String city);

	void addSessionId(String city, String sessionId);

	void removeCityIfNoSubscribers(String sessionId);

	void changeWeatherResponse(String city, WeatherResponse response);

	Set<String> findAllSubscribedCities();
}
