package com.heath_bar.tvdb.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.heath_bar.tvdb.types.FavoriteSeriesInfo;

public class SeriesDbAdapter {

	
    public static final String KEY_ID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_LAST_AIRED = "last_aired";
    public static final String KEY_NEXT_AIRED = "next_aired";

    private DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    public static final String FAVORITES_TABLE = "favorite_series";

    private final Context mCtx;

    public SeriesDbAdapter(Context ctx){
    	mCtx = ctx;
    }
    
    public SeriesDbAdapter open() throws SQLException {
        mDbHelper = new DbHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
    	try{
    		mDbHelper.close();
    	} catch(SQLiteException ex){
    		mDbHelper = null; // make the most of a bad situation
    	}
    }
    
    
    public long createFavoriteSeries(FavoriteSeriesInfo info) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, Long.valueOf(info.get_seriesId()));
        initialValues.put(KEY_TITLE, info.get_seriesName());
        initialValues.put(KEY_LAST_AIRED, Long.valueOf(info.get_lastAired()));
        initialValues.put(KEY_NEXT_AIRED, Long.valueOf(info.get_nextAired()));

        long groupId = mDb.insert(FAVORITES_TABLE, "0", initialValues);
        
        
        return groupId;
    }

    
    /**
     * Fetch the entire favorites table
     * @return
     */
    public Cursor fetchFavorites() {

        return mDb.query(FAVORITES_TABLE, 
        		new String[] {
        			KEY_ID, 
        			KEY_TITLE,
        			"CASE WHEN " + KEY_LAST_AIRED + " = 0 THEN 'Unknown' ELSE date(datetime("+KEY_LAST_AIRED+", 'unixepoch')) END AS " + KEY_LAST_AIRED,
        			"CASE WHEN " + KEY_NEXT_AIRED + " = 0 THEN 'Unknown' ELSE date(datetime("+KEY_NEXT_AIRED+", 'unixepoch')) END AS " + KEY_NEXT_AIRED
    			},
                null, null, null, null, KEY_TITLE);
    }
    
    
    public void updateFavorite(FavoriteSeriesInfo info){
    	if (info.get_seriesId() != 0){
	    	ContentValues newValues = new ContentValues();
	        newValues.put(KEY_ID, info.get_seriesId());
	        if (info.get_seriesName() != null)
	        	newValues.put(KEY_TITLE, info.get_seriesName());
	        if (info.get_lastAired() != null)
	        	newValues.put(KEY_LAST_AIRED, info.get_lastAired());
	        if (info.get_nextAired() != null)
	        	newValues.put(KEY_NEXT_AIRED, info.get_nextAired());
	        
	    	mDb.update(FAVORITES_TABLE, newValues, KEY_ID + " = ?", new String[]{String.valueOf(info.get_seriesId())});
    	}
    }
    
    /**
     * Reset all Next and Last aired dates
     */
    public void clearAiredDates(){
    	mDb.execSQL("UPDATE " + FAVORITES_TABLE + " SET " + KEY_LAST_AIRED + " = 0, " + KEY_NEXT_AIRED + " = 0");
    }

	public boolean isFavoriteSeries(String id) {

		
		Cursor c = mDb.query(FAVORITES_TABLE, new String[]{KEY_ID}, KEY_ID + "=?", new String[]{id}, null, null,null);

		int count = c.getCount();
		c.close();
		if (count > 0)
			return true;
		else
			return false;
	}

	public boolean removeFavoriteSeries(long id) {
		int rows = mDb.delete(FAVORITES_TABLE, KEY_ID + "=?", new String[]{ String.valueOf(id)});
		return (rows > 0);		
	}
    

}
