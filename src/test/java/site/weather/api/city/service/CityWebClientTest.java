package site.weather.api.city.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import site.weather.api.city.domain.City;

@SpringBootTest
class CityWebClientTest {
	private MockWebServer mockWebServer;
	private CityWebClient client;

	@Value("${weather.appid}")
	private String appid;

	@Autowired
	private ObjectMapper objectMapper;

	private static String getOpenWeatherResponseJson(String path) {
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
		this.client = new CityWebClient(WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.codecs(configurer -> {
				configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
				configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
			})
			.build(), appid, objectMapper);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@DisplayName("도시 이름 키워드가 주어질때 도시를 조회하고 도시 정보들을 응답받는다")
	@Test
	void givenCityKeyword_whenFetchCity_thenReturnOfCityResponse() {
		// given
		MockResponse mockResponse = new MockResponse()
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.setResponseCode(200)
			.setBody(getOpenWeatherResponseJson("city.json"));
		mockWebServer.enqueue(mockResponse);
		String city = "서울";
		// when
		Mono<List<City>> source = client.fetchCityBy(city);
		// then
		List<City> expected = List.of(
			new City("KR", "", "Seoul", 37.5666791, 126.9782914),
			new City("MX", "Chiapas", "Seoul", 16.1482357, -93.1950796)
		);
		StepVerifier.create(source)
			.expectNext(expected)
			.verifyComplete();
	}
}
