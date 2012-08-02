package com.heath_bar.tvdb.types;

import android.graphics.Bitmap;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.util.ImageGetter;

public class WebImage {

	private String url;
	private Bitmap b;
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Bitmap getBitmap() {
		return b;
	}
		
	public Bitmap Load(boolean... force) {
		if (url != null && !url.equals(""))
    		if (b == null || force[0] == true)
    			b = ImageGetter.LoadImage(AppSettings.SERIES_BANNER_URL + url);
		
		return b;
	}	
}
