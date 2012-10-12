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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;
import com.heath_bar.lazylistadapter.BitmapFileCache;
import com.heath_bar.lazylistadapter.BitmapWebUtil;
import com.heath_bar.tvdb.types.TvEpisode;
import com.heath_bar.tvdb.util.DateUtil;
import com.heath_bar.tvdb.util.NonUnderlinedClickableSpan;
import com.heath_bar.tvdb.util.ShareUtil;
import com.heath_bar.tvdb.xml.handlers.EpisodeHandler;


public class EpisodeDetails extends SherlockActivity {

	protected long episodeId;
	protected long seriesId;
	protected String imageId;
	protected TvEpisode myEpisode = null;
	protected long cacheSize;
	
	// OnCreate... display essentially just a loading screen while we call LoadInfoTask in the background
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.episode_details);
        	
		try {
			
			Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	episodeId = getIntent().getLongExtra("id", 0);
		    	seriesId = getIntent().getLongExtra("seriesId", 0);
		    	String seriesName = getIntent().getStringExtra("seriesName");
		    			    	
				// Set title
				getSupportActionBar().setTitle(seriesName);
		    	
				ApplyPreferences();
				
				// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(true);
				new LoadEpisodeDetailsTask().execute(episodeId);
		    	
    		}
		}catch (Exception e){
			e.printStackTrace();
			Toast.makeText(this, "Failed to load episode details", Toast.LENGTH_LONG).show();
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
	}
	
	private class LoadEpisodeDetailsTask extends AsyncTask<Long, Void, TvEpisode>{
		@Override
		protected TvEpisode doInBackground(Long... id) {
			
			try {
				// Lookup episode info
				EpisodeHandler episodeQuery = new EpisodeHandler(getApplicationContext());
				TvEpisode theEpisode = episodeQuery.getEpisode(id[0]);
				
				// If anything explodes while trying to get the image, don't let it stop us from returning theEpisode.
				try {
					Bitmap bitmap;				
			    	BitmapFileCache fileCache = new BitmapFileCache(getApplicationContext(), cacheSize);
			    	
			    	
			    	if (fileCache.contains(theEpisode.getImage().getId())){
			    		
			    		bitmap = fileCache.get(theEpisode.getImage().getId());
	
			    	}else{
			    	
			    		BitmapWebUtil web = new BitmapWebUtil(getApplicationContext());
			    		bitmap = web.downloadBitmap(theEpisode.getImage().getUrl());
						fileCache.put(theEpisode.getImage().getId(), bitmap);
					}
			    	theEpisode.getImage().setBitmap(bitmap);
				
				} catch (NullPointerException e){
					e.printStackTrace();
				}
			    	
				return theEpisode; 
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(TvEpisode theEpisode){

			// Store the episode info for sharing later
			myEpisode = theEpisode;
			
			// Populate the activity with the data we just found
			PopulateStuff(theEpisode);
			
			// Hide the loading text
			findViewById(R.id.loading1).setVisibility(View.GONE);
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
	}
	
	/** Populate the GUI with the data we've found */
	public void PopulateStuff(TvEpisode theEpisode){
		
		// Set Title
		TextView textview = (TextView)findViewById(R.id.title);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theEpisode.getSeason() + "x" + String.format("%02d", theEpisode.getNumber()) + " " + theEpisode.getName());

		// Set Thumb
		if (theEpisode.getImage() != null && (theEpisode.getImage().getBitmap() == null || theEpisode.getImage().getUrl().equals(""))){
			// do nothin
		} else {
			imageId = theEpisode.getImage().getId();			
			ImageButton banner = (ImageButton)findViewById(R.id.episode_thumb);
    		banner.setImageBitmap(theEpisode.getImage().getBitmap());
    		banner.setVisibility(View.VISIBLE);
    		banner.setOnClickListener(new View.OnClickListener() {   
    			public void onClick(View v) { 
					shareImage();
    			}
			});
		}

		// Overview
		textview = (TextView)findViewById(R.id.overview_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.overview);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theEpisode.getOverview());
		
		// Director
		textview = (TextView)findViewById(R.id.director_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.director);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theEpisode.getDirector());
		
		// Writer
		textview = (TextView)findViewById(R.id.writer_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.writer);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theEpisode.getWriter());
				

		// Rating
		textview = (TextView)findViewById(R.id.rating_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.rating);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theEpisode.getRating() + " / 10");		


		// First Aired
		textview = (TextView)findViewById(R.id.first_aired_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.first_aired);
		textview.setVisibility(View.VISIBLE);
		textview.setText(DateUtil.toString(theEpisode.getAirDate()));		


		// Guest Stars
		textview = (TextView)findViewById(R.id.guest_stars_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.guest_stars);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theEpisode.getGuestStars());
		
		textview = (TextView)findViewById(R.id.imdb_link);
		textview.setVisibility(View.VISIBLE);
		
		final String imdbId = theEpisode.getIMDB();
		if (imdbId != ""){
			SpannableStringBuilder ssb = new SpannableStringBuilder(getResources().getString(R.string.imdb));
			ssb.setSpan(new NonUnderlinedClickableSpan(getResources().getString(R.string.imdb)) {
				@Override
				public void onClick(View v){
					Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + imdbId));
					startActivity(myIntent);	        		
				}
			}, 0, ssb.length(), 0);
			
			ssb.setSpan(new TextAppearanceSpan(this, R.style.episode_link), 0, ssb.length(), 0);	// Set the style of the text
			textview.setText(ssb, BufferType.SPANNABLE);
			textview.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	
	
	
	/** Launch the share menu for the episode image */
	public void shareImage(){
		try{
			Intent i = ShareUtil.makeIntent(getApplicationContext(), myEpisode.getImage().getId());
			if (i != null)
				startActivity(i);
		}catch (Exception e){
			Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	// ACTION BAR MENU ///////////////////////////////////////
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {

		// SHARE Sub Menu /////////////////////////
		
		SubMenu subMenu1 = menu.addSubMenu("Share");
		subMenu1
    		.add("TheTVDB Link")
    		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    			@Override
    			public boolean onMenuItemClick(MenuItem item) {
    				try{
    					startActivity(ShareUtil.makeIntent(getApplicationContext(), myEpisode.getName(), "http://thetvdb.com/?tab=episode&seriesid=" + seriesId + "&id=" + episodeId));				
    				}catch (Exception e){
    					Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
    				}
    				return false;
    			}
    		});
			subMenu1
			.add("IMDB Link")
			.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					try{
						startActivity(ShareUtil.makeIntent(getApplicationContext(), myEpisode.getName(), "http://www.imdb.com/title/" + myEpisode.getIMDB()));
					}catch (Exception e){
						Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
					}
					return false;
				}
			});
			subMenu1
			.add("Episode Image")
			.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					shareImage();
					return false;
				}
			});
        
        MenuItem subMenu1Item = subMenu1.getItem();
        subMenu1Item.setIcon(R.drawable.ic_share);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        		
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
	
	// Apply Preferences
	private void ApplyPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		cacheSize = settings.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
    	float textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
    	
		TextView textview = (TextView)findViewById(R.id.loading1);
		textview.setTextSize(textSize);
		
		textview = (TextView)findViewById(R.id.title);
		textview.setTextSize(textSize*1.4f);

		textview = (TextView)findViewById(R.id.overview_header);
		textview.setTextSize(textSize*1.3f);
		
		textview = (TextView)findViewById(R.id.overview);
		textview.setTextSize(textSize);
		
		textview = (TextView)findViewById(R.id.director_header);
		textview.setTextSize(textSize*1.3f);
		
		textview = (TextView)findViewById(R.id.director);
		textview.setTextSize(textSize);
		
		textview = (TextView)findViewById(R.id.writer_header);
		textview.setTextSize(textSize*1.3f);
		
		textview = (TextView)findViewById(R.id.writer);
		textview.setTextSize(textSize);
		
		textview = (TextView)findViewById(R.id.rating_header);
		textview.setTextSize(textSize*1.3f);
		
		textview = (TextView)findViewById(R.id.rating);
		textview.setTextSize(textSize);
		
		textview = (TextView)findViewById(R.id.first_aired_header);
		textview.setTextSize(textSize*1.3f);
		
		textview = (TextView)findViewById(R.id.first_aired);
		textview.setTextSize(textSize);
		
		textview = (TextView)findViewById(R.id.guest_stars_header);
		textview.setTextSize(textSize*1.3f);
		
		textview = (TextView)findViewById(R.id.guest_stars);
		textview.setTextSize(textSize);
	}
}
