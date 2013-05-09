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
	
	public ArrayList<Integer> getSeasonList(){
		ArrayList<Integer> seasons = new ArrayList<Integer>();
		
		for (Iterator<TvEpisode> i = this.iterator(); i.hasNext();){
			 int seasonNo = i.next().getSeason();
			 
			 if (!seasons.contains(seasonNo))
				 seasons.add(seasonNo);
		}
		return seasons;	
	}
	

	public int getNumberOfEpisodesInSeason(int seasonNumber) {
		int count = 0;
		
		for (Iterator<TvEpisode> i = this.iterator(); i.hasNext();){
			 if (i.next().getSeason() == seasonNumber)
				count++;
		}
		return count;
	}
	
	public TvEpisode getEpisode(int seasonNumber, int episodeNumber){
		for (Iterator<TvEpisode> i = this.iterator(); i.hasNext();){
			TvEpisode ep = i.next();
			if (ep.getSeason() == seasonNumber && ep.getNumber() == episodeNumber){
				return ep;
			}
		}
		return null;
	}
}
