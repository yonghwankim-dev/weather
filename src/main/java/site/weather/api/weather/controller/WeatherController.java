package site.weather.api.weather.controller;

import java.util.HashSet;
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
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.service.WeatherService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

	private final WeatherService service;

	private final SimpMessagingTemplate messagingTemplate;

	private final Set<String> subscribers = ConcurrentHashMap.newKeySet();
	// 도시별 세션 ID 컬렉션
	private final Map<String, Set<String>> subscribersSessionId = new ConcurrentHashMap<>();
	private final Map<String, WeatherResponse> cachedWeatherResponses = new ConcurrentHashMap<>();

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		subscribers.add(city);

		if (cachedWeatherResponses.containsKey(city)) {
			messagingTemplate.convertAndSend("/topic/weather/" + city, cachedWeatherResponses.get(city));
		} else {
			fetchWeatherByCity(city);
		}
	}

	private void fetchWeatherByCity(String city) {
		service.fetchWeatherByCity(city)
			.publishOn(Schedulers.boundedElastic())
			.log()
			.subscribe(response -> {
				cachedWeatherResponses.put(city, response);
				messagingTemplate.convertAndSend("/topic/weather/" + city, response);
			});
	}

	@Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
	public void fetchAndBroadcastByCity() {
		subscribers.forEach(this::fetchWeatherByCity);
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
		Set<String> delCity = new HashSet<>();
		// 도시별로 sessionId를 제거하고 제거한 Set이 비어있으면 삭제 집합에 추가한다
		subscribersSessionId.keySet().forEach(city -> {
			Set<String> sessionIdSet = subscribersSessionId.getOrDefault(city, new HashSet<>());
			sessionIdSet.remove(sessionId);
			if (sessionIdSet.isEmpty()) {
				delCity.add(city);
			}
		});
		delCity.forEach(city -> {
			subscribersSessionId.remove(city);
			subscribers.remove(city);
			log.info("remove the city " + city);
		});
	}
}
