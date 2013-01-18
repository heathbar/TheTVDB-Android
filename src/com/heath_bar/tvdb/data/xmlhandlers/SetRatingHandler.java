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
package com.heath_bar.tvdb.data.xmlhandlers;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.types.exceptions.RatingNotFoundException;

public class SetRatingHandler extends DefaultHandler{
		private StringBuilder sb;
	    private boolean result = false;
	    
	    public enum RatingType {EPISODE, SERIES};
	    
	    @Override
		public void startElement(String uri, String name, String qName, Attributes atts) {
		    sb = new StringBuilder();						// Reset the string builder
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

				if (name.trim().toLowerCase().equals("rating"))
					result = true;
			    
			} catch (Exception e) {
				if (AppSettings.LOG_ENABLED)
					Log.e("xml.handlers.RatingAdapter", "Error:" + e.toString());
			}
		}
	    
	    public boolean setSeriesRating(String accountId, String seriesId, int rating) {
	    	return setRating(accountId, RatingType.SERIES, seriesId, null, rating);
	    }
	    
	    public boolean setEpisodeRating(String accountId, String seriesId, String episodeId, int rating) {
	    	return setRating(accountId, RatingType.EPISODE, seriesId, episodeId, rating);
	    }
	    
	    private boolean setRating(String accountId, RatingType type, String seriesId, String episodeId, int rating) {
		    try {
		    	
	    		String itemId = "";
		    	String sType = "";
		    	if (type == RatingType.EPISODE){
		    		sType = "episode";
		    		itemId = episodeId;
		    	}else if (type == RatingType.SERIES){
		    		sType = "series";
		    		itemId = seriesId;
		    	}else{
		    		return false;
		    	}
		    	
		    	if (rating < 0 || rating > 10)
		    		return false;
		    	
		    	
		    	
		    	// API BUG
		    	// If no series rating is set, you can't retrieve any episode ratings, so we make sure the series always has a rating
		    	
		    	if (type == RatingType.EPISODE){
		    		try {
		    			new GetRatingHandler().getSeriesRating(accountId, Long.valueOf(seriesId));
		    		}catch(RatingNotFoundException e){
		    			new SetRatingHandler().setSeriesRating(accountId, seriesId, rating);
		    		}
		    	}
		    	
		    	
		    			    	
				URL url = new URL(AppSettings.GET_RATING_URL + "accountid=" + accountId + "&itemtype=" + sType + "&itemid=" + itemId + "&rating=" + rating);	
								
			    SAXParserFactory spf = SAXParserFactory.newInstance();
			    SAXParser sp = spf.newSAXParser();
			    XMLReader xr = sp.getXMLReader();
			    xr.setContentHandler(this);
			    xr.parse(new InputSource(url.openStream()));
			    		    
			    return result;
			} catch (Exception e) {
				if (AppSettings.LOG_ENABLED)
					Log.e("xml.handlers.RatingAdapter", "Error:" + e.toString());
				return false;
			}
		}
	}


