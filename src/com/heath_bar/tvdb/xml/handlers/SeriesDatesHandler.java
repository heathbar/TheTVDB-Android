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

import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;

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
import com.heath_bar.tvdb.util.DateUtil;


public class SeriesDatesHandler extends DefaultHandler {
    private StringBuilder sb;
    private long lastAired = 0;
    private long nextAired = 0;
    private long timeNow;
    private Context context;
    
    public SeriesDatesHandler(Context ctx){
    	context = ctx;
    }
    

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
			name = name.trim().toLowerCase();
			
			if (name.equals("firstaired") && !sb.toString().equals("")){
				long airedDate = DateUtil.parseDate(sb.toString()).getTime()/1000L;
				
				if (airedDate > lastAired && airedDate < timeNow)
					lastAired = airedDate;
				
				if (airedDate > timeNow && (nextAired == 0 || airedDate < nextAired))
					nextAired = airedDate;
			}
		    
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesDatesHandler", e.toString());
		}
	}
    
	public long[] getDates(long seriesId) {
	    try {
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	String languageCode = settings.getString("language", "en");
	    	
			URL url = new URL(AppSettings.SERIES_FULL_URL + String.valueOf(seriesId) + "/all/" + languageCode + ".xml");		//http://thetvdb.com/api/0A41C0DEA5531762/series/<seriesid>/all/en.xml
			timeNow = System.currentTimeMillis() / 1000L;

		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
		    
		    return new long[]{lastAired, nextAired};
	    } catch (ConnectException e){
	    	return new long[]{-1,-1};
	    } catch (UnknownHostException e){
	    	return new long[]{-1,-2};
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesDatesHandler", e.toString());
			return new long[]{0,0};
		}
	}
}
