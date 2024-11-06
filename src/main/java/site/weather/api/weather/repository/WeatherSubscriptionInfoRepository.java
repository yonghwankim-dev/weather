package site.weather.api.weather.repository;

import java.util.Set;

public interface WeatherSubscriptionInfoRepository {
	void addSubscription(String city, String sessionId);

	void removeCityIfNoSubscribers(String sessionId);

	Set<String> findAllCities();

	void removeCity(String city);
}
