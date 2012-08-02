package com.heath_bar.tvdb.types;

import java.util.ArrayList;
import java.util.Iterator;



public class TvEpisodeList extends ArrayList<TvEpisode> {

	private static final long serialVersionUID = 1L;
	
	public TvEpisode getNextAired() {
		TvEpisode nextAired = null;
		long nextAiredLong = 0;
		long timeNow = System.currentTimeMillis() / 1000L;
		
		for (Iterator<TvEpisode> i = this.iterator(); i.hasNext();){
			TvEpisode ep = i.next(); 
						
			if (ep.getAirDate() > timeNow && (nextAiredLong == 0 || ep.getAirDate() < nextAiredLong)){
				nextAiredLong = ep.getAirDate();
				nextAired = ep;
			}
		}
		return nextAired;
	}
	
	public TvEpisode getLastAired() {
		TvEpisode lastAired = null;
		long lastAiredLong = 0;
		long timeNow = System.currentTimeMillis() / 1000L;
		
		for (Iterator<TvEpisode> i = this.iterator(); i.hasNext();){
			 TvEpisode ep = i.next();

			 
			 if (ep.getAirDate() > lastAiredLong && ep.getAirDate() < timeNow){
				lastAiredLong = ep.getAirDate();
				lastAired = ep;
			 }
		}
		return lastAired;
	}

	public int getNumberOfSeasons() {
		int maxSeasonNo = 0;
		
		for (Iterator<TvEpisode> i = this.iterator(); i.hasNext();){
			 int seasonNo = i.next().getSeason();
			
			if (seasonNo > maxSeasonNo)
				maxSeasonNo = seasonNo;
		}
		return maxSeasonNo;
	}
	

	public int getNumberOfEpisodesPerSeason(int seasonNumber) {
		int maxSeasonNo = 0;
		
		for (Iterator<TvEpisode> i = this.iterator(); i.hasNext();){
			 int seasonNo = i.next().getSeason();
			
			if (seasonNo > maxSeasonNo)
				maxSeasonNo = seasonNo;
		}
		return maxSeasonNo;
	}
}
