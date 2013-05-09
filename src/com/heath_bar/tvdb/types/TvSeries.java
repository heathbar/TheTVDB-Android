/*
│──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────│
│                                                  TERMS OF USE: MIT License                                                   │
│                                                  Copyright © 2012 Heath Paddock                                              │
├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation    │ 
│files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,    │
│modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software│
│is furnished to do so, subject to the following conditions:                                                                   │
│                                                                                                                              │
│The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.│
│                                                                                                                              │
│THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE          │
│WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR         │
│COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,   │
│ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                         │
├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 */
package com.heath_bar.tvdb.types;

import com.heath_bar.tvdb.data.adapters.lazylist.WebImage;


public class TvSeries {

	private Long id;
	private String name;
	private WebImage banner;
	private String overview;
	private String firstAired;
	private String language;
	private WebImage poster;
	private String actors;
	private String airDay;
	private String airTime;
	private String network;
	private String rating;
	private String status;
	private String genre;
	private String runtime;
	private String IMDB;
	
	
	public String getIMDB() {
		return IMDB;
	}
	public void setIMDB(String iMDB) {
		IMDB = iMDB;
	}
	public String getRuntime() {
		return runtime;
	}
	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
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
	public WebImage getPoster() {
		if (poster == null)
			poster = new WebImage();
		return poster;
	}
	public void setPoster(WebImage poster) {
		this.poster = poster;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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
	public void setActors(String actorList) {
		actors = actorList;
	}
	public String getActors(){
		return actors;
	}
	public WebImage getBanner() {
		if (banner == null)
			banner = new WebImage();
		return banner;
	}
	public void setBanner(WebImage banner) {
		this.banner = banner;
	}

}
