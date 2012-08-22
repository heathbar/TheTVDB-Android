package com.heath_bar.tvdb.xml.handlers;

import java.net.URL;

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
import com.heath_bar.tvdb.types.TvEpisode;
import com.heath_bar.tvdb.types.TvEpisodeList;
import com.heath_bar.tvdb.util.DateUtil;


public class EpisodeListHandler extends DefaultHandler {
    private StringBuilder sb;
    private TvEpisode currentEpisode;
    private TvEpisodeList episodeList;
    private Context context;
    
    public EpisodeListHandler(Context ctx){
    	context = ctx;
    }

    @Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
	    name = name.trim().toLowerCase();				// format the current element name
	    sb = new StringBuilder();						// Reset the string builder
	    
	    if (name.equals("episode"))
	    	currentEpisode = new TvEpisode();
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
			
			if (name.equals("episode")){
				episodeList.add(currentEpisode);
			} else if (name.equals("id")){
				if (currentEpisode != null)
					currentEpisode.setId(Integer.valueOf(sb.toString()));
			} else if (name.equals("episodenumber")){
				currentEpisode.setNumber(Integer.valueOf(sb.toString()));
			} else if (name.equals("seasonnumber")) {
				currentEpisode.setSeason(Integer.valueOf(sb.toString()));
			} else if (name.equals("episodename")) {
				currentEpisode.setName(sb.toString());
			} else if (name.equals("firstaired")){
				if (currentEpisode != null){
					long date = DateUtil.parseDate(sb.toString()).getTime()/1000L;
					currentEpisode.setAirDate(date);
				}
			}
		    
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.EpisodeListHandler", e.toString());
		}
	}
    
	public TvEpisodeList getEpisodes(long seriesId) {
	    try {
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	String languageCode = settings.getString("language", "en");
	    	
			URL url = new URL(AppSettings.SERIES_FULL_URL + String.valueOf(seriesId) + "/all/" + languageCode + ".xml");		//http://thetvdb.com/api/0A41C0DEA5531762/series/<seriesid>/all/en.xml
			
			episodeList = new TvEpisodeList();
			
		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
		    
		    return episodeList; 
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.EpisodeListHandler", e.toString());
			return new TvEpisodeList();
		}
	}
}
