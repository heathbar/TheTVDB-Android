package com.heath_bar.tvdb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.Window;
import com.heath_bar.tvdb.adapters.SeriesAiredAdapter;
import com.heath_bar.tvdb.adapters.SeriesDbAdapter;

public class TheTVDBActivity extends SherlockListActivity  {
	
	
	private SeriesDbAdapter db;
	private Cursor cursor;
	private Cursor refreshCursor;
	private SeriesAiredAdapter adapter;
	private ResponseReceiver updateReceiver;
	private boolean showRefreshButton = true; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(false);
        setContentView(R.layout.favorites_list);

        // Setup the ListView header
        View header = getLayoutInflater().inflate(R.layout.text, null);
        TextView header_text = (TextView) header.findViewById(R.id.text);
        header_text.setText("Favorite Shows");        
        header_text.setTextSize(20);
        header_text.setTextColor(Color.WHITE);
        header_text.setBackgroundColor(getResources().getColor(R.color.tvdb_green));
        header_text.setTypeface(null, Typeface.BOLD);
        getListView().addHeaderView(header, null, false);
	}	
			
    
    // When the task is created, or the user returns, refresh to pick up any new favorites
    @Override
    protected void onResume(){
    	super.onResume();
    	RefreshFavoritesAsync();
	}
    
	private void RefreshFavoritesAsync(){
		
		// Hide refresh button and show Progress animation
		setSupportProgressBarIndeterminateVisibility(true);
		showRefreshButton = false;
		invalidateOptionsMenu();
				
		// Set the empty list text
		TextView emptyList = (TextView)findViewById(android.R.id.empty);
		emptyList.setText(getResources().getString(R.string.loading));
		
		// Get favorite shows from the database in an AsyncTask 
		new QueryDatabaseTask().execute();
		
		// Register for responses from the update service
		updateReceiver = new ResponseReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UpdateService.ACTION_UPDATE);
		filter.addAction(UpdateService.ACTION_COMPLETE);
        registerReceiver(updateReceiver, filter);
	}
	
	private class QueryDatabaseTask extends AsyncTask<Void, Void, Void>{
	
		@Override
		protected Void doInBackground(Void... params) {
			
			try {
		        db = new SeriesDbAdapter(getApplicationContext());
		        db.open();

		        // Get all of the favorite shows (sans aired dates)
		        cursor = db.fetchFavorites();
				
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
			
			// Apply the cursor to the ListView
			String[] from = new String[]{SeriesDbAdapter.KEY_TITLE, SeriesDbAdapter.KEY_LAST_AIRED, SeriesDbAdapter.KEY_NEXT_AIRED};
	        int[] to = new int[]{R.id.list_item_title, R.id.last_aired, R.id.next_aired};
	      
	        try{
		        adapter = new SeriesAiredAdapter(getApplicationContext(), R.layout.show_aired_row, cursor, from, to, 0, AppSettings.listBackgroundColors);
				setListAdapter(adapter);
				getListView().setOnItemClickListener(new ItemClickedListener());
				registerForContextMenu(getListView());
	        }catch (Exception e){
	        	if(AppSettings.LOG_ENABLED)
	        		Log.e("TheTVDBActivity","Failed to set the cursor");
	        	Toast.makeText(getApplicationContext(), "There was a problem loading your favorite shows from the database", Toast.LENGTH_SHORT).show();
	        }
	        
			// Launch the update service to lookup the aired dates in the background
			Intent updater = new Intent(getApplicationContext(), UpdateService.class);
			startService(updater);
		}
	}
	
	
	private class ResponseReceiver extends BroadcastReceiver {
		 
		@Override
		public void onReceive(Context context, Intent intent) {
			   
			if (intent.getAction().equals(UpdateService.ACTION_UPDATE)){
			
				// TODO: This shouldn't create a new cursor.. that seems wasteful.
				refreshCursor = db.fetchFavorites();
		        	
				String[] from = new String[]{SeriesDbAdapter.KEY_TITLE, SeriesDbAdapter.KEY_LAST_AIRED, SeriesDbAdapter.KEY_NEXT_AIRED};
		        int[] to = new int[]{R.id.list_item_title, R.id.last_aired, R.id.next_aired};
		        
		        try {
			        adapter = new SeriesAiredAdapter(getApplicationContext(), R.layout.show_aired_row, refreshCursor, from, to, 0, AppSettings.listBackgroundColors);
			        setListAdapter(adapter);
		        }catch(Exception e){}
		        
			} else if (intent.getAction().equals(UpdateService.ACTION_COMPLETE)){
				// Hide the progress animation and show the refresh button
				setSupportProgressBarIndeterminateVisibility(false);
				showRefreshButton = true;
				invalidateOptionsMenu();

				// Reset the empty list text
				TextView emptyList = (TextView)findViewById(android.R.id.empty);
				emptyList.setText(getResources().getString(R.string.empty_list_favorites));
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
	
		
	
	
	// ACTIONBAR MENU
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    	if (showRefreshButton){
	    	menu.add("Refresh")
	    	.setIcon(R.drawable.ic_refresh)
	    	.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					RefreshFavoritesAsync();
					return false;
				}
			})
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    	}
    	menu.add("Search")
            .setIcon(R.drawable.ic_search)
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					onSearchRequested();
					return false;
				}
			})
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        
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
}