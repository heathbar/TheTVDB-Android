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
	private String IMDB_ID;
	
	public String getGuestStars() {
		return StringUtil.commafy(guestStars);
	}
	public void setGuestStars(String guestStarsList) {
		this.guestStars = guestStarsList;
	}
		
	public String getIMDB() {
		return IMDB_ID;
	}
	public void setIMDB(String iMDB_ID) {
		IMDB_ID = iMDB_ID;
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
