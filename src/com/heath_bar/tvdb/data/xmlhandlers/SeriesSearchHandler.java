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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.types.TvSeries;


public class SeriesSearchHandler extends DefaultHandler {

    private TvSeries currentSeries = null;
    private ArrayList<TvSeries> seriesList = null;
    private StringBuilder sb;
    private Context context;
    private String languageCode = "en";
    
    public SeriesSearchHandler(Context ctx){
    	context = ctx;
    }

    @Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
	    name = name.trim().toLowerCase(Locale.getDefault());				// format the current element name
	    sb = new StringBuilder();						// Reset the string builder

	    if (name.equals("series")){						// If this is a new node, create a new instance
	    	currentSeries = new TvSeries();
    	}
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
			name = name.trim().toLowerCase(Locale.getDefault());
			
			if (name.equals("series")){
				if (currentSeries.getLanguage().equals(languageCode))	// only add series that match my language
					seriesList.add(currentSeries);								// End of a mirror, add it to the list
			
			}else if (name.equals("id")){
				currentSeries.setId(Long.valueOf(sb.toString()));
			}else if (name.equals("banner")){
				currentSeries.getBanner().setUrl(sb.toString());
			}else if (name.equals("firstAired")){
				currentSeries.setFirstAired(sb.toString());
			}else if (name.equals("imdb_id")){
				currentSeries.setIMDB(sb.toString());
			}else if (name.equals("language")){
				currentSeries.setLanguage(sb.toString());
			}else if (name.equals("seriesname")){
				currentSeries.setName(sb.toString());
			}else if (name.equals("overview")){
				currentSeries.setOverview(sb.toString());
			}else{
				if (AppSettings.LOG_ENABLED)
					Log.d("xml.handlers.SeriesSearchHandler", "'"+ name + "' - tag not recognized: " + sb.toString());
			}
		    
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesHandler", e.toString());
		}
	}
    
	public ArrayList<TvSeries> searchSeries(String seriesName) {
	    try {
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	languageCode = settings.getString("language", "en");
	    	
			URL url = new URL(AppSettings.SERIES_BASIC_URL + URLEncoder.encode(seriesName,"UTF-8"));		//http://www.thetvdb.com/api/GetSeries.php?seriesname=
			
			seriesList = new ArrayList<TvSeries>();

		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
	    
		    return seriesList;
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesSearchHandler", e.toString());
		    return null;
		}
	}
}
