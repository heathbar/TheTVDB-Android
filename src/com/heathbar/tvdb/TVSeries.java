package com.heathbar.tvdb;

public class TVSeries {

	private String name;
	private int[] seasons;
	private String description;
	private String network;
	private String airDayOfWeek;
	private String airTime;
	

	public int[] getSeasons() {
		return seasons;
	}

	public void setSeasons(int[] seasons) {
		this.seasons = seasons;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getAirDayOfWeek() {
		return airDayOfWeek;
	}

	public void setAirDayOfWeek(String airDayOfWeek) {
		this.airDayOfWeek = airDayOfWeek;
	}

	public String getAirTime() {
		return airTime;
	}

	public void setAirTime(String airTime) {
		this.airTime = airTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
