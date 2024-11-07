package site.weather.api.weather.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import site.weather.api.weather.dto.response.WeatherResponse;
import site.weather.api.weather.error.exception.BadWebClientRequestException;
import site.weather.api.weather.error.exception.WebClientResponseException;
import site.weather.api.weather.repository.WeatherSubscriptionInfoRepository;

@SpringBootTest
class WeatherWebClientTest {

	private MockWebServer mockWebServer;
	private WeatherWebClient client;

	@Value("${weather.appid}")
	private String appid;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WeatherSubscriptionInfoRepository repository;

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
		this.client = new WeatherWebClient(WebClient.builder()
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

	@DisplayName("서울의 날씨 정보를 조회한다")
	@Test
	void givenCityName_whenFetchWeatherByCity_thenReturnWeatherOfSeoul() {
		// given
		MockResponse mockResponse = new MockResponse()
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.setResponseCode(200)
			.setBody(getOpenWeatherResponseJson("weather.json"));
		mockWebServer.enqueue(mockResponse);

		String city = "Seoul";
		// when
		Mono<WeatherResponse> source = client.fetchWeatherByCity(city);
		// then
		WeatherResponse expected = WeatherResponse.ok("Seoul", 24.66);
		StepVerifier.create(source)
			.expectNext(expected)
			.verifyComplete();
	}

	@DisplayName("서울의 날씨 정보를 조회하고 Flux로 받는다")
	@Test
	void givenCityName_whenFetchWeatherByCity_thenReturnFluxWeather() {
		// given
		MockResponse mockResponse = new MockResponse()
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.setResponseCode(200)
			.setBody(getOpenWeatherResponseJson("weather.json"));
		mockWebServer.enqueue(mockResponse);

		String city = "Seoul";
		// when
		Flux<WeatherResponse> source = Flux.just(client.fetchWeatherByCity(city))
			.flatMap(Mono::flux)
			.publishOn(Schedulers.boundedElastic());
		// then
		WeatherResponse expected = WeatherResponse.ok("Seoul", 24.66);
		StepVerifier.create(source)
			.expectNext(expected)
			.verifyComplete();
	}

	@DisplayName("존재하지 않는 도시 이름으로 날씨 조회시 API가 404을 응답하고 클라이언트에게 에러를 응답한다")
	@Test
	void givenNotFoundCityName_whenResponseError_thenReturnErrorMono() {
		// given
		MockResponse mockResponse = new MockResponse()
			.setResponseCode(404)
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.setBody(getOpenWeatherResponseJson("404.json"));

		mockWebServer.enqueue(mockResponse);

		String city = "awoiejfaoiwejf";
		// when
		Mono<WeatherResponse> source = client.fetchWeatherByCity(city);

		// then
		String expected = "city not found";
		StepVerifier.create(source)
			.expectErrorMatches(throwable -> throwable instanceof BadWebClientRequestException exception
				&& exception.getStatusCode() == 404 && exception.getRawMessage().equals(expected))
			.verify();
		Assertions.assertThat(repository.findAllCities()).isEmpty();
	}

	@DisplayName("날씨 조회시 API가 유효하지 않으면 401을 응답하고 클라이언트에게 에러를 응답한다")
	@Test
	void givenCityName_whenResponse401Error_thenReturnErrorMono() {
		// given
		MockResponse mockResponse = new MockResponse().setResponseCode(401)
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.setBody(getOpenWeatherResponseJson("401.json"));
		mockWebServer.enqueue(mockResponse);

		String city = "Seoul";
		// when
		Mono<WeatherResponse> source = client.fetchWeatherByCity(city);
		// then
		String expected = "invalid API key";
		StepVerifier.create(source)
			.expectErrorMatches(throwable -> throwable instanceof BadWebClientRequestException exception
				&& exception.getStatusCode() == 401 && exception.getRawMessage().equals(expected))
			.verify();
	}

	@DisplayName("날씨 조회시 서버로부터 500 응답을 받으면 에러를 응답한다")
	@Test
	void givenCityName_whenResponse500Error_thenReturnErrorMono() {
		// given
		MockResponse mockResponse = new MockResponse().setResponseCode(500)
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.setBody(getOpenWeatherResponseJson("500.json"));
		mockWebServer.enqueue(mockResponse);

		String city = "Seoul";
		// when
		Mono<WeatherResponse> source = client.fetchWeatherByCity(city);
		// then
		String expected = "internal server error";
		StepVerifier.create(source)
			.expectErrorMatches(throwable -> throwable instanceof WebClientResponseException exception
				&& exception.getStatusCode() == 500 && exception.getRawMessage().equals(expected))
			.verify();
	}
}
