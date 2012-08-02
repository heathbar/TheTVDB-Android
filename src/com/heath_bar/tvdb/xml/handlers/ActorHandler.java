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
import com.heath_bar.tvdb.types.Actor;

public class ActorHandler extends DefaultHandler{
	private StringBuilder sb;
    private Actor currentActor;
    private Actor targetActor;
    private String searchName;

    @Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
	    name = name.trim().toLowerCase();				// format the current element name
	    sb = new StringBuilder();						// Reset the string builder
	    
	    if (name.equals("actor"))
	    	currentActor = new Actor();
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
			
			if (targetActor == null){
				if (name.equals("id")){
					currentActor.setId(Integer.valueOf(sb.toString()));
				} else if (name.equals("image")){
					currentActor.getImage().setUrl(sb.toString());
				} else if (name.equals("name")) {
					currentActor.setName(sb.toString());
				} else if (name.equals("role")) {
					currentActor.setRole(sb.toString());
				} else if (name.equals("actor") && currentActor.getName().equals(searchName)){
					targetActor = currentActor;
				}
			}
		    
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.EpisodeHandler", e.toString());
		}
	}
    
	public Actor getActor(String seriesId, String actorName) {
	    try {
			URL url = new URL(AppSettings.SERIES_FULL_URL + seriesId + "/actors.xml");	
			searchName = actorName;
			
		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
		    
		    return targetActor;
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.EpisodeHandler", e.toString());
			return new Actor();
		}
	}
}


