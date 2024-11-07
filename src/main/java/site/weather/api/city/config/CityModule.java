package site.weather.api.city.config;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import site.weather.api.city.domain.City;

public class CityModule extends SimpleModule {

	@Override
	public void setupModule(SetupContext context) {
		SimpleDeserializers simpleDeserializers = new SimpleDeserializers();
		simpleDeserializers.addDeserializer(City.class, new CityDeserializer());
		context.addDeserializers(simpleDeserializers);
	}
}
