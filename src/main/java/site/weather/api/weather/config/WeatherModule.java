package site.weather.api.weather.config;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import site.weather.api.weather.dto.response.WeatherResponse;

public class WeatherModule extends SimpleModule {

	@Override
	public void setupModule(SetupContext context) {
		SimpleDeserializers simpleDeserializers = new SimpleDeserializers();
		simpleDeserializers.addDeserializer(WeatherResponse.class, new WeatherResponseDeserializer());
		context.addDeserializers(simpleDeserializers);
	}
}
