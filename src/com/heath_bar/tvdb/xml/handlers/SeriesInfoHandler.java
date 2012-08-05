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
import com.heath_bar.tvdb.types.TvSeries;


public class SeriesInfoHandler extends DefaultHandler {

    private StringBuilder sb;
    private TvSeries currentSeries;

    @Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
	    name = name.trim().toLowerCase();				// format the current element name
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
			name = name.trim().toLowerCase();
			
			if (name.equals("id")){
				currentSeries.setId(sb.toString());
			}else if (name.equals("banner")){
				currentSeries.getImage().setUrl(sb.toString());
			}else if (name.equals("poster")){
				currentSeries.setPoster(sb.toString());
			}else if (name.equals("firstaired")){
				currentSeries.setFirstAired(sb.toString());
			}else if (name.equals("imdb_id")){
				currentSeries.setImdb(sb.toString());
			}else if (name.equals("language")){
				currentSeries.setLanguage(sb.toString());
			}else if (name.equals("seriesname")){
				currentSeries.setName(sb.toString());
			}else if (name.equals("overview")){
				currentSeries.setOverview(sb.toString());
			}else if (name.equals("actors")){
				currentSeries.setActors(sb.toString());
			}else if (name.equals("airs_dayofweek")){
				currentSeries.setAirDay(sb.toString());
			}else if (name.equals("airs_time")){
				currentSeries.setAirTime(sb.toString());
			}else if (name.equals("network")){
				currentSeries.setNetwork(sb.toString());
			}else if (name.equals("rating")){
				currentSeries.setRating(sb.toString());
			}else if (name.equals("status")){
				currentSeries.setStatus(sb.toString());
			}else if (name.equals("genre")){
				currentSeries.setStatus(sb.toString());
			}else if (name.equals("runtime")){
				currentSeries.setStatus(sb.toString());
			}
				
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesHandler", e.toString());
		}
	}
    
	public TvSeries getInfo(long seriesId) {
	    try {
			URL url = new URL(AppSettings.SERIES_FULL_URL + String.valueOf(seriesId) + "/" + AppSettings.LANGUAGE + ".xml");		//http://thetvdb.com/api/0A41C0DEA5531762/series/<seriesid>/en.xml
			
		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
	    
		    return currentSeries;
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesHandler", e.toString());
		    return null;
		}
	}
}
