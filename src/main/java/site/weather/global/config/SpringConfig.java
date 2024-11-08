package site.weather.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.weather.api.city.config.CityModule;
import site.weather.api.weather.config.WeatherModule;

@Configuration
public class SpringConfig {
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
			.registerModule(new WeatherModule())
			.registerModule(new CityModule());
	}
}
