package site.weather.api.weather.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import site.weather.api.weather.domain.WeatherSubscriptionInfo;
import site.weather.api.weather.dto.response.WeatherResponse;

@Component
@Slf4j
public class MemoryWeatherSubscriptionInfoRepository implements WeatherSubscriptionInfoRepository {

	private final Map<String, WeatherSubscriptionInfo> weatherSubscriptionInfoMap = new ConcurrentHashMap<>();

	@Override
	public WeatherSubscriptionInfo computeIfAbsent(String city) {
		return weatherSubscriptionInfoMap.computeIfAbsent(city, key -> new WeatherSubscriptionInfo());
	}

	@Override
	public void addSessionId(String city, String sessionId) {
		// 도시가 없는 경우 새로운 객체 추가한 다음에 sessionId 추가
		weatherSubscriptionInfoMap.computeIfAbsent(city, k -> new WeatherSubscriptionInfo())
			.addSessionId(sessionId);
		log.info("add city:{}, sessionId: {}", city, sessionId);
	}

	@Override
	public void removeCityIfNoSubscribers(String sessionId) {
		List<String> citiesToRemove = new ArrayList<>();

		// 도시별로 sessionId를 제거하고 제거한 Set이 비어있으면 삭제 목록에 추가
		weatherSubscriptionInfoMap.forEach((city, weatherSubscriptionInfo) -> {
			weatherSubscriptionInfo.removeSessionId(sessionId);
			if (weatherSubscriptionInfo.isEmptySessionIds()) {
				citiesToRemove.add(city);
			}
		});

		// 삭제할 도시 목록에서 제거 작업 수행
		citiesToRemove.forEach(city -> {
			weatherSubscriptionInfoMap.remove(city);
			log.info("remove the city " + city);
		});
	}

	@Override
	public void changeWeatherResponse(String city, WeatherResponse response) {
		weatherSubscriptionInfoMap.computeIfPresent(city,
			(key, subscriptionInfo) -> {
				subscriptionInfo.changeWeatherResponse(response);
				return subscriptionInfo;
			});
	}

	@Override
	public Set<String> findAllSubscribedCities() {
		return weatherSubscriptionInfoMap.keySet();
	}
}
