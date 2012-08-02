package com.heath_bar.tvdb.adapters;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.heath_bar.tvdb.AppSettings;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, AppSettings.DATABASE_VERSION);
    }

	private static final String DATABASE_NAME = "tvdb_data";

	@Override
	public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE =
                "create table " + SeriesDbAdapter.FAVORITES_TABLE + " ("
            	+ SeriesDbAdapter.KEY_ID + " integer primary key, "
                + SeriesDbAdapter.KEY_TITLE + " text not null, "
                + SeriesDbAdapter.KEY_LAST_AIRED + " integer, "
                + SeriesDbAdapter.KEY_NEXT_AIRED + " integer);";
            db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
