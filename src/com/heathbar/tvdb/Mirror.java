package com.heathbar.tvdb;

import java.util.ArrayList;

import com.heathbar.tvdb.xml.handlers.Mirrors;

// http://www.thetvdb.com/wiki/index.php/API:mirrors.xml

public class Mirror {

	private String id;
	private String path;
	private String typemask;
	
	// typemask is the sum of what it contains of the following values: 
	// 1 xml files
	// 2 banner files
	// 4 zip files

	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getTypemask() {
		return typemask;
	}
	public void setTypemask(String typemask) {
		this.typemask = typemask;
	}
	
	
	/** Get list of mirrors from thetvdb.com */
	public static String[] getMirrorList(){
		
		
		ArrayList<Mirror> mirrors = new Mirrors().getList();
		
		
		
	}
	
	
}
