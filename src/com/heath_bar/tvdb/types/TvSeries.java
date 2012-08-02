package com.heath_bar.tvdb.types;

import android.content.Context;

import com.heath_bar.tvdb.adapters.SeriesDbAdapter;

public class TvSeries {

	private String id;
	private String name;
	private WebImage image;
	private String overview;
	private String firstAired;
	private String imdb;
	private String language;
	private String poster;
	private String actors;
	private String airDay;
	private String airTime;
	private String network;
	private String rating;
	
	
	public String getAirDay() {
		return airDay;
	}
	public void setAirDay(String airDay) {
		this.airDay = airDay;
	}
	public String getAirTime() {
		return airTime;
	}
	public void setAirTime(String airTime) {
		this.airTime = airTime;
	}
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	public String getPoster() {
		return poster;
	}
	public void setPoster(String poster) {
		this.poster = poster;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getOverview() {
		return overview;
	}
	public void setOverview(String overview) {
		this.overview = overview;
	}
	public String getFirstAired() {
		return firstAired;
	}
	public void setFirstAired(String firstAired) {
		this.firstAired = firstAired;
	}
	public String getImdb() {
		return imdb;
	}
	public void setImdb(String imdb) {
		this.imdb = imdb;
	}
	public boolean isFavorite(Context ctx) {
		SeriesDbAdapter db = new SeriesDbAdapter(ctx);
		db.open();
		boolean isFav = db.isFavoriteSeries(id);
		db.close();
		return isFav;
	}
	public void setActors(String actorList) {
		actors = actorList;
	}
	public String getActors(){
		return actors;
	}
	public WebImage getImage() {
		if (image == null)
			image = new WebImage();
		return image;
	}
	public void setImage(WebImage banner) {
		this.image = banner;
	}

}
