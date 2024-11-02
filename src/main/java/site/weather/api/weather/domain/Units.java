package site.weather.api.weather.domain;

public enum Units {
	STANDARD,
	METRIC,
	IMPERIAL;

	@Override
	public String toString() {
		return this.name();
	}
}
