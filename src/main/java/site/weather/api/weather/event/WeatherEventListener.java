package site.weather.api.weather.event;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.weather.api.weather.service.WeatherService;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherEventListener {

	private final WeatherService service;

	@EventListener
	public void handleStompConnectedHandler(SessionSubscribeEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = headerAccessor.getDestination();

		parseCityFrom(destination).ifPresent(city -> {
			String sessionId = headerAccessor.getSessionId();
			service.addSessionId(city, sessionId);
		});
	}

	private Optional<String> parseCityFrom(String destination) {
		return Optional.ofNullable(destination)
			.map(dst -> dst.substring(dst.lastIndexOf("/") + 1));
	}

	@EventListener
	public void handleStompDisconnectedHandler(SessionDisconnectEvent event) {
		service.removeCityIfNoSubscribers(event.getSessionId());
	}
}
