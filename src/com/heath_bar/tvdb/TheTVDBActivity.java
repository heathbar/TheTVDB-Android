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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.internal.widget.IcsListPopupWindow;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.Window;
import com.heath_bar.lazylistadapter.BitmapFileCache;
import com.heath_bar.tvdb.adapters.PopupMenuAdapter;
import com.heath_bar.tvdb.adapters.SeriesAiredListAdapter;
import com.heath_bar.tvdb.adapters.SeriesDbAdapter;

public class TheTVDBActivity extends SherlockListActivity implements OnItemClickListener   {
	
	
	private SeriesDbAdapter db;							// db where favorites are stored... should replace this with an API
	private Cursor cursor;								// cursor to hold the favorites from the db
	private Cursor refreshCursor;						// replacement cursor
	private SeriesAiredListAdapter adapter;				// adapter to lookup air times
	private ResponseReceiver updateReceiver;			// listener for updates from the adapter
	private Intent airedDatesUpdater;
	private boolean isRefreshing = false;
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	
	private IcsListPopupWindow sortPopupMenu;			// define a popup menu for the sort button to show
	private String sortBy = SeriesDbAdapter.KEY_TITLE;  // Sort by show title by default
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(false);
        setContentView(R.layout.favorites_list);

        if (airedDatesUpdater == null)
        	airedDatesUpdater = new Intent(getApplicationContext(), UpdateService.class);
        
        // Setup the ListView header
        View header = getLayoutInflater().inflate(R.layout.favorites_header, null);
        getListView().addHeaderView(header, null, false);

        // Setup the sort menu
        PopupMenuAdapter adapter = new PopupMenuAdapter(this, com.actionbarsherlock.R.layout.sherlock_spinner_dropdown_item, getResources().getStringArray(R.array.sortOptions));
        sortPopupMenu = new IcsListPopupWindow(this);
        sortPopupMenu.setAdapter(adapter);
        sortPopupMenu.setModal(true);
        sortPopupMenu.setOnItemClickListener(this);
        
        
        
