package site.weather.api.weather.repository;

import java.util.Set;

public interface WeatherSubscriptionInfoRepository {
	void addSessionId(String city, String sessionId);

	void removeCityIfNoSubscribers(String sessionId);

	Set<String> findAllSubscribedCities();
}
