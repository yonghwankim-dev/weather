package site.weather.api.city.config;

import java.io.IOException;

import org.apache.logging.log4j.util.Strings;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import site.weather.api.city.domain.City;

public class CityDeserializer extends JsonDeserializer<City> {

	@Override
	public City deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
		JsonNode jsonNode = p.getCodec().readTree(p);
		String country = jsonNode.get("country").asText();
		JsonNode stateNode = jsonNode.get("state");
		String state = stateNode == null ? Strings.EMPTY : stateNode.asText(Strings.EMPTY);
		String name = jsonNode.get("name").asText();
		return new City(country, state, name);
	}
}
