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
	@JsonProperty("lat")
	private final double lat;
	@JsonProperty("len")
	private final double lon;

	public City(String country, String state, String name, double lat, double lon) {
		this.country = country;
		this.state = state;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}
}
