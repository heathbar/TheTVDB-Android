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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.heath_bar.tvdb.data.TvdbDAL;
import com.heath_bar.tvdb.types.TvEpisode;
import com.heath_bar.tvdb.types.TvSeries;
import com.heath_bar.tvdb.util.DateUtil;
import com.heath_bar.tvdb.util.DialogBuilder;
import com.heath_bar.tvdb.util.NonUnderlinedClickableSpan;
import com.heath_bar.tvdb.util.StringUtil;

public class SummaryFragment extends SherlockFragment  {

	protected TvSeries seriesInfo;
	protected float textSize;
	protected long cacheSize;
	protected String userAccountId;
	protected Boolean isFavorite = null;
	protected boolean useNiceDates;
	protected int userRating;
	protected FragmentManager mFragmentManager;
	protected static final String TASK_FRAGMENT_TAG = "series_task";
	protected static final int TASK_FRAGMENT = 0;
	protected boolean isLoaded = false;
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (container == null){
            return null;
    	}else{
			return inflater.inflate(R.layout.series_summary, container, false);
        }
    }
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	ApplyPreferences();
    }
        
 	
	/** Populate the interface with the data pulled from the webz */
	public void populateTheUI(Activity activity, final TvSeries seriesInfo){
		if (activity == null){
			return;
		}else if (seriesInfo == null){
			Toast.makeText(activity, "Something bad happened. No data was found.", Toast.LENGTH_SHORT).show();
			return;
		}

		this.seriesInfo = seriesInfo;
		
		Animation fade = new AlphaAnimation(0.0f, 1.0f);
		fade.setDuration(700);
		
		ImageView imageView = (ImageView)activity.findViewById(R.id.series_banner);
		imageView.setImageBitmap(seriesInfo.getBanner().getBitmap());
		imageView.setVisibility(View.VISIBLE);
		imageView.startAnimation(fade);
		
		// Set the banner link
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(getActivity(), ImageViewer.class);
				myIntent.putExtra("imageTitle", seriesInfo.getName());
				myIntent.putExtra("imageId", seriesInfo.getBanner().getId());
				myIntent.putExtra("imageUrl", seriesInfo.getBanner().getUrl());
	        	startActivity(myIntent);
			}
		});
		
		
		TextView textview = (TextView)activity.findViewById(R.id.banner_listing_link);
		textview.setTextColor(getResources().getColor(R.color.tvdb_green));
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		textview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), BannerListing.class);
				i.putExtra("seriesId", seriesInfo.getId());
				i.putExtra("seriesName", seriesInfo.getName());
				startActivity(i);
			}
		});
		
		// Set air info
		textview = (TextView)activity.findViewById(R.id.airs_header);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		textview = (TextView)activity.findViewById(R.id.last_episode);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		textview = (TextView)activity.findViewById(R.id.next_episode);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		textview = (TextView)activity.findViewById(R.id.series_air_info);
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
	        	DialogBuilder.InformationalDialog(getActivity(), "Disclaimer", "All TV show information is maintained by users on thetvdb website. \n\nAir Times are typically listed in the timezone of the network that airs them.\n\nCorrections can be made at http://thetvdb.com").show();
	        }  
	    };
	    int start = airedText.length()-1;
		int end = airedText.length();
		airedText.setSpan(clickableSpan, start, end, 0);
		airedText.setSpan(new TextAppearanceSpan(activity, R.style.episode_link), start, end, 0);
		airedText.setSpan(new AbsoluteSizeSpan((int)textSize, true), 0, airedText.length(), 0);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		textview.setText(airedText, BufferType.SPANNABLE);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		// Set rating
		textview = (TextView)activity.findViewById(R.id.rating_header);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		textview = (TextView)activity.findViewById(R.id.rating);
		textview.setText(seriesInfo.getRating() + " / 10");
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		
		// Set genre
		textview = (TextView)activity.findViewById(R.id.genre_header);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		textview = (TextView)activity.findViewById(R.id.genre);
		textview.setText(StringUtil.commafy(seriesInfo.getGenre()));
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		
		// Set runtime
		textview = (TextView)activity.findViewById(R.id.runtime_header);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		textview = (TextView)activity.findViewById(R.id.runtime);
		textview.setText(seriesInfo.getRuntime() + " minutes");
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		// Set overview		
		textview = (TextView)activity.findViewById(R.id.overview);
		textview.setText(seriesInfo.getOverview());
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		// IMDB Link
		textview = (TextView)activity.findViewById(R.id.imdb_link);
		textview.setVisibility(View.VISIBLE);
		textview.startAnimation(fade);
		
		final String imdbId = seriesInfo.getIMDB();
		SpannableStringBuilder ssb = new SpannableStringBuilder(getResources().getString(R.string.imdb));
		ssb.setSpan(new NonUnderlinedClickableSpan(getResources().getString(R.string.imdb)) {
			@Override
			public void onClick(View v){
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + imdbId));
				startActivity(myIntent);	        		
			}
		}, 0, ssb.length(), 0);
		
		ssb.setSpan(new TextAppearanceSpan(activity, R.style.episode_link), 0, ssb.length(), 0);	// Set the style of the text
		textview.setText(ssb, BufferType.SPANNABLE);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		textview.startAnimation(fade);
		
		// Hide & show the loading spinner
		activity.findViewById(R.id.progress_summary).setVisibility(View.GONE);
		activity.findViewById(R.id.progress_summary2).setVisibility(View.VISIBLE);
				
		isLoaded = true;

	}

		
	public void populateTheUIPart2(Activity activity, TvEpisode last, TvEpisode next){

		if (activity == null){
			return;
		}
		Animation fade = new AlphaAnimation(0.0f, 1.0f);
		fade.setDuration(700);
		
		LinearLayout ll = (LinearLayout)activity.findViewById(R.id.last_episode_row);
		TextView textview = (TextView)activity.findViewById(R.id.last_episode);
		
		if (last == null){
			 textview.setText("Last Episode: unknown");
		}else{
		
			textview.setText("Last Episode:");
			ll.setId(last.getId());
			ll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					episodeListener.onClick(v);
				}
			});
			
			textview = (TextView)ll.findViewById(R.id.episode_number);
			textview.setText(last.getSeason() + "." + String.format("%02d", last.getNumber()));
			
			textview = (TextView)ll.findViewById(R.id.episode_name);
			textview.setTextColor(getResources().getColor(R.color.tvdb_green));
			textview.setText(last.getName());
			
			String dateString = (useNiceDates) ? DateUtil.toNiceString(DateUtil.toString(last.getAirDate())) : DateUtil.toString(last.getAirDate());
			textview = (TextView)ll.findViewById(R.id.episode_date);
			textview.setText(dateString);

			ll.startAnimation(fade);	
		}
		
		ll = (LinearLayout)activity.findViewById(R.id.next_episode_row);
		textview = (TextView)activity.findViewById(R.id.next_episode);

		if (next == null){
			 textview.setText("Next Episode: unknown");
		}else{
		
			textview.setText("Next Episode:");
			ll.setId(next.getId());
			ll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					episodeListener.onClick(v);
				}
			});
			
			textview = (TextView)ll.findViewById(R.id.episode_number);
			textview.setText(next.getSeason() + "." + String.format("%02d", next.getNumber()));
			
			textview = (TextView)ll.findViewById(R.id.episode_name);
			textview.setTextColor(getResources().getColor(R.color.tvdb_green));
			textview.setText(next.getName());
			
			
			String dateString = (useNiceDates) ? DateUtil.toNiceString(DateUtil.toString(next.getAirDate())) : DateUtil.toString(next.getAirDate());
			textview = (TextView)ll.findViewById(R.id.episode_date);
			textview.setText(dateString);
			
			ll.startAnimation(fade);
		}
		
		ProgressBar pb = (ProgressBar)activity.findViewById(R.id.progress_summary2);
		pb.setVisibility(View.GONE);
	}

		// Handle episode clicks
		final OnClickListener episodeListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				long episodeId = v.getId();
	        	Intent myIntent = new Intent(v.getContext(), EpisodeDetails.class);
	        	myIntent.putExtra("id", episodeId);
	        	myIntent.putExtra("seriesId", seriesInfo.getId());
	        	myIntent.putExtra("seriesName", seriesInfo.getName());
	    		startActivityForResult(myIntent, 0);
			}
		};
	
	
	/** Update the GUI with the specified rating */
 	public void setUserRatingTextView(int rating){
 		
 		try {
	 		TextView ratingTextView = (TextView)getActivity().findViewById(R.id.rating);
			String communityRating = (seriesInfo == null) ? "?" : seriesInfo.getRating(); 
	 		String communityRatingText = communityRating + " / 10";
			
			String ratingTextA = communityRatingText + "  (";
			String ratingTextB = (rating == 0) ? "RATE" : StringUtil.wordify(rating);
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
			
			ssb.setSpan(new TextAppearanceSpan(getActivity(), R.style.episode_link), start, end, 0);	// Set the style of the text
			ratingTextView.setText(ssb, BufferType.SPANNABLE);
			ratingTextView.setMovementMethod(LinkMovementMethod.getInstance());
			
			userRating = rating;
 		}catch (Exception e){
 			Log.e("SeriesOverview", "Failed to setUserRatingTextView: " + e.getMessage());
 		}
 	}
 	
 	/** Display the rating dialog to the user */
	private void showRatingDialog(){
		if (userAccountId.equals("")){
			DialogBuilder.InformationalDialog(getActivity(), "Error", "You must specify your account identifier in the application settings before you can set ratings.").show();
		}else{
			RatingFragment dialog = new RatingFragment();
			dialog.setTitle(seriesInfo.getName());
			dialog.setInitialValue(userRating);
			dialog.show(getActivity().getSupportFragmentManager(), "RatingFragment");
		}
	}
	
	public void updateRating(String value) {
	    TvdbDAL tvdb = new TvdbDAL(getActivity());
	    tvdb.new UpdateRatingTask(this).execute(userAccountId, String.valueOf(seriesInfo.getId()), value);
	}
	
	
	
	// Apply Preferences
	private void ApplyPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		cacheSize = settings.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
		userAccountId = settings.getString("accountId", "").trim();
		useNiceDates = settings.getBoolean("useNiceDates", true);
    	textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
    	Activity activity = getActivity();

    	try {
	    	TextView textview = (TextView)activity.findViewById(R.id.banner_listing_link);
	    	textview.setTextSize(textSize*1.2f);
	
	    	textview = (TextView)activity.findViewById(R.id.airs_header);
	    	textview.setTextSize(textSize*1.3f);
	    	
	    	textview = (TextView)activity.findViewById(R.id.last_episode);
			textview.setTextSize(textSize);
	
			textview = (TextView)activity.findViewById(R.id.next_episode);
			textview.setTextSize(textSize);
	
			textview = (TextView)activity.findViewById(R.id.series_air_info);
			textview.setTextSize(textSize);
	
			textview = (TextView)activity.findViewById(R.id.rating_header);
			textview.setTextSize(textSize*1.3f);
	
			textview = (TextView)activity.findViewById(R.id.rating);
			textview.setTextSize(textSize);
	
			textview = (TextView)activity.findViewById(R.id.genre_header);
			textview.setTextSize(textSize*1.3f);
	
			textview = (TextView)activity.findViewById(R.id.genre);
			textview.setTextSize(textSize);
	
			textview = (TextView)activity.findViewById(R.id.runtime_header);
			textview.setTextSize(textSize*1.3f);
	
			textview = (TextView)activity.findViewById(R.id.runtime);
			textview.setTextSize(textSize);
	
			textview = (TextView)activity.findViewById(R.id.overview);
			textview.setTextSize(textSize);
			
			textview = (TextView)activity.findViewById(R.id.imdb_link);
			textview.setTextSize(textSize*1.2f);
			
			LinearLayout ll = (LinearLayout)activity.findViewById(R.id.last_episode_row);
			
			textview = (TextView)ll.findViewById(R.id.episode_number);
			textview.setTextSize(textSize*2.0f);
			
			textview = (TextView)ll.findViewById(R.id.episode_name);
			textview.setTextSize(textSize);
			
			textview = (TextView)ll.findViewById(R.id.episode_date);
			textview.setTextSize(textSize*0.7f);
			
			ll = (LinearLayout)activity.findViewById(R.id.next_episode_row);
			
			textview = (TextView)ll.findViewById(R.id.episode_number);
			textview.setTextSize(textSize*2.0f);
			
			textview = (TextView)ll.findViewById(R.id.episode_name);
			textview.setTextSize(textSize);
			
			textview = (TextView)ll.findViewById(R.id.episode_date);
			textview.setTextSize(textSize*0.7f);
			
    	} catch (NullPointerException e){
    		Log.e("SummaryFragment", "ApplyPreferences: " + e.getMessage());
    	}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isLoaded", isLoaded);
	}
}
