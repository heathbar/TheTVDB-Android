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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;
import com.heath_bar.tvdb.data.FavoritesData;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapFileCache;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapWebUtil;
import com.heath_bar.tvdb.data.xmlhandlers.EpisodeListHandler;
import com.heath_bar.tvdb.data.xmlhandlers.GetRatingHandler;
import com.heath_bar.tvdb.data.xmlhandlers.SeriesDetailsHandler;
import com.heath_bar.tvdb.data.xmlhandlers.SetRatingHandler;
import com.heath_bar.tvdb.types.FavoriteSeriesInfo;
import com.heath_bar.tvdb.types.Rating;
import com.heath_bar.tvdb.types.TvEpisode;
import com.heath_bar.tvdb.types.TvEpisodeList;
import com.heath_bar.tvdb.types.TvSeries;
import com.heath_bar.tvdb.types.exceptions.RatingNotFoundException;
import com.heath_bar.tvdb.util.DateUtil;
import com.heath_bar.tvdb.util.DialogBuilder;
import com.heath_bar.tvdb.util.NonUnderlinedClickableSpan;
import com.heath_bar.tvdb.util.ShareUtil;
import com.heath_bar.tvdb.util.StringUtil;


public class SeriesOverview extends SherlockFragmentActivity implements RatingFragment.NoticeDialogListener {

	
	protected long seriesId;
	protected TvSeries seriesInfo;
	protected TvEpisodeList episodeList;
	protected float textSize;
	protected long cacheSize;
	protected String userAccountId;
	protected Boolean isFavorite = null;
	protected boolean useNiceDates;
	
