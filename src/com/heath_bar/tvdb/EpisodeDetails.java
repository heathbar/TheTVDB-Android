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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.Window;
import com.heath_bar.tvdb.types.TvEpisode;
import com.heath_bar.tvdb.util.DateUtil;
import com.heath_bar.tvdb.util.NonUnderlinedClickableSpan;
import com.heath_bar.tvdb.xml.handlers.EpisodeHandler;


public class EpisodeDetails extends SherlockActivity {

	protected long episodeId;
	protected long seriesId;
	protected TvEpisode myEpisode = null;
	
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
		    	
		    	// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(true);
				new LoadEpisodeDetailsTask().execute(episodeId);
		    	
				ApplyPreferences();
    		}
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	private class LoadEpisodeDetailsTask extends AsyncTask<Long, Void, TvEpisode>{
		@Override
		protected TvEpisode doInBackground(Long... id) {
			
			try {
				// Lookup episode info
				EpisodeHandler episodeQuery = new EpisodeHandler(getApplicationContext());
				TvEpisode theEpisode = episodeQuery.getEpisode(id[0]);
				
				// Load the image while we're still in the background thread
				theEpisode.getImage().Load();
				
				return theEpisode; 
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(TvEpisode theEpisode){

			// Populate the activity with the data we just found
			PopulateStuff(theEpisode);
			
			// Hide the loading text
			findViewById(R.id.loading1).setVisibility(View.GONE);
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
	}
	
	public void PopulateStuff(TvEpisode theEpisode){
		
		// Set Title
		TextView textview = (TextView)findViewById(R.id.title);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theEpisode.getSeason() + "x" + String.format("%02d", theEpisode.getNumber()) + " " + theEpisode.getName());

		// Set Thumb
		if (theEpisode.getImage().getBitmap() == null || theEpisode.getImage().getUrl().equals("")){
			// do nothin
		} else {
			ImageView banner = (ImageView)findViewById(R.id.episode_thumb);
    		banner.setImageBitmap(theEpisode.getImage().getBitmap());
    		banner.setVisibility(View.VISIBLE);
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

	
	
	// Show the Search Button in the action bar
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
	
	// Apply Preferences
	private void ApplyPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
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
