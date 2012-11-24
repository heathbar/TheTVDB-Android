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

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.heath_bar.tvdb.data.adapters.TvSeriesListAdapter;
import com.heath_bar.tvdb.data.xmlhandlers.SeriesSearchHandler;
import com.heath_bar.tvdb.types.TvSeries;

public class SearchResults extends SherlockListActivity {
	
	private TvSeriesListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    setContentView(R.layout.search_results);
        
	    // Load Preferences
	    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
     	float textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
     	
     	
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	
	    	View header = getLayoutInflater().inflate(R.layout.text, null);

	    	TextView header_text = (TextView) header.findViewById(R.id.text);
	        header_text.setText("search results for: " + query);
	        header_text.setTextSize(textSize);
	        header_text.setTextColor(Color.WHITE);
	        header_text.setBackgroundColor(getResources().getColor(R.color.tvdb_green));
	        header_text.setTypeface(null, Typeface.BOLD);
			
	        getListView().addHeaderView(header, null, false);
	        
	        TextView empty = (TextView)findViewById(android.R.id.empty);
	        empty.setText(getResources().getString(R.string.loading));
	        empty.setTextSize(textSize);

			new SearchTask().execute(query);
	    }
	}
	
	
	// Class to load the search response asynchronously
	private class SearchTask extends AsyncTask<String, Void, ArrayList<TvSeries>>{
		@Override
		protected ArrayList<TvSeries> doInBackground(String... query) {
			
			try {
				// Search the tvdb API
				SeriesSearchHandler tvdbApiSearch = new SeriesSearchHandler(getApplicationContext());
				return tvdbApiSearch.searchSeries(query[0]);
				
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<TvSeries> results){
			
			// Setup the list adapter with the data from the web call
			adapter = new TvSeriesListAdapter(getApplicationContext(), R.layout.text_row_with_id, results, AppSettings.listBackgroundColors);
	        
	        setListAdapter(adapter);
	        getListView().setOnItemClickListener(new ItemClickedListener());
	        
	        // Replace the loading text with empty text in case no items were found
	        TextView empty = (TextView)findViewById(android.R.id.empty);
	        empty.setText(getResources().getString(R.string.empty_list));
		}
	}

    private class ItemClickedListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
        	
        	long seriesId = adapter.getItemId(position);            
        	Intent myIntent = new Intent(arg0.getContext(), SeriesOverview.class);
        	myIntent.putExtra("id", seriesId);
    		startActivityForResult(myIntent, 0);
        }
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("Search")
            .setIcon(R.drawable.ic_search)
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					onSearchRequested();
					return false;
				}
			})
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    // Home button moves back
    @Override
	public boolean onOptionsItemSelected(MenuItem item){
	     switch (item.getItemId()) {
	         case android.R.id.home:
	        	 finish();
	        	 return true;
	     }
	     return false;
	}
}
