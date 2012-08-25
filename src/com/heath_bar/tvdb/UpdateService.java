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
package com.heath_bar.tvdb;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.heath_bar.tvdb.adapters.SeriesDbAdapter;
import com.heath_bar.tvdb.types.FavoriteSeriesInfo;
import com.heath_bar.tvdb.xml.handlers.SeriesDatesHandler;

public class UpdateService extends IntentService {

	private static boolean RUNNING = false;
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_COMPLETE = "complete";
	public static final String CONNECT_EXCEPTION = "except";

	/** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	public UpdateService(){
		super("UpdateService");
	}

	/**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	
	@Override
	protected void onHandleIntent (Intent intent){
	
		if (AppSettings.LOG_ENABLED)
			Log.d("UpdateService", "Service Handle Intent");

		RefreshAirDates(this);
		
	}
	
	private void RefreshAirDates(Context ctx){
		if (!RUNNING){
			RUNNING = true;
			
			SeriesDbAdapter db = new SeriesDbAdapter(ctx);
			Cursor favs = null;
			
			try {
				// Get the list of favorite shows from the database
				db.open();
				favs = db.fetchFavorites();
				
				// Loop through each show
				while (favs.moveToNext())
				{
					int seriesId = favs.getInt(favs.getColumnIndex(SeriesDbAdapter.KEY_ID));
					String seriesName = favs.getString(favs.getColumnIndex(SeriesDbAdapter.KEY_TITLE));
					
					SeriesDatesHandler tvdb = new SeriesDatesHandler(getApplicationContext());
					long[] airDates = tvdb.getDates(seriesId);
					
					if (airDates[0] == -1){
						// Tell the UI that there was an error
						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(CONNECT_EXCEPTION);
						sendBroadcast(broadcastIntent);
					} else {
						// update the db with the new dates
						FavoriteSeriesInfo info = new FavoriteSeriesInfo(seriesId, seriesName, String.valueOf(airDates[0]), String.valueOf(airDates[1]));
						db.updateFavorite(info);
						
						// Tell the UI that something changed
						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(ACTION_UPDATE);
						sendBroadcast(broadcastIntent);
					}
				}
			} finally {
				if (!favs.equals(null))
					favs.close();
				db.close();
			}
			
			// Tell the UI that we're done
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_COMPLETE);
			sendBroadcast(broadcastIntent);
			
	        RUNNING = false;
		}
    }
	
}
