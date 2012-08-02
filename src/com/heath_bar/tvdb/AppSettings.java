package com.heath_bar.tvdb;


public final class AppSettings {

	public static final String API_KEY = "0A41C0DEA5531762";
	public static final String BASE_URL =  "http://thetvdb.com/api/" + API_KEY + "/";
	public static final String SERIES_BASIC_URL = "http://www.thetvdb.com/api/GetSeries.php?seriesname=";
	public static final String SERIES_FULL_URL = "http://thetvdb.com/api/0A41C0DEA5531762/series/"; // <seriesid>/all/en.xml
	public static final String EPISODE_FULL_URL = "http://thetvdb.com/api/0A41C0DEA5531762/episodes/"; // <seriesid>/all/en.xml
	public static final String SERIES_BANNER_URL = "http://thetvdb.com/banners/";
	public static final boolean LOG_ENABLED = true;
	public static final int DATABASE_VERSION = 1;
	public static final String LANGUAGE = "en";
	public static final int[] listBackgroundColors = new int[]{0xff002337, 0xff001d2d};	// R.color.blue1 & R.color.blue2
	
}
