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

public class TVDBFavoritesHandler extends DefaultHandler{
		private StringBuilder sb;
	    private ArrayList<Long> favoritesList;
	    
	    
	    @Override
		public void startElement(String uri, String name, String qName, Attributes atts) {
		    sb = new StringBuilder();						// Reset the string builder
		    
		    name = name.trim().toLowerCase();
		    if (name.equals("favorites"))
		    	favoritesList = new ArrayList<Long>();
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
				
				if (name.equals("series")){
					favoritesList.add(Long.parseLong(sb.toString()));
				}

			} catch (Exception e) {
				if (AppSettings.LOG_ENABLED)
					Log.e("GetTVDBFavoritesAdapter", e.toString());				
			}
		}
	    
	    /** Add a show to the accountId's favorites list; return a list of all favorites for accountId */
	    public ArrayList<Long> addFavorite(String accountId, long seriesId){
		    try {
				URL url = new URL(AppSettings.FAVORITES_URL + "accountid=" + accountId + "&type=add&seriesid=" + seriesId);

			    SAXParserFactory spf = SAXParserFactory.newInstance();
			    SAXParser sp = spf.newSAXParser();
			    XMLReader xr = sp.getXMLReader();
			    xr.setContentHandler(this);
			    xr.parse(new InputSource(url.openStream()));
			    
			    return favoritesList;
		    } catch (Exception e){
				Log.e("GetTVDBFavoritesAdapter", e.getMessage());
			}
		    return null;
		}
	    
	    /** Return a list of all favorites for accountId */
	    public ArrayList<Long> getFavorites(String accountId){
		    try {
				URL url = new URL(AppSettings.FAVORITES_URL + "accountid=" + accountId);	

			    SAXParserFactory spf = SAXParserFactory.newInstance();
			    SAXParser sp = spf.newSAXParser();
			    XMLReader xr = sp.getXMLReader();
			    xr.setContentHandler(this);
			    xr.parse(new InputSource(url.openStream()));
			    
			    return favoritesList;
		    } catch (Exception e){
				Log.e("GetTVDBFavoritesAdapter", e.getMessage());
			}
		    return null;
		}
	    
	    /** Remove a show from accountId's favorites list; return a list of all favorites for accountId */
	    public ArrayList<Long> removeFavorite(String accountId, long seriesId){
		    try {
				URL url = new URL(AppSettings.FAVORITES_URL + "accountid=" + accountId + "&type=remove&seriesid=" + seriesId);

			    SAXParserFactory spf = SAXParserFactory.newInstance();
			    SAXParser sp = spf.newSAXParser();
			    XMLReader xr = sp.getXMLReader();
			    xr.setContentHandler(this);
			    xr.parse(new InputSource(url.openStream()));
			    
			    return favoritesList;
		    } catch (Exception e){
				Log.e("GetTVDBFavoritesAdapter", e.getMessage());
			}
		    return null;
		}
	}


