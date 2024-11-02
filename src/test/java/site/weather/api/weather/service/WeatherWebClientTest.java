package site.weather.api.weather.service;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class WeatherWebClientTest {

	private MockWebServer mockWebServer;
	private WeatherWebClient client;

	@Value("${weather.appid}")
	private String appid;

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
			.setResponseCode(200)
			.setBody("ok");
		mockWebServer.enqueue(mockResponse);

		String city = "Seoul";
		// when
		String json = client.fetchWeatherByCity(city);
		// then
		assertThat(json).isEqualTo("ok");
	}
}
