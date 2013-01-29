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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.heath_bar.tvdb.data.adapters.lazylist.LazyBitmapAdapter;
import com.heath_bar.tvdb.data.adapters.lazylist.WebImage;
import com.heath_bar.tvdb.data.adapters.lazylist.WebImageList;
import com.heath_bar.tvdb.data.xmlhandlers.BannerHandler;

public class BannerListing extends SherlockListActivity {
	
	private long seriesId;
	private String seriesName;
	private LazyBitmapAdapter adapter;
	private long cacheSize; 

	// OnCreate... display essentially just a loading screen while we call LoadBannerListTask in the background
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.banner_listing);

		try {
			
			Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	seriesId = getIntent().getLongExtra("seriesId", 0);
		    	seriesName = getIntent().getStringExtra("seriesName");
		    	
				// Set title
				getSupportActionBar().setTitle(seriesName);
		    	
		    	// Apply Preferences
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				cacheSize = settings.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
				float textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
						    	
				TextView textview = (TextView)findViewById(android.R.id.empty);
				textview.setTextSize(textSize);
				textview.setText(getResources().getString(R.string.loading));
				
				// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(true);
				new LoadBannerThumbsTask().execute(String.valueOf(seriesId));
		    	
    		}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
		
		
	private class LoadBannerThumbsTask extends AsyncTask<String, Void, WebImageList>{
		@Override
		protected WebImageList doInBackground(String... series) {
			
			try {
				// get the urls to the thumbnails
				BannerHandler bannerQuery = new BannerHandler();
				WebImageList banners = bannerQuery.getImageList(series[0]);
				
				return banners;
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(WebImageList banners){
			SetupAdapter(banners);
			setSupportProgressBarIndeterminateVisibility(false);
		}	
	}
	
	protected void SetupAdapter(WebImageList banners){
		try {
			adapter = new LazyBitmapAdapter(this, banners, R.layout.image, R.id.image);
			adapter.setFileCacheMaxSize(cacheSize);
			setListAdapter(adapter);
			getListView().setOnItemClickListener(new ItemClickedListener());
		}catch (Exception e){
			if (AppSettings.LOG_ENABLED)
				Log.e("BannerViewer", e.getMessage());
		}
	}
	
	
	// Handle Clicks
	private class ItemClickedListener implements OnItemClickListener {
		
	    @Override
	    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {

	    	WebImage image = (WebImage)adapter.getItem(position);
	    	
        	Intent myIntent = new Intent(arg0.getContext(), ImageViewer.class);
        	myIntent.putExtra("imageTitle", seriesName);
        	myIntent.putExtra("imageId", image.getId());
        	myIntent.putExtra("imageUrl", image.getUrl());
        	adapter.clearMemoryCache();
    		startActivity(myIntent);
	    }
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
