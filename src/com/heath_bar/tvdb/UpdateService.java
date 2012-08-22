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
