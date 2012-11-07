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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.types.Rating;

public class GetRatingAdapter extends DefaultHandler{
		private StringBuilder sb;
	    private long episodeId = -1;
	    private Rating currentRating;
	    private Rating resultRating;
	    
	    
	    @Override
		public void startElement(String uri, String name, String qName, Attributes atts) {
		    sb = new StringBuilder();						// Reset the string builder
		    currentRating = new Rating();
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
				
				if (name.equals("seriesid") || name.equals("id"))
					currentRating.setId(sb.toString());
				else if (name.equals("userrating"))
					currentRating.setUserRating(sb.toString());
				else if (name.equals("communityrating"))
					currentRating.setCommunityRating(sb.toString());	
				
				
				// If we hit the end of a series tag and we aren't looking for an episode rating, the current rating is our response
				else if (name.equals("series") && episodeId == -1)
					resultRating = currentRating;
				

				// if we hit the end of an episode tag and the id matches our episode id, the current rating is our response
				else if (name.equals("episode") && currentRating.getId().equals(String.valueOf(episodeId)))
						resultRating = currentRating;

			} catch (Exception e) {
				if (AppSettings.LOG_ENABLED)
					Log.e("xml.handlers.RatingAdapter", e.toString());
			}
		}
	    
	    public Rating getSeriesRating(String accountId, long seriesId) {
		    try {
				URL url = new URL(AppSettings.SET_RATING_URL + "accountid=" + accountId + "&seriesid=" + seriesId);	
								
			    SAXParserFactory spf = SAXParserFactory.newInstance();
			    SAXParser sp = spf.newSAXParser();
			    XMLReader xr = sp.getXMLReader();
			    xr.setContentHandler(this);
			    xr.parse(new InputSource(url.openStream()));
			    		    
			    return resultRating;
			} catch (Exception e) {
				if (AppSettings.LOG_ENABLED)
					Log.e("xml.handlers.RatingAdapter", e.toString());
				return new Rating();
			}
		}
	    
	    public Rating getEpisodeRating(String accountId, long seriesId, long episodeId) {
		    try {
				URL url = new URL(AppSettings.SET_RATING_URL + "accountid=" + accountId + "&seriesid=" + seriesId);	
				this.episodeId = episodeId;
				
			    SAXParserFactory spf = SAXParserFactory.newInstance();
			    SAXParser sp = spf.newSAXParser();
			    XMLReader xr = sp.getXMLReader();
			    xr.setContentHandler(this);
			    xr.parse(new InputSource(url.openStream()));
			    		    
			    return resultRating;
			} catch (Exception e) {
				if (AppSettings.LOG_ENABLED)
					Log.e("xml.handlers.RatingAdapter", e.toString());
				return new Rating();
			}
		}
	}


