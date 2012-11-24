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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.heath_bar.tvdb.data.FavoritesData;
import com.heath_bar.tvdb.data.adapters.SeriesDbAdapter;
import com.heath_bar.tvdb.data.xmlhandlers.FavoritesDetailsHandler;
import com.heath_bar.tvdb.types.FavoriteSeriesInfo;

public class UpdateService extends IntentService {

	private static boolean RUNNING = false;
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_COMPLETE = "complete";

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
	
		if (!RUNNING){
			RUNNING = true;

			// Lookup which pieces we need to run
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
	    	boolean syncFavsTVDB = preferences.getBoolean("syncFavsTVDB", false);
	    	boolean syncFavsXBMC = preferences.getBoolean("syncFavsXBMC", false);
	
	    	// Connect to the data store
	    	FavoritesData favorites = new FavoritesData(this);
			
	    	if (syncFavsTVDB)
				favorites.importFavoritesFromTVDB();
		
			if (syncFavsXBMC)
				favorites.importFavoritesFromXBMC();
		
			
			FavoritesDetailsHandler tvdb = new FavoritesDetailsHandler(getApplicationContext());
			Cursor favs = null;
			
			try {
				// Get the list of favorite shows from the database
				favs = favorites.fetchAllFavorites();
				
				// Loop through each show
				while (favs.moveToNext())
				{
					long seriesId = favs.getLong(favs.getColumnIndex(SeriesDbAdapter.KEY_ID));
					FavoriteSeriesInfo info = tvdb.getInfo(seriesId);
									
					// update the db with the new info
					favorites.updateFavorite(info);
					
					// Tell the UI that something changed
					Intent broadcastIntent = new Intent();
					broadcastIntent.setAction(ACTION_UPDATE);
					sendBroadcast(broadcastIntent);
					
				}
			}catch (Exception e){
				Log.e("Update Service", "Crash! " + e.getMessage());				
			}finally {
				if (favs != null)
					favs.close();
				favorites.close();
			}
			
			// Tell the UI that we're done
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_COMPLETE);
			sendBroadcast(broadcastIntent);
			
	        RUNNING = false;
		}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		RUNNING = false;
	}
	
}
