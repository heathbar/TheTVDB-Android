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
import java.util.HashMap;
import java.util.Map;

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
import com.heath_bar.lazylistadapter.LazyBitmapAdapter;
import com.heath_bar.tvdb.types.WebImage;
import com.heath_bar.tvdb.xml.handlers.BannerHandler;

public class BannerListing extends SherlockListActivity {
	
	private long seriesId;
	private String seriesName;
	//private ArrayList<WebImage> imageList;
	private Map<String, String> idUrlList;
	private LazyBitmapAdapter adapter;
	private long fileCacheMaxSize = 10485760;	// 10MB 

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
		    	
		    	// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(true);
				new LoadBannerThumbsTask().execute(String.valueOf(seriesId));
		    	
				// Apply Preferences
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				float textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
				fileCacheMaxSize = Long.parseLong(settings.getString("fileCacheMaxSize", "10000000"));
		    	
				TextView textview = (TextView)findViewById(android.R.id.empty);
				textview.setTextSize(textSize);
				textview.setText(getResources().getString(R.string.loading));
    		}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
		
		
	private class LoadBannerThumbsTask extends AsyncTask<String, Void, Map<String, String>>{
		@Override
		protected Map<String, String> doInBackground(String... series) {
			
			try {
				// get the urls to the thumbnails
				BannerHandler bannerQuery = new BannerHandler();
				ArrayList<WebImage> banners = bannerQuery.getImageList(series[0]);
				idUrlList = new HashMap<String, String>();
				
				for (int i=0; i<banners.size(); i++)
					idUrlList.put(banners.get(i).getId(), banners.get(i).getUrl());
				
				return idUrlList;
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Map<String, String> idUrlList){
			SetupAdapter(idUrlList);
			setSupportProgressBarIndeterminateVisibility(false);
		}	
	}
	
	protected void SetupAdapter(Map<String, String> idUrlList){
		try {
			adapter = new LazyBitmapAdapter(this, idUrlList, R.layout.image, R.id.image);
			adapter.setFileCacheMaxSize(fileCacheMaxSize);
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

	    	String imageId = adapter.getItemIdString(position);
	    	
        	Intent myIntent = new Intent(arg0.getContext(), BannerViewer.class);
        	myIntent.putExtra("imageId", imageId);
        	myIntent.putExtra("url", idUrlList.get(imageId));
        	myIntent.putExtra("seriesName", seriesName);
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
