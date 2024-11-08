package site.weather.api.city.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class City {
	@JsonProperty("country")
	private final String country;
	@JsonProperty("state")
	private String state;
	@JsonProperty("name")
	private final String name;

	public City(String country, String state, String name) {
		this.country = country;
		this.state = state;
		this.name = name;
	}
}
