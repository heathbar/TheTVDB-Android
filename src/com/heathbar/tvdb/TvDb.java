package com.heathbar.tvdb;

import java.util.ArrayList;

import android.util.Log;

import com.heathbar.tvdb.xml.handlers.SeriesHandler;

public class TvDb {


	public ArrayList<TvSeries> getSeriesList(String name){
		try {
			SeriesHandler seriesHandler = new SeriesHandler();
			return seriesHandler.getList(name);
		}catch (Exception e){
			if (AppSettings.LOG_ENABLED)
				Log.e("TvDb", e.getMessage());
			return null;
		}
	}
	
	public String[] getSeriesNames(String name){
		ArrayList<TvSeries> seriesList = getSeriesList(name);
		
		String[] nameList = new String[seriesList.size()];
		
		for (int i=0; i<seriesList.size(); i++)
			nameList[i] = seriesList.get(i).getName();
		
		return nameList;
	}
	
	
}
