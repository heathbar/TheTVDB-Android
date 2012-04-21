package com.heathbar.tvdb;

import java.util.Date;

public class TvEpisode {

	private String name;
	private int number;
	private String synopsis;
	private Date airDate;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}
	public Date getAirDate() {
		return airDate;
	}
	public void setAirDate(Date airDate) {
		this.airDate = airDate;
	}
	
}
