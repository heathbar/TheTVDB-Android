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
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.heath_bar.tvdb.data.FavoritesData;
import com.heath_bar.tvdb.data.adapters.SeriesDbAdapter;
import com.heath_bar.tvdb.data.xmlhandlers.FavoritesDetailsHandler;
import com.heath_bar.tvdb.types.FavoriteSeriesInfo;



/*
 * 
 * The IntentService does the following:
 * Creates a default worker thread that executes all intents delivered to onStartCommand() separate from your application's main thread.
 * Creates a work queue that passes one intent at a time to your onHandleIntent() implementation, so you never have to worry about multi-threading.
 * Stops the service after all start requests have been handled, so you never have to call stopSelf().
 * Provides default implementation of onBind() that returns null.
 * Provides a default implementation of onStartCommand() that sends the intent to the work queue and then to your onHandleIntent() implementation.
 * 
 * All this adds up to the fact that all you need to do is implement onHandleIntent() to do the work provided by the client. 
 */
public class UpdateService extends IntentService {

	private boolean stopNow = false;
	private final IBinder binder = new LocalBinder();		// provide a binder to the client so that they can ask us to stop early
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
	
		// Reset this flag
		stopNow = false;

		// Lookup which pieces we need to run
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean syncFavsTVDB = preferences.getBoolean("syncFavsTVDB", false);
    	boolean syncFavsXBMC = preferences.getBoolean("syncFavsXBMC", false);

    	// Connect to the data store
    	FavoritesData favorites = new FavoritesData(this);
		
    	if (syncFavsTVDB && !stopNow)
			favorites.importFavoritesFromTVDB();
	
		if (syncFavsXBMC && !stopNow)
			favorites.importFavoritesFromXBMC();
	
		
		FavoritesDetailsHandler tvdb = new FavoritesDetailsHandler(getApplicationContext());
		Cursor favs = null;
		
		try {
			// Get the list of favorite shows from the database
			if (!stopNow)
				favs = favorites.fetchAllFavorites();
			
			// Loop through each show
			while (favs.moveToNext() && !stopNow)
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

		if (!stopNow){
			// Tell the UI that we have completed the update
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_COMPLETE);
			sendBroadcast(broadcastIntent);
		}
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {		
		return binder;
	}
	
	
	public class LocalBinder extends Binder {
        UpdateService getService() {
            return UpdateService.this;	// Return this instance of LocalService so clients can call public methods
        }
    }
	
	/** Stop updating early */
	public void stop(){
		stopNow = true;
	}
}
