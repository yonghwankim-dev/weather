package site.weather.api.weather.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WeatherWebClientTest {

	@Autowired
	private WeatherWebClient client;

	@DisplayName("서울의 날씨 정보를 조회한다")
	@Test
	void givenCityName_whenFetchWeatherByCity_thenReturnWeatherOfSeoul() {
		// given
		String city = "Seoul";
		// when
		String json = client.fetchWeatherByCity(city);
		// then
		Assertions.assertThat(json).isNotNull();
	}
}
