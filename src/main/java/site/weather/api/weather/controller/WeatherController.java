package site.weather.api.weather.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;
import site.weather.api.weather.domain.WeatherSubscriptionInfo;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

	private final WeatherService service;

	private final SimpMessagingTemplate messagingTemplate;

	// 도시별 세션 ID 컬렉션
	private final Map<String, Set<String>> subscribersSessionId = new ConcurrentHashMap<>();
	private final Map<String, WeatherSubscriptionInfo> weatherSubscriptionInfoMap = new ConcurrentHashMap<>();

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		if (weatherSubscriptionInfoMap.containsKey(city)) {
			weatherSubscriptionInfoMap.get(city).sendMessage(messagingTemplate, city);
		} else {
			fetchWeatherByCity(city);
		}
	}

	private void fetchWeatherByCity(String city) {
		service.fetchWeatherByCity(city)
			.publishOn(Schedulers.boundedElastic())
			.log()
			.subscribe(response -> {
				weatherSubscriptionInfoMap.computeIfPresent(city,
					(key, weatherSubscriptionInfo) -> {
						weatherSubscriptionInfo.changeWeatherResponse(response);
						return weatherSubscriptionInfo;
					});
				messagingTemplate.convertAndSend("/topic/weather/" + city, response);
			});
	}

	@Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
	public void fetchAndBroadcastByCity() {
		subscribersSessionId.keySet().forEach(this::fetchWeatherByCity);
	}

	@EventListener
	public void handleStompConnectedHandler(SessionSubscribeEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = headerAccessor.getSessionId();
		String destination = headerAccessor.getDestination();
		if (destination == null) {
			return;
		}
		String[] parts = destination.split("/");
		String city = parts[parts.length - 1];
		// subscribersSessionId에 도시가 없는 경우 새로운 HashSet을 추가
		subscribersSessionId.computeIfAbsent(city, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
		log.info("subscribersSessionId: {}", subscribersSessionId);
	}

	@EventListener
	public void handleStompDisconnectedHandler(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		List<String> citiesToRemove = new ArrayList<>();

		// 도시별로 sessionId를 제거하고 제거한 Set이 비어있으면 삭제 목록에 추가
		subscribersSessionId.forEach((city, sessionIdSet) -> {
			sessionIdSet.remove(sessionId);
			if (sessionIdSet.isEmpty()) {
				citiesToRemove.add(city);
			}
		});

		// 삭제할 도시 목록에서 제거 작업 수행
		citiesToRemove.forEach(city -> {
			subscribersSessionId.remove(city);
			log.info("remove the city " + city);
		});
	}
}
