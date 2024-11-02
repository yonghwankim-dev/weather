package site.weather.api.weather.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import site.weather.api.weather.dto.response.WeatherResponse;

@SpringBootTest
class WeatherWebClientTest {

	private MockWebServer mockWebServer;
	private WeatherWebClient client;

	@Value("${weather.appid}")
	private String appid;

	private static String getWeatherJson(String path) {
		ClassPathResource resource = new ClassPathResource(path);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			return br.lines()
				.collect(Collectors.joining());
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@BeforeEach
	void setup() {
		this.mockWebServer = new MockWebServer();
		try {
			mockWebServer.start();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		this.client = new WeatherWebClient(WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build(), appid);
	}

	@DisplayName("서울의 날씨 정보를 조회한다")
	@Test
	void givenCityName_whenFetchWeatherByCity_thenReturnWeatherOfSeoul() {
		// given
		MockResponse mockResponse = new MockResponse()
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.setResponseCode(200)
			.setBody(getWeatherJson("weather.json"));
		mockWebServer.enqueue(mockResponse);

		String city = "Seoul";
		// when
		Mono<WeatherResponse> source = client.fetchWeatherByCity(city);
		// then
		WeatherResponse expected = new WeatherResponse("Seoul");
		StepVerifier.create(source)
			.expectNext(expected)
			.verifyComplete();
	}
}
