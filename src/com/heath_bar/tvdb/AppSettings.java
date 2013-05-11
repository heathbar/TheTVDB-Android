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
package com.heath_bar.tvdb;


public final class AppSettings {

	public static final String API_KEY = "0A41C0DEA5531762";
	public static final String BASE_URL =  "http://thetvdb.com/api/" + API_KEY + "/";
	public static final String SERIES_BASIC_URL = "http://www.thetvdb.com/api/GetSeries.php?seriesname=";
	public static final String SERIES_FULL_URL = "http://thetvdb.com/api/" + API_KEY + "/series/"; // <seriesid>/all/en.xml
	public static final String EPISODE_FULL_URL = "http://thetvdb.com/api/" + API_KEY + "/episodes/"; // <seriesid>/all/en.xml
	public static final String GET_RATING_URL = "http://thetvdb.com/api/User_Rating.php?";
	public static final String SET_RATING_URL = "http://thetvdb.com/api/GetRatingsForUser.php?apikey=" + API_KEY + "&";
	public static final String FAVORITES_URL = "http://thetvdb.com/api/User_Favorites.php?";
	public static final String BANNER_URL = "http://thetvdb.com/banners/";
	public static final boolean LOG_ENABLED = true;
	public static final int DATABASE_VERSION = 1;
	public static final int[] listBackgroundColors = new int[]{0xff000000, 0xff001d2d};	// R.color.background1 & R.color.background2
	public static final String PREFS_NAME = "TheTVDBSettings";
	public static final int THUMBNAIL_SIZE = 100;
	public static final int DEFAULT_CACHE_SIZE = 50; // 50 MB
	
}