	// OnCreate... display essentially just a loading screen while we call LoadInfoTask in the background
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.series_overview);
        	
		try {
			
			Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	seriesId = getIntent().getLongExtra("id", 0);
				
				ApplyPreferences();
				
		    	// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(true);
				new LoadInfoTask().execute(seriesId);
				
    		}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	// Class to load the basic series info asynchronously
	private class LoadInfoTask extends AsyncTask<Long, Void, TvSeries>{
		@Override
		protected TvSeries doInBackground(Long... id) {
			
			try {
				// Lookup basic series info
				SeriesDetailsHandler infoQuery = new SeriesDetailsHandler(getApplicationContext());
	    		seriesInfo = infoQuery.getInfo(id[0]);
	    			    		
	    		Bitmap bitmap;
		    	BitmapFileCache fileCache = new BitmapFileCache(getApplicationContext(), cacheSize);
		    	
		    	if (fileCache.contains(seriesInfo.getImage().getId())){
		    		
		    		bitmap = fileCache.get(seriesInfo.getImage().getId());

		    	}else{
		    	
		    		BitmapWebUtil web = new BitmapWebUtil(getApplicationContext());
		    		bitmap = web.downloadBitmap(seriesInfo.getImage().getUrl());
					fileCache.put(seriesInfo.getImage().getId(), bitmap);
				}
				seriesInfo.getImage().setBitmap(bitmap);
				
				return seriesInfo;

	    		
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(TvSeries info){
			// Populate the activity with the data we just loaded
			PopulateStuff(info);
			
			// Hide the first loading text, show the second
			findViewById(R.id.loading1).setVisibility(View.GONE);
			findViewById(R.id.loading2).setVisibility(View.VISIBLE);
			
			
			
			// Load Rating
			if (!userAccountId.equals(""))
				new LoadRatingTask().execute();
			else
				setUserRatingTextView(0);
			
			// Load episodes 
			new LoadEpisodesTask().execute();
		}
		
	}
	/** Populate the interface with the data pulled from the webz */
	private void PopulateStuff(TvSeries seriesInfo){
		
		if (seriesInfo == null)
		{
			Toast.makeText(getApplicationContext(), "Something bad happened. No data was found.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		
		// Set title
		getSupportActionBar().setTitle(seriesInfo.getName());
				
		// redraw the options menu with the correct icon
		isFavorite = seriesInfo.isFavorite(getApplicationContext());
		invalidateOptionsMenu();
		
		// Set the banner
		final String imageTitle = seriesInfo.getName();
		final String imageId = seriesInfo.getImage().getId();
		final String imageUrl = seriesInfo.getImage().getUrl();
		
		ImageView imageView = (ImageView)findViewById(R.id.series_banner);
		imageView.setImageBitmap(seriesInfo.getImage().getBitmap());
		imageView.setVisibility(View.VISIBLE);
		final String seriesName = seriesInfo.getName();
		
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(), ImageViewer.class);
				myIntent.putExtra("imageTitle", imageTitle);
				myIntent.putExtra("imageId", imageId);
				myIntent.putExtra("imageUrl", imageUrl);
	        	startActivity(myIntent);
			}
		});
		
		
		
		// Set the banner link
		TextView textview = (TextView)findViewById(R.id.banner_listing_link);
		textview.setTextColor(getResources().getColor(R.color.tvdb_green));
		textview.setVisibility(View.VISIBLE);
		textview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), BannerListing.class);
				i.putExtra("seriesId", seriesId);
				i.putExtra("seriesName", seriesName);
				startActivity(i);
			}
		});
		
		// Set air info
		textview = (TextView)findViewById(R.id.airs_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.last_episode);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.next_episode);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)findViewById(R.id.series_air_info);
		StringBuffer sb = new StringBuffer();
		sb.append(seriesInfo.getAirDay());
		if (!seriesInfo.getAirTime().equals(""))
			sb.append(" at " + seriesInfo.getAirTime());
		if (!seriesInfo.getNetwork().equals(""))
			sb.append(" on " + seriesInfo.getNetwork());
		sb.append("*");
		
		SpannableString airedText = new SpannableString(sb.toString());

		NonUnderlinedClickableSpan clickableSpan = new NonUnderlinedClickableSpan() {  
	        @Override  
	        public void onClick(View view) { 
	        	DialogBuilder.InformationalDialog(SeriesOverview.this, "Disclaimer", "All TV show information is maintained by users on thetvdb website. \n\nTimes are typically listed in the timezone of the network that airs them.\n\nCorrections can be made at http://thetvdb.com").show();
	        }  
	    };
	    int start = airedText.length()-1;
		int end = airedText.length();
		airedText.setSpan(clickableSpan, start, end, 0);
		airedText.setSpan(new TextAppearanceSpan(this, R.style.episode_link), start, end, 0);
		airedText.setSpan(new AbsoluteSizeSpan((int)textSize, true), 0, airedText.length(), 0);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		textview.setText(airedText, BufferType.SPANNABLE);
		textview.setVisibility(View.VISIBLE);
		
		// Set actors
		textview = (TextView)findViewById(R.id.starring);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)findViewById(R.id.series_actors);
		textview.setVisibility(View.VISIBLE);
				
		SpannableStringBuilder text = tagsBuilder(seriesInfo.getActors(), "|");
		textview.setText(text, BufferType.SPANNABLE);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		
		// Set rating
		textview = (TextView)findViewById(R.id.rating_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)findViewById(R.id.rating);
		textview.setText(seriesInfo.getRating() + " / 10");
		textview.setVisibility(View.VISIBLE);
		
		
		// Set genre
		textview = (TextView)findViewById(R.id.genre_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)findViewById(R.id.genre);
		textview.setText(StringUtil.commafy(seriesInfo.getGenre()));
		textview.setVisibility(View.VISIBLE);
		
		
		// Set runtime
		textview = (TextView)findViewById(R.id.runtime_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)findViewById(R.id.runtime);
		textview.setText(seriesInfo.getRuntime() + " minutes");
		textview.setVisibility(View.VISIBLE);
		
		
		// Set overview
		textview = (TextView)findViewById(R.id.overview_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)findViewById(R.id.overview);
		textview.setText(seriesInfo.getOverview());
		textview.setVisibility(View.VISIBLE);
		
		// Show Seasons header
		textview = (TextView)findViewById(R.id.seasons_header);
		textview.setVisibility(View.VISIBLE);
		
		// IMDB Link
		textview = (TextView)findViewById(R.id.imdb_link);
		textview.setVisibility(View.VISIBLE);
		
		final String imdbId = seriesInfo.getIMDB();
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
	
	// Make the clickable span for the actors
	private SpannableStringBuilder tagsBuilder(String text, String delim){
		SpannableStringBuilder builtTags = new SpannableStringBuilder();
		int start = 0;
		int end = 0;
		
		// if no text, break early
		if (text.length() == 0)
			return builtTags;
				
		// If the string starts with delim, remove the first delim
		if (text.substring(0, 1).equals(delim))
			text = text.substring(1);
		
		do {
			start = 0;
			end = text.indexOf(delim, 0);
			
			try {
				if (start < end) {
					final Context ctx = getApplicationContext();
					String targetString = text.substring(start, end);
					SpannableStringBuilder ssb = new SpannableStringBuilder(targetString);
					ssb.setSpan(new NonUnderlinedClickableSpan(targetString) {
						@Override
						public void onClick(View v){
							Intent myIntent = new Intent(ctx, ActorDetails.class);
				        	myIntent.putExtra("ActorName", tag);
				        	myIntent.putExtra("seriesId", seriesId);
				        	myIntent.putExtra("seriesName", seriesInfo.getName());
				    		startActivityForResult(myIntent, 0);	
						}
					}, start, end, 0);
					ssb.setSpan(new TextAppearanceSpan(this, R.style.episode_link), 0, ssb.length(), 0);	// Set the style of the text
					//ssb.setSpan(new AbsoluteSizeSpan((int)textSize, true), 0, ssb.length(), 0);				// Override the text size with the user's preference
					builtTags.append(ssb);
					if (text.substring(end + 1).indexOf(delim) >= 0)
						builtTags.append(", ");
					text = text.substring(end + 1);
				}
			} catch (IndexOutOfBoundsException e){}
		} while (start < end);

		return builtTags;
	}
	
	
	
	
	// Load the episode info asynchronously
	private class LoadEpisodesTask extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				// Lookup Season/Episode listing
	    		EpisodeListHandler episodeHandler = new EpisodeListHandler(getApplicationContext());
	    		episodeList = episodeHandler.getEpisodes(seriesId);
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
			 
			// Populate the next/last aired text views
			PopulateStuffPartTwo();
			
    		// Append the Season info at the bottom of the View
    		ArrayList<Integer> seasonList = episodeList.getSeasonList();
    		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.series_overview_linear_layout);
    		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		for (int i=0; i<seasonList.size(); i++) {

    			int seasonNo = seasonList.get(i);
    			
    			View seasonRow = inflater.inflate(R.layout.season_row, mainLayout, false);
    			seasonRow.setBackgroundColor(AppSettings.listBackgroundColors[i % AppSettings.listBackgroundColors.length]);
    			
    			TextView text = (TextView)seasonRow.findViewById(R.id.season_text);
    			//text.setId(seasonNo);
    			text.setTextSize(textSize*1.6f);

    			if (seasonNo == 0)
    				text.setText("Specials");
    			else
    				text.setText("Season " + seasonNo);
    			
    			seasonRow.setId(seasonNo);
    			
    			seasonRow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View seasonRow) {
						
						ShowHideEpisodes(seasonRow);
					}
				});
    			
    			mainLayout.addView(seasonRow);
    			
    		}

    		// Hide the progress animation and loading text
			setSupportProgressBarIndeterminateVisibility(false);
			findViewById(R.id.loading2).setVisibility(View.GONE);
		}
	}
	
	/** Populate the GUI with the episode information */
	private void PopulateStuffPartTwo(){
		
		// Populate the next/last episodes
		TvEpisode last = episodeList.getLastAired();
		TvEpisode next = episodeList.getNextAired();
		
		SpannableString text = null;		
		
		TextView richTextView = (TextView)findViewById(R.id.last_episode);
		
		if (last == null){
			 text = new SpannableString("Last Episode: unknown");
		}else{
		
			String dateString = (useNiceDates) ? DateUtil.toNiceString(DateUtil.toString(last.getAirDate())) : DateUtil.toString(last.getAirDate());
			text = new SpannableString("Last Episode: " + last.getName() + " (" + dateString + ")");

			NonUnderlinedClickableSpan clickableSpan = new NonUnderlinedClickableSpan() {  
		        @Override  
		        public void onClick(View view) { 
		            episodeListener.onClick(view);
		        }  
		    };
		    int start = 14;
			int end = start + last.getName().length();
		    text.setSpan(clickableSpan, start, end, 0);
		    text.setSpan(new TextAppearanceSpan(this, R.style.episode_link), start, end, 0);
		    text.setSpan(new AbsoluteSizeSpan((int)textSize, true), 0, text.length(), 0);
		    richTextView.setId(last.getId());
			richTextView.setMovementMethod(LinkMovementMethod.getInstance());
		}
		richTextView.setText(text, BufferType.SPANNABLE);
		
		
		text = null;
		richTextView = (TextView)findViewById(R.id.next_episode);

		if (seriesInfo != null && seriesInfo.getStatus() != null && !seriesInfo.getStatus().equals("Ended")){
			if (next == null){
				 text = new SpannableString("Next Episode: unknown");
			}else{
			
				String dateString = (useNiceDates) ? DateUtil.toNiceString(DateUtil.toString(next.getAirDate())) : DateUtil.toString(next.getAirDate());
				text = new SpannableString("Next Episode: " + next.getName() + " (" + dateString + ")");
	
				NonUnderlinedClickableSpan clickableSpan = new NonUnderlinedClickableSpan() {  
			        @Override  
			        public void onClick(View view) { 
			        	episodeListener.onClick(view);  
			        }  
			    };
			    int start = 14;
				int end = start + next.getName().length();
			    text.setSpan(clickableSpan, start, end, 0);
			    text.setSpan(new TextAppearanceSpan(this, R.style.episode_link), start, end, 0);
			    text.setSpan(new AbsoluteSizeSpan((int)textSize, true), 0, text.length(), 0);
				richTextView.setId(next.getId());
				richTextView.setMovementMethod(LinkMovementMethod.getInstance());
			}
			richTextView.setText(text, BufferType.SPANNABLE);
		}
	}
	
	
	// Dynamically add/remove the views for each episode
	protected void ShowHideEpisodes(View seasonRow) {
		
		// Get the linear layout that we will be adding/removing the episodes to/from
		LinearLayout epLinearLayout = (LinearLayout)seasonRow;
		
		if (epLinearLayout.getChildCount() == 1){	// if collapsed, expand (add) the seasons
		
			TextView seasonText = (TextView)seasonRow.findViewById(R.id.season_text);
			seasonText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.arrow_down), null, null, null);

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			for (int i=0; i<episodeList.size(); i++){
				if (seasonRow.getId() == episodeList.get(i).getSeason()){
					View episodeView = inflater.inflate(R.layout.episode_text_row, epLinearLayout, false);
					
					episodeView.setBackgroundColor(AppSettings.listBackgroundColors[i % AppSettings.listBackgroundColors.length]);
					
					TextView text = (TextView)episodeView.findViewById(R.id.text);
					String nameText = String.format("%02d", episodeList.get(i).getNumber()) + " " + episodeList.get(i).getName();
					text.setText(nameText);
					text.setTextSize(textSize);
					text.setId(episodeList.get(i).getId());
										
					episodeView.setOnClickListener(episodeListener);
					epLinearLayout.addView(episodeView);
				}
			}
		} else {	 // else season is expanded, collapse it
			TextView seasonText = (TextView)seasonRow.findViewById(R.id.season_text);
			seasonText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.arrow_right), null, null, null);
			
			for (int i=epLinearLayout.getChildCount()-1; i>0; i--){
				epLinearLayout.removeView(epLinearLayout.getChildAt(i));	
			}
			
		}
		
	}
	
	// Handle episode clicks
	final OnClickListener episodeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView epText = (TextView)v;
        	long episodeId = epText.getId();            
        	Intent myIntent = new Intent(getApplicationContext(), EpisodeDetails.class);
        	myIntent.putExtra("id", episodeId);
        	myIntent.putExtra("seriesId", seriesId);
        	myIntent.putExtra("seriesName", seriesInfo.getName());        	
    		startActivityForResult(myIntent, 0);		
		}
	};
	

	public void addToFavorites(){
		new AddFavoriteTask().execute(seriesInfo);		
	}
	
	private class AddFavoriteTask extends AsyncTask<TvSeries, Void, Void>{
		@Override
		protected Void doInBackground(TvSeries... params) {
			FavoriteSeriesInfo info = new FavoriteSeriesInfo(Long.valueOf(params[0].getId()), params[0].getName(), 0, 0);
			FavoritesData favorites = new FavoritesData(getApplicationContext());
			favorites.createFavoriteSeries(info);
			favorites.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			Toast.makeText(getApplicationContext(), "This show will now appear in your favorites list.", Toast.LENGTH_SHORT).show();
			isFavorite = true;
			invalidateOptionsMenu();
		}
	}
	
	private class RemoveFavoriteTask extends AsyncTask<Long, Void, Boolean>{
		@Override
		protected Boolean doInBackground(Long... params) {
			FavoritesData favorites = new FavoritesData(getApplicationContext());
			favorites.removeSeries(params[0]);
			favorites.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			Toast.makeText(getApplicationContext(), "The show has been removed from your favorites.", Toast.LENGTH_SHORT).show();
			isFavorite = false;
			invalidateOptionsMenu();
		}
	}
	

	/** Launch the share menu for the series banner */
	public void shareImage(){
		try{
			Intent i = ShareUtil.makeIntent(getApplicationContext(), seriesInfo.getImage().getId());
			if (i != null)
				startActivity(i);
		}catch (Exception e){
			Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	
	
	// User Rating Functions ////////////////////////////////////////////////////////
	
	
	// Load the user's rating asynchronously
	private class LoadRatingTask extends AsyncTask<Void, Void, Integer>{
		
		private Exception e;
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			try {
	    		GetRatingHandler ratingAdapter = new GetRatingHandler();
	    		Rating r = ratingAdapter.getSeriesRating(userAccountId, seriesId);
	    		return Integer.valueOf(r.getUserRating());
			}catch (RatingNotFoundException e){
				return 0;
			}catch (Exception e){
				this.e = e;
			}
			return 0;
		}
		
		@Override
		protected void onPostExecute(Integer rating){
			if (e != null)
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			
			setUserRatingTextView(rating);
		}
	}
	
 	/** Update the GUI with the specified rating */
 	private void setUserRatingTextView(int rating){
 		
 		try {
	 		TextView ratingTextView = (TextView)findViewById(R.id.rating);
			String communityRating = (seriesInfo == null) ? "?" : seriesInfo.getRating(); 
	 		String communityRatingText = communityRating + " / 10";
			
			String ratingTextA = communityRatingText + "  (";
			String ratingTextB = (rating == 0) ? "rate" : String.valueOf(rating);
			String ratingTextC = ")";
			
			int start = ratingTextA.length();
			int end = ratingTextA.length() + ratingTextB.length();
					
			SpannableStringBuilder ssb = new SpannableStringBuilder(ratingTextA + ratingTextB + ratingTextC);
			
			ssb.setSpan(new NonUnderlinedClickableSpan() {
				@Override
				public void onClick(View v){
					showRatingDialog();		        		
				}
			}, start, end, 0);
			
			ssb.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.episode_link), start, end, 0);	// Set the style of the text
			ratingTextView.setText(ssb, BufferType.SPANNABLE);
			ratingTextView.setMovementMethod(LinkMovementMethod.getInstance());
 		}catch (Exception e){
 			Log.e("SeriesOverview", "Failed to setUserRatingTextView: " + e.getMessage());
 		}
 	}
 	
 	
 	/** Display the rating dialog to the user */
	private void showRatingDialog(){
		if (userAccountId.equals("")){
			Toast.makeText(this, "You must specify your account identifier in the application settings before you can set ratings.", Toast.LENGTH_LONG).show();
		}else{
			RatingFragment dialog = new RatingFragment();
			dialog.setTitle(seriesInfo.getName());
			dialog.show(getSupportFragmentManager(), "RatingFragment");
		}
	}
	
	// Called when the user clicks Rate from the dialog 
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        TextView valueText = (TextView)dialog.getDialog().findViewById(R.id.value);
        new UpdateRatingTask().execute(userAccountId, String.valueOf(seriesId), valueText.getText().toString());
    }
    
    // Called when the user clicks Cancel from the dialog
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Do nothing	
    }
    
    // Update the rating asynchronously
 	private class UpdateRatingTask extends AsyncTask<String, Void, Boolean>{
 		@Override
 		protected Boolean doInBackground(String... params) {
 			try {
 				SetRatingHandler ra = new SetRatingHandler();
 		        return ra.setSeriesRating(params[0], params[1], Integer.valueOf(params[2]));
 			}catch (Exception e){
 				return false;
 			} 			
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean result){
 			if (result){
 				new LoadRatingTask().execute();
 				Toast.makeText(getApplicationContext(), "Your rating has been saved", Toast.LENGTH_SHORT).show();
 			}else{
 				Toast.makeText(getApplicationContext(), "A problem was encountered while trying to save your rating", Toast.LENGTH_SHORT).show();
 			}
 		}

 	}
 	
 	
	
	
	// ACTIONBAR MENU ////////////////////////////////////////////////
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
		if (isFavorite != null){
			if (isFavorite){
				menu.add("Remove Favorite")
				.setIcon(R.drawable.ic_discard)
		        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						new RemoveFavoriteTask().execute(seriesInfo.getId());
						return false;
					}
				})
		        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
				
			}else{
				menu.add("Favorite")
				.setIcon(R.drawable.ic_favorite)
		        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						addToFavorites();
						return false;
					}
				})
		        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);			
			}
		}
		
		
		
		// SHARE Sub Menu ///////////////////////////
		SubMenu subMenu1 = menu.addSubMenu("Share");
		subMenu1
    		.add("TheTVDB Link")
    		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    			@Override
    			public boolean onMenuItemClick(MenuItem item) {
    				try{
    					startActivity(ShareUtil.makeIntent(getApplicationContext(), seriesInfo.getName(), "http://thetvdb.com/?tab=series&id=" + seriesId));
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
						if (!seriesInfo.getIMDB().equals(""))
							startActivity(ShareUtil.makeIntent(getApplicationContext(), seriesInfo.getName(), "http://www.imdb.com/title/" + seriesInfo.getIMDB()));
						else
							Toast.makeText(getApplicationContext(), "IMDB link could not be found.", Toast.LENGTH_SHORT).show();
					}catch (Exception e){
						Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
					}
					return false;
				}
			});
			subMenu1
			.add("Series Banner")
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
		userAccountId = settings.getString("accountId", "").trim();
		useNiceDates = settings.getBoolean("useNiceDates", true);
    	textSize = Float.parseFloat(settings.getString("textSize", "18.0"));

    	TextView textview = (TextView)findViewById(R.id.loading1);
    	textview.setTextSize(textSize);
    	
    	textview = (TextView)findViewById(R.id.loading2);
    	textview.setTextSize(textSize);
    	
    	textview = (TextView)findViewById(R.id.banner_listing_link);
    	textview.setTextSize(textSize);

    	textview = (TextView)findViewById(R.id.airs_header);
    	textview.setTextSize(textSize*1.3f);
    	
    	textview = (TextView)findViewById(R.id.last_episode);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.next_episode);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.series_air_info);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.starring);
		textview.setTextSize(textSize*1.3f);

		textview = (TextView)findViewById(R.id.series_actors);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.rating_header);
		textview.setTextSize(textSize*1.3f);

		textview = (TextView)findViewById(R.id.rating);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.genre_header);
		textview.setTextSize(textSize*1.3f);

		textview = (TextView)findViewById(R.id.genre);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.runtime_header);
		textview.setTextSize(textSize*1.3f);

		textview = (TextView)findViewById(R.id.runtime);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.overview_header);
		textview.setTextSize(textSize*1.3f);

		textview = (TextView)findViewById(R.id.overview);
		textview.setTextSize(textSize);

		textview = (TextView)findViewById(R.id.seasons_header);
		textview.setTextSize(textSize*1.3f);


	}
}