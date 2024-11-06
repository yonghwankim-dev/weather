package site.weather.api.weather.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import site.weather.api.weather.domain.WeatherSubscriptionInfo;

@Component
@Slf4j
public class MemoryWeatherSubscriptionInfoRepository implements WeatherSubscriptionInfoRepository {

	private final Map<String, Set<WeatherSubscriptionInfo>> store = new ConcurrentHashMap<>();

	@Override
	public void addSubscription(String city, String sessionId) {
		store.computeIfAbsent(city, key -> ConcurrentHashMap.newKeySet())
			.add(WeatherSubscriptionInfo.create(city, sessionId));
	}

	@Override
	public void removeCityIfNoSubscribers(String sessionId) {
		List<String> citiesToRemove = new ArrayList<>();

		// 도시별로 sessionId인 구독 정보를 제거하고 제거한 Set이 비어있으면 삭제 목록에 추가
		store.forEach((city, set) -> {
			set.remove(WeatherSubscriptionInfo.create(city, sessionId));
			if (set.isEmpty()) {
				citiesToRemove.add(city);
			}
		});

		// 삭제할 도시 목록에서 제거 작업 수행
		citiesToRemove.forEach(city -> {
			store.remove(city);
			log.info("remove the city " + city);
		});
	}

	@Override
	public Set<String> findAllCities() {
		return store.keySet();
	}
}
