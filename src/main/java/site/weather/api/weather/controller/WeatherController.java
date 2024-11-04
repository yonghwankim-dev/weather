package site.weather.api.weather.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

	// 도시별 구독 정보
	private final Map<String, WeatherSubscriptionInfo> weatherSubscriptionInfoMap = new ConcurrentHashMap<>();

	@MessageMapping("/weather")
	public void subscribeWeather(String city) {
		weatherSubscriptionInfoMap.computeIfAbsent(city, key -> new WeatherSubscriptionInfo())
			.sendOrFetchWeather(messagingTemplate, city, this::fetchWeatherByCity);
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
		weatherSubscriptionInfoMap.keySet().forEach(this::fetchWeatherByCity);
	}

	@EventListener
	public void handleStompConnectedHandler(SessionSubscribeEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = headerAccessor.getDestination();

		parseCityFrom(destination).ifPresent(city -> {
			String sessionId = headerAccessor.getSessionId();
			// subscribersSessionId에 도시가 없는 경우 새로운 HashSet을 추가
			weatherSubscriptionInfoMap.computeIfAbsent(city, k -> new WeatherSubscriptionInfo())
				.addSessionId(sessionId);
		});
		log.info("weatherSubscriptionInfoMap: {}", weatherSubscriptionInfoMap);
	}

	private Optional<String> parseCityFrom(String destination) {
		if (destination == null) {
			return Optional.empty();
		}
		final String DESTINATION_SPLITTER = "/";
		String[] parts = destination.split(DESTINATION_SPLITTER);
		return Optional.of(parts[parts.length - 1]);
	}

	@EventListener
	public void handleStompDisconnectedHandler(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
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
}
