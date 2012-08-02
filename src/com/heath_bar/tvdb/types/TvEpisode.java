package com.heath_bar.tvdb.types;

import com.heath_bar.tvdb.util.StringUtil;




public class TvEpisode {

	private int id;
	private String name;
	private int season;
	private int number;
	private long airDate;
	private String director;
	private String writer;
	private String overview;
	private String rating;
	private WebImage image;
	private String guestStars;
	
	public String getGuestStars() {
		return StringUtil.commafy(guestStars);
	}
	public void setGuestStars(String guestStarsList) {
		this.guestStars = guestStarsList;
	}
		
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = StringUtil.commafy(director);
	}
	public String getWriter() {
		return StringUtil.commafy(writer);
	}
	public void setWriter(String writer) {
		this.writer = writer;
	}
	public String getOverview() {
		return overview;
	}
	public void setOverview(String overview) {
		this.overview = overview;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	public WebImage getImage() {
		if (image == null)
			image = new WebImage();
		return image;
	}
	public void setImage(WebImage thumb) {
		this.image = thumb;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}	
	public int getSeason() {
		return season;
	}
	public void setSeason(int season) {
		this.season = season;
	}
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
	public long getAirDate() {
		return airDate;
	}
	public void setAirDate(long airDate) {
		this.airDate = airDate;
	}
	
}
