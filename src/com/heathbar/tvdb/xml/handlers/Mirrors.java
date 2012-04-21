package com.heathbar.tvdb.xml.handlers;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

import com.heathbar.tvdb.AppSettings;
import com.heathbar.tvdb.Mirror;


public class Mirrors extends DefaultHandler {

    private Mirror currentMirror = null;
    private ArrayList<Mirror> mirrorList = null;
    private StringBuilder sb;

    public void startElement(String uri, String name, String qName, Attributes atts) {
	    name = name.trim();								// format the current element name
	    sb = new StringBuilder();						// Reset the string builder
	    
	    if (name.equals("node")){						// If this is a new node, create a new instance
	    	currentMirror = new Mirror();
    	}
    }
    
    // SAX parsers may return all contiguous character data in a single chunk, or they may split it into several chunks
    // Therefore we must aggregate the data here, and set it in endElement() function
	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));
		sb.append(chars);
	}


    public void endElement(String uri, String name, String qName) throws SAXException {
		try {
			name = name.trim().toLowerCase();
			
			
			if (name.equals("mirrors")){
																			// end of file, do nothing
			}else if (name.equals("mirror")){
	    		mirrorList.add(currentMirror);								// End of a mirror, add it to the list
			}else if (name.equals("id")){
				currentMirror.setId(sb.toString());
			}else if (name.equals("mirrorpath")){
				currentMirror.setPath(sb.toString());
			}else if (name.equals("typemask")){
				currentMirror.setPath(sb.toString());
			}else{
				if (AppSettings.LOG_ENABLED)
					Log.w("xml.handlers.Mirrors", "'"+ name + "' - tag not recognized: " + sb.toString());
			}
		    
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.Mirrors", e.toString());
		}
	}
    
	public ArrayList<Mirror> getList() {
	    try {
			URL url = new URL(AppSettings.MIRROR_URL);
			mirrorList = new ArrayList<Mirror>();

		    SAXParserFactory spf = SAXParserFactory.newInstance();
		    SAXParser sp = spf.newSAXParser();
		    XMLReader xr = sp.getXMLReader();
		    xr.setContentHandler(this);
		    xr.parse(new InputSource(url.openStream()));
	    
		    return mirrorList;
		} catch (Exception e) {
			if (AppSettings.LOG_ENABLED)
				Log.e("xml.handlers.Mirrors", e.toString());
		    return null;
		}
	}
}
