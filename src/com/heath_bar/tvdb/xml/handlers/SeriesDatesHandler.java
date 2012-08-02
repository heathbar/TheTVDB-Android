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
import com.heath_bar.tvdb.util.DateUtil;


public class SeriesDatesHandler extends DefaultHandler {
    private StringBuilder sb;
    private long lastAired = 0;
    private long nextAired = 0;
    private long timeNow;
    

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
			URL url = new URL(AppSettings.SERIES_FULL_URL + String.valueOf(seriesId) + "/all/" + AppSettings.LANGUAGE + ".xml");		//http://thetvdb.com/api/0A41C0DEA5531762/series/<seriesid>/all/en.xml
			timeNow = System.currentTimeMillis() / 1000L;

		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
		    
		    return new long[]{lastAired, nextAired}; 
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesDatesHandler", e.toString());
			return new long[]{0,0};
		}
	}
}
