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
package com.heath_bar.tvdb.xml.handlers;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.types.WebImage;

public class BannerHandler extends DefaultHandler{
	private StringBuilder sb;
	private ArrayList<WebImage> imageList;
	private WebImage currentImage;
    
    @Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
	    name = name.trim().toLowerCase();				// format the current element name
	    sb = new StringBuilder();						// Reset the string builder
	    
	    if (name.equals("banners"))
	    	imageList = new ArrayList<WebImage>();
    }
    
    // SAX parsers may return all contiguous character data in a single chunk, or they may split it into several chunks
    // Therefore we must aggregate the data here, and set it in endElement() function
	@Override
	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));
		sb.append(chars);
	}


    @Override
	public void endElement(String uri, String name, String qName) throws SAXException {
		try {
			name = name.trim().toLowerCase();
			
			if (name.equals("id")){
				currentImage = new WebImage();
				currentImage.setId(sb.toString());
			} else if (name.equals("bannerpath")){
				currentImage.setUrl(AppSettings.SERIES_BANNER_URL + sb.toString());
			} else if (name.equals("thumbnailpath")){
				WebImage thumb = new WebImage();
				thumb.setUrl(AppSettings.SERIES_BANNER_URL + sb.toString());
				currentImage.setThumbnail(thumb);
			} else if (name.equals("banner")){
				imageList.add(currentImage);
			}
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.EpisodeHandler", e.toString());
		}
	}
    
	public ArrayList<WebImage> getImageList(String seriesId) {
	    try {
			URL url = new URL(AppSettings.SERIES_FULL_URL + seriesId + "/banners.xml");	
						
		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
		    
		    return imageList;
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.EpisodeHandler", e.toString());
			return new ArrayList<WebImage>();
		}
	}
	
	public String[] getThumbList(String seriesId) {
	    try {
	    	ArrayList<WebImage> imageList = getImageList(seriesId);
	    	
	    	String[] urls = new String[imageList.size()];
	    	for (int i=0; i<imageList.size(); i++) {
	    		if (imageList.get(i).getThumbnail() != null)
	    			urls[i] = imageList.get(i).getThumbnail().getUrl();
	    		else
	    			urls[i] = imageList.get(i).getUrl();
	    	}
	    			    
		    return urls;
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.EpisodeHandler", e.toString());
			return new String[0];
		}
	}
}


