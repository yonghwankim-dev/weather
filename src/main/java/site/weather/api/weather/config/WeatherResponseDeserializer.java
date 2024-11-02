package site.weather.api.weather.config;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import site.weather.api.weather.dto.response.WeatherResponse;

public class WeatherResponseDeserializer extends JsonDeserializer<WeatherResponse> {

	@Override
	public WeatherResponse deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
		JsonNode jsonNode = p.getCodec().readTree(p);
		String name = jsonNode.get("name").asText();
		double temperature = jsonNode.path("main").get("temp").asDouble();
		return new WeatherResponse(name, temperature);
	}
}
