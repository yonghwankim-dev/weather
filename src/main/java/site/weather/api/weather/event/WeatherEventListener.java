package site.weather.api.weather.event;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.weather.api.weather.repository.WeatherSubscriptionInfoRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherEventListener {

	private final WeatherSubscriptionInfoRepository repository;

	@EventListener
	public void handleStompConnectedHandler(SessionSubscribeEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = headerAccessor.getDestination();

		parseCityFrom(destination).ifPresent(city -> {
			String sessionId = headerAccessor.getSessionId();
			repository.addSessionId(city, sessionId);
		});
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
		repository.removeCityIfNoSubscribers(event.getSessionId());
	}
}
