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
package com.heath_bar.tvdb.data.adapters;

import java.util.ArrayList;

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
    public static final String KEY_POSTER = "poster";

    private DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    public static final String FAVORITES_TABLE = "favorite_series";

    private final Context context;

    public SeriesDbAdapter(Context ctx){
    	context = ctx;
    }
        
    
    public SeriesDbAdapter open() throws SQLException {
    	mDbHelper = new DbHelper(context);
        mDb = mDbHelper.getWritableDatabase();
        
        return this;
    }

    public void close() {
    	try{
    		mDbHelper.close();
    		mDbHelper = null;
    	} catch(SQLiteException ex){
    		mDbHelper = null; // make the most of a bad situation
    	}
    }
    
    
    /** Basic create, only creates seriesId, the assumption being that the updater service will fill in the rest of the info */
	public long createFavoriteSeries(long seriesId) {
		ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, seriesId);
        initialValues.put(KEY_TITLE, "");
        initialValues.put(KEY_LAST_AIRED, 0);
        initialValues.put(KEY_NEXT_AIRED, 0);
        initialValues.put(KEY_POSTER, "");

        return mDb.insert(FAVORITES_TABLE, "0", initialValues);
	}
	
    public long addFavoriteSeries(FavoriteSeriesInfo info) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, info.getSeriesId());
        initialValues.put(KEY_TITLE, info.getSeriesName());
        initialValues.put(KEY_LAST_AIRED, info.getLastAired());
        initialValues.put(KEY_NEXT_AIRED, info.getNextAired());
        initialValues.put(KEY_POSTER, info.getPoster());

        return mDb.insert(FAVORITES_TABLE, "0", initialValues);
    }
   
    
    /**
     * Fetch favorites that have names
     * @return
     */
    public Cursor fetchNamedFavorites(String sortBy){
        return mDb.query(FAVORITES_TABLE, 
        		new String[] {
        			KEY_ID, 
        			KEY_TITLE,
        			"CASE WHEN " + KEY_LAST_AIRED + " = 0 THEN 'Unknown' ELSE date(datetime("+KEY_LAST_AIRED+", 'unixepoch', 'localtime')) END AS " + KEY_LAST_AIRED,
        			"CASE WHEN " + KEY_NEXT_AIRED + " = 0 THEN 'Unknown' WHEN " + KEY_NEXT_AIRED + " = -1 THEN 'ZZ' ELSE date(datetime("+KEY_NEXT_AIRED+", 'unixepoch', 'localtime')) END AS " + KEY_NEXT_AIRED,	// using ZZ = hack so that it shows up at the bottom when sorted
        			KEY_POSTER
    			},
                KEY_TITLE + " <> ''", null, null, null, sortBy + ", " + KEY_TITLE);
    }
    
    public Cursor fetchAllFavorites(){
        return mDb.query(FAVORITES_TABLE, 
        		new String[] {
        			KEY_ID, 
        			KEY_TITLE,
        			"CASE WHEN " + KEY_LAST_AIRED + " = 0 THEN 'Unknown' ELSE date(datetime("+KEY_LAST_AIRED+", 'unixepoch', 'localtime')) END AS " + KEY_LAST_AIRED,
        			"CASE WHEN " + KEY_NEXT_AIRED + " = 0 THEN 'Unknown' WHEN " + KEY_NEXT_AIRED + " = -1 THEN 'ZZ' ELSE date(datetime("+KEY_NEXT_AIRED+", 'unixepoch', 'localtime')) END AS " + KEY_NEXT_AIRED,	// using ZZ = hack so that it shows up at the bottom when sorted
        			KEY_POSTER
    			},
                null, null, null, null, null);
    }
    
    
    
    public void updateFavorite(FavoriteSeriesInfo info){
    	if (info.getSeriesId() != 0){
	    	ContentValues newValues = new ContentValues();
	        newValues.put(KEY_ID, info.getSeriesId());
	        if (info.getSeriesName() != null)
	        	newValues.put(KEY_TITLE, info.getSeriesName());
        	newValues.put(KEY_LAST_AIRED, info.getLastAired());
        	newValues.put(KEY_NEXT_AIRED, info.getNextAired());
        	newValues.put(KEY_POSTER, info.getPoster());
	        
	    	mDb.update(FAVORITES_TABLE, newValues, KEY_ID + " = ?", new String[]{String.valueOf(info.getSeriesId())});
    	}
    }
    
    /**
     * Reset all Next and Last aired dates
     */
    public void clearAiredDates(){
    	mDb.execSQL("UPDATE " + FAVORITES_TABLE + " SET " + KEY_LAST_AIRED + " = 0, " + KEY_NEXT_AIRED + " = 0");
    }

	public boolean isFavoriteSeries(Long id) {
		
		Cursor c = mDb.query(FAVORITES_TABLE, new String[]{KEY_ID}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null,null);

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



	public void truncateFavorites() {
				
	}


	public void truncateExcept(ArrayList<Long> favoritesList) {
		String whereString = buildWhereString(favoritesList);

		if (whereString.equals("")){
			mDb.execSQL("DELETE FROM " + FAVORITES_TABLE);
		}else{
			mDb.execSQL("DELETE FROM " + FAVORITES_TABLE + " WHERE " + KEY_ID + " NOT IN " + whereString);
		}
	}
	
	
	private String buildWhereString(ArrayList<Long> list){
		
		if (list == null || list.size() == 0){
			return "";
		}else{
			StringBuilder sb = new StringBuilder();
			
			sb.append("(");
			
			for(int i=0; i<list.size(); i++)
				if (i+1 == list.size())
					sb.append("'" + list.get(i) + "'");
				else
					sb.append("'" + list.get(i) + "', ");
			sb.append(")");
			
			return sb.toString();
		}
	}


    

}
