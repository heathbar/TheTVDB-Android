package com.heath_bar.tvdb.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.heath_bar.tvdb.data.adapters.SeriesDbAdapter;
import com.heath_bar.tvdb.data.xmlhandlers.GetTVDBFavoritesHandler;
import com.heath_bar.tvdb.data.xmlhandlers.SetTVDBFavoritesHandler;
import com.heath_bar.tvdb.types.FavoriteSeriesInfo;
import com.heath_bar.tvdb.types.exceptions.InvalidAccountIdException;

public class DALFavorites {

	protected Context context;
	private SeriesDbAdapter db;
	
	
	public DALFavorites(Context ctx){
		context = ctx;
		
		db = new SeriesDbAdapter(ctx);
		db.open();
	}
	
	public void close(){
		db.close();
	}
	
	
	
	/** Copy our list of favorites to our account on thetvdb.com; this only happens the first time when we select the option to sync */
	public void uploadLocalFavoritesToTheTVDB() {

		try {
			String accountId = getAccountId();
			
			Cursor c = db.fetchFavorites();
	
			SetTVDBFavoritesHandler setFavsHelper = new SetTVDBFavoritesHandler();			// re-implement this here vs calling addFavoriteToTheTVDB so we don't have to re-instantiate setFavsAdapter a million times
			while (c.moveToNext()){
				setFavsHelper.setFavorite(accountId, c.getLong(c.getColumnIndex(SeriesDbAdapter.KEY_ID)));
			}
		}catch (InvalidAccountIdException e){
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}catch (Exception e){
			Toast.makeText(context, "Something bad happend while uploading your favorites to thetvdb.com", Toast.LENGTH_LONG).show();
		}
	}
	
	
	/** Push a single new favorite to the account on thetvdb.com */
	public ArrayList<String> addFavoriteToTheTVDB(String accountId, long seriesId){
		SetTVDBFavoritesHandler setFavsAdatpter = new SetTVDBFavoritesHandler();
		return setFavsAdatpter.setFavorite(accountId, seriesId);
	}
	
    /** Save the series as a favorite to the database, and if necessary, to thetvdb.com  */
    public long createFavoriteSeries(FavoriteSeriesInfo info) {

    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	boolean syncFavsTVDB = preferences.getBoolean("syncFavsTVDB", false);
    	
    	if (syncFavsTVDB){
    		String accountId;
			try {
				accountId = getAccountId();
	    		ArrayList<String> favoritesList = addFavoriteToTheTVDB(accountId, info.get_seriesId());
	    		importFavorites(favoritesList);
	    		return 1;
			} catch (InvalidAccountIdException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
    		return 0;
    	}else {
    		return db.createFavoriteSeries(info);
    	}
    }

    public void importFavoritesFromTVDB(){
    	
    	String accountId;
		try {
			accountId = getAccountId();
			
			GetTVDBFavoritesHandler getFavsHelper = new GetTVDBFavoritesHandler();
	    	ArrayList<String> favoritesList = getFavsHelper.getFavorites(accountId);
	    	
	    	importFavorites(favoritesList);
	    	
		} catch (InvalidAccountIdException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }

    public void importFavoritesFromXBMC() {
		// TODO: implement this
	}
    
    private void importFavorites(ArrayList<String> favoritesList){
    	
    }
	
	public Cursor fetchFavorites(String sortBy){
		return db.fetchFavorites(sortBy);
	}

	
	
	
	/** Helper function to easily get the accountId */
	private String getAccountId() throws InvalidAccountIdException {
		String accountId = PreferenceManager.getDefaultSharedPreferences(context).getString("accountId", "");
		if (accountId.equals(""))
			throw new InvalidAccountIdException("You must specify your account identifier in the application settings before you can sync your favorite shows.");
		return accountId;
	}
	
}
