package com.heath_bar.tvdb.xml.handlers;

import java.net.URL;
import java.net.URLEncoder;
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
import com.heath_bar.tvdb.types.TvSeries;


public class SeriesSearchHandler extends DefaultHandler {

    private TvSeries currentSeries = null;
    private ArrayList<TvSeries> seriesList = null;
    private StringBuilder sb;

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
			
			if (name.equals("series")){
				if (currentSeries.getLanguage().equals(AppSettings.LANGUAGE))	// only add series that match my language
					seriesList.add(currentSeries);								// End of a mirror, add it to the list
			
			}else if (name.equals("id")){
				currentSeries.setId(sb.toString());
			}else if (name.equals("banner")){
				currentSeries.getImage().setUrl(sb.toString());
			}else if (name.equals("firstAired")){
				currentSeries.setFirstAired(sb.toString());
			}else if (name.equals("imdb")){
				currentSeries.setImdb(sb.toString());
			}else if (name.equals("language")){
				currentSeries.setLanguage(sb.toString());
			}else if (name.equals("seriesname")){
				currentSeries.setName(sb.toString());
			}else if (name.equals("overview")){
				currentSeries.setOverview(sb.toString());
			}else{
				if (AppSettings.LOG_ENABLED)
					Log.w("xml.handlers.SeriesSearchHandler", "'"+ name + "' - tag not recognized: " + sb.toString());
			}
		    
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.SeriesHandler", e.toString());
		}
	}
    
	public ArrayList<TvSeries> searchSeries(String seriesName) {
	    try {
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