        // Listen for Preference changes
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
    	  	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        	  	if (key.equals("cacheSize")){
        	  		long cacheSize = prefs.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
        	  		BitmapFileCache fileCache = new BitmapFileCache(getApplicationContext(), cacheSize);
        	  		fileCache.trimCache();
        	  	}else{
        	  		ApplyPreferences();
        	  	}
    	  	}
    	};
    	
    	// Apply Preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(prefListener);
        ApplyPreferences();
	}	
			
    
    private void ApplyPreferences() {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	float textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
        TextView textview = (TextView)findViewById(android.R.id.empty);
        textview.setTextSize(textSize);	
        
        View header = getLayoutInflater().inflate(R.layout.text, null);
        TextView header_text = (TextView) header.findViewById(R.id.text);
        header_text.setTextSize(textSize*1.1f);
	}


	// When the task is created, or the user returns, refresh to pick up any new favorites
    @Override
    protected void onResume(){
    	super.onResume();
    	RefreshFavoritesAsync();
	}
    
	private void RefreshFavoritesAsync(){
		
		if (isRefreshing)
			return;
		else 
			isRefreshing = true;				
				
		// Hide refresh button and show Progress animation
		setSupportProgressBarIndeterminateVisibility(true);
				
		// Set the empty list text
		TextView emptyList = (TextView)findViewById(android.R.id.empty);
		emptyList.setText(getResources().getString(R.string.loading));
		
		// Get favorite shows from the database in an AsyncTask 
		new QueryDatabaseTask().execute();
		
		// Register for responses from the update service
		if (updateReceiver != null)
			unregisterReceiver(updateReceiver);
		
		updateReceiver = new ResponseReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UpdateService.ACTION_UPDATE);
		filter.addAction(UpdateService.ACTION_COMPLETE);
		filter.addAction(UpdateService.CONNECT_EXCEPTION);
        registerReceiver(updateReceiver, filter);
        
        // Launch the update service to lookup the aired dates in the background
     	startService(airedDatesUpdater);
	}
	
	private class QueryDatabaseTask extends AsyncTask<Void, Void, Cursor>{
	
		@Override
		protected Cursor doInBackground(Void... params) {
			
			try {
		        db = new SeriesDbAdapter(getApplicationContext());
		        db.open();

		        // Get all of the favorite shows (sans aired dates)
		        return db.fetchFavorites(sortBy);
		        
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Cursor c){
			
			cursor = c;
			
			// Apply the cursor to the ListView
			String[] from = new String[]{SeriesDbAdapter.KEY_TITLE, SeriesDbAdapter.KEY_LAST_AIRED, SeriesDbAdapter.KEY_NEXT_AIRED};
	        int[] to = new int[]{R.id.list_item_title, R.id.last_aired, R.id.next_aired};
	      
	        try{
		        adapter = new SeriesAiredListAdapter(getApplicationContext(), R.layout.show_aired_row, cursor, from, to, 0, AppSettings.listBackgroundColors);
				setListAdapter(adapter);
				getListView().setOnItemClickListener(new ItemClickedListener());
				registerForContextMenu(getListView());
	        }catch (Exception e){
	        	if(AppSettings.LOG_ENABLED)
	        		Log.e("TheTVDBActivity","Failed to set the cursor");
	        	Toast.makeText(getApplicationContext(), "There was a problem loading your favorite shows from the database", Toast.LENGTH_SHORT).show();
	        }			
		}
	}
	
	
	private class ResponseReceiver extends BroadcastReceiver {
		 
		@Override
		public void onReceive(Context context, Intent intent) {
			   
			if (intent.getAction().equals(UpdateService.ACTION_UPDATE)){
			
				refreshCursor = db.fetchFavorites(sortBy);
				
		        try {
			        adapter.changeCursor(refreshCursor);
			        //setListAdapter(adapter);
		        }catch(Exception e){}
		        
			} else if (intent.getAction().equals(UpdateService.ACTION_COMPLETE)){
				// Hide the progress animation and show the refresh button
				setSupportProgressBarIndeterminateVisibility(false);
				
				// Reset the empty list text
				TextView emptyList = (TextView)findViewById(android.R.id.empty);
				emptyList.setText(getResources().getString(R.string.empty_list_favorites));
				
				isRefreshing = false;
				
			} else if (intent.getAction().equals(UpdateService.CONNECT_EXCEPTION)){
				Toast.makeText(context, "There was a problem reaching thetvdb.com. Perhaps the site is down", Toast.LENGTH_SHORT).show();
				isRefreshing = false;
			}
		}
	}
	
	// Handle Clicks
	private class ItemClickedListener implements OnItemClickListener {
		
	    @Override
	    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
	    	
        	long seriesId = adapter.getItemId(position-1);            
        	Intent myIntent = new Intent(arg0.getContext(), SeriesOverview.class);
        	myIntent.putExtra("id", seriesId);
    		startActivityForResult(myIntent, 0);
	    }
	}
		
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == android.R.id.list) {
        	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        	Cursor c = (Cursor)adapter.getItem(info.position-1);
            
            menu.setHeaderTitle(c.getString(c.getColumnIndex("title")));
            menu.add("Remove");
            c = null;
        }
    }
	
	@Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		boolean success = db.removeFavoriteSeries(info.id);
		RefreshFavoritesAsync();
		if (success)
			Toast.makeText(this, "The show has been removed from your favorites.", Toast.LENGTH_SHORT).show();
		else 
			Toast.makeText(this, "Something bad happened: nothing was deleted.", Toast.LENGTH_SHORT).show();
		
		
        return true;
    }
	
	@SuppressWarnings("deprecation")
	public void showSortPopupMenu(View v){
		sortPopupMenu.setContentWidth(getWindowManager().getDefaultDisplay().getWidth()/2);
		sortPopupMenu.setAnchorView(v);
		sortPopupMenu.show();
	}
	
	
	// ACTIONBAR MENU
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    	
		menu.add("Refresh")
	    	.setIcon(R.drawable.ic_refresh)
	    	.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					RefreshFavoritesAsync();
					return false;
				}
			})
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add("Search")
			.setIcon(R.drawable.ic_search)
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					onSearchRequested();
					return false;
				}
			})
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
    	
    	
    	menu.add("Preferences")
    		.setIcon(R.drawable.ic_prefs)
    		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					Intent i = new Intent(getApplicationContext(), Preferences.class);
					startActivity(i);
					return false;
				}
			})
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    	
    	return true;
    }
    

	
	/** Close the database, we're done. */
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (cursor != null)
	    	cursor.close();
	    if (refreshCursor != null)
	    	refreshCursor.close();
	    if (db != null)
            db.close();
	    
	    unregisterReceiver(updateReceiver);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position){
		case 0:
			sortBy = SeriesDbAdapter.KEY_TITLE;
			break;
		case 1:
			sortBy = SeriesDbAdapter.KEY_NEXT_AIRED;
			break;
		case 2:
			sortBy = SeriesDbAdapter.KEY_LAST_AIRED;
			break;
		}
		sortPopupMenu.dismiss();
		new QueryDatabaseTask().execute();		
	}
}