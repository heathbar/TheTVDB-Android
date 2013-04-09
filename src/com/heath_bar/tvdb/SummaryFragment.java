package com.heath_bar.tvdb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
import android.widget.ImageView;
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

public class SummaryFragment extends SherlockFragment implements RatingFragment.NoticeDialogListener {

	protected long seriesId;
	protected TvSeries seriesInfo;
	protected float textSize;
	protected long cacheSize;
	protected String userAccountId;
	protected Boolean isFavorite = null;
	protected boolean useNiceDates;
	
	
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
    	
    	((SeriesOverview)getActivity()).requestSummaryRefresh(this);
    }
    
    
 	
	/** Populate the interface with the data pulled from the webz */
	public void populateTheUI(Activity activity, TvSeries seriesInfo){
		if (activity == null){
			return;
		}else if (seriesInfo == null)
		{
			Toast.makeText(activity, "Something bad happened. No data was found.", Toast.LENGTH_SHORT).show();
			return;
		}

		
		// redraw the options menu with the correct icon
		isFavorite = seriesInfo.isFavorite(activity);
		((FragmentActivity)activity).supportInvalidateOptionsMenu();
		
		// Set the banner
		final String imageTitle = seriesInfo.getName();
		final String imageId = seriesInfo.getImage().getId();
		final String imageUrl = seriesInfo.getImage().getUrl();
		
		ImageView imageView = (ImageView)activity.findViewById(R.id.series_banner);
		imageView.setImageBitmap(seriesInfo.getImage().getBitmap());
		imageView.setVisibility(View.VISIBLE);
		final String seriesName = seriesInfo.getName();
		
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(getActivity(), ImageViewer.class);
				myIntent.putExtra("imageTitle", imageTitle);
				myIntent.putExtra("imageId", imageId);
				myIntent.putExtra("imageUrl", imageUrl);
	        	startActivity(myIntent);
			}
		});
		
		
		
		// Set the banner link
		TextView textview = (TextView)activity.findViewById(R.id.banner_listing_link);
		textview.setTextColor(getResources().getColor(R.color.tvdb_green));
		textview.setVisibility(View.VISIBLE);
		textview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), BannerListing.class);
				i.putExtra("seriesId", seriesId);
				i.putExtra("seriesName", seriesName);
				startActivity(i);
			}
		});
		
		// Set air info
		textview = (TextView)activity.findViewById(R.id.airs_header);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)activity.findViewById(R.id.last_episode);
		textview.setVisibility(View.VISIBLE);
		textview = (TextView)activity.findViewById(R.id.next_episode);
		textview.setVisibility(View.VISIBLE);
		
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
						
		// Set rating
		textview = (TextView)activity.findViewById(R.id.rating_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)activity.findViewById(R.id.rating);
		textview.setText(seriesInfo.getRating() + " / 10");
		textview.setVisibility(View.VISIBLE);
		
		
		// Set genre
		textview = (TextView)activity.findViewById(R.id.genre_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)activity.findViewById(R.id.genre);
		textview.setText(StringUtil.commafy(seriesInfo.getGenre()));
		textview.setVisibility(View.VISIBLE);
		
		
		// Set runtime
		textview = (TextView)activity.findViewById(R.id.runtime_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)activity.findViewById(R.id.runtime);
		textview.setText(seriesInfo.getRuntime() + " minutes");
		textview.setVisibility(View.VISIBLE);
		
		
		// Set overview
		textview = (TextView)activity.findViewById(R.id.overview_header);
		textview.setVisibility(View.VISIBLE);
		
		textview = (TextView)activity.findViewById(R.id.overview);
		textview.setText(seriesInfo.getOverview());
		textview.setVisibility(View.VISIBLE);
		
		// IMDB Link
		textview = (TextView)activity.findViewById(R.id.imdb_link);
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
		
		ssb.setSpan(new TextAppearanceSpan(activity, R.style.episode_link), 0, ssb.length(), 0);	// Set the style of the text
		textview.setText(ssb, BufferType.SPANNABLE);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		
		// Hide the loading image
		activity.findViewById(R.id.progress_summary).setVisibility(View.GONE);
	}

	
		
	public void populateTheUIPart2(Activity activity, TvEpisode last, TvEpisode next){

		SpannableString text = null;		
		
		TextView richTextView = (TextView)activity.findViewById(R.id.last_episode);
		
		if (last == null){
			 text = new SpannableString("Last Episode: unknown");
		}else{
		
			String name = last.getSeason() + "x" + String.format("%02d", last.getNumber()) + " " + last.getName();
			String dateString = (useNiceDates) ? DateUtil.toNiceString(DateUtil.toString(last.getAirDate())) : DateUtil.toString(last.getAirDate());
			text = new SpannableString("Last Episode: " + name + " (" + dateString + ")");

			NonUnderlinedClickableSpan clickableSpan = new NonUnderlinedClickableSpan() {  
		        @Override  
		        public void onClick(View view) { 
		            episodeListener.onClick(view);
		        }  
		    };
		    int start = 14;
			int end = start + name.length();
		    text.setSpan(clickableSpan, start, end, 0);
		    text.setSpan(new TextAppearanceSpan(activity, R.style.episode_link), start, end, 0);
		    text.setSpan(new AbsoluteSizeSpan((int)textSize, true), 0, text.length(), 0);
		    richTextView.setId(last.getId());
			richTextView.setMovementMethod(LinkMovementMethod.getInstance());
		}
		richTextView.setText(text, BufferType.SPANNABLE);
		
		
		text = null;
		richTextView = (TextView)activity.findViewById(R.id.next_episode);

		if (next == null){
			 text = new SpannableString("Next Episode: unknown");
		}else{
		
			String name = next.getSeason() + "x" + String.format("%02d", next.getNumber()) + " " + next.getName();
			String dateString = (useNiceDates) ? DateUtil.toNiceString(DateUtil.toString(next.getAirDate())) : DateUtil.toString(next.getAirDate());
			text = new SpannableString("Next Episode: " + name + " (" + dateString + ")");

			NonUnderlinedClickableSpan clickableSpan = new NonUnderlinedClickableSpan() {  
		        @Override  
		        public void onClick(View view) { 
		        	episodeListener.onClick(view);  
		        }  
		    };
		    int start = 14;
			int end = start + name.length();
		    text.setSpan(clickableSpan, start, end, 0);
		    text.setSpan(new TextAppearanceSpan(activity, R.style.episode_link), start, end, 0);
		    text.setSpan(new AbsoluteSizeSpan((int)textSize, true), 0, text.length(), 0);
			richTextView.setId(next.getId());
			richTextView.setMovementMethod(LinkMovementMethod.getInstance());
		}
		richTextView.setText(text, BufferType.SPANNABLE);
	}

		// Handle episode clicks
		final OnClickListener episodeListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView epText = (TextView)v;
	        	long episodeId = epText.getId();
	        	Intent myIntent = new Intent(v.getContext(), EpisodeDetails.class);
	        	myIntent.putExtra("id", episodeId);
	        	myIntent.putExtra("seriesId", seriesId);
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
			
			ssb.setSpan(new TextAppearanceSpan(getActivity(), R.style.episode_link), start, end, 0);	// Set the style of the text
			ratingTextView.setText(ssb, BufferType.SPANNABLE);
			ratingTextView.setMovementMethod(LinkMovementMethod.getInstance());
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
			dialog.show(getActivity().getSupportFragmentManager(), "RatingFragment");
		}
	}
	
	// Called when the user clicks Rate from the dialog 
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
	    TextView valueText = (TextView)dialog.getDialog().findViewById(R.id.value);

	    TvdbDAL tvdb = new TvdbDAL(getActivity());
	    tvdb.new UpdateRatingTask(this).execute(userAccountId, String.valueOf(seriesId), valueText.getText().toString());
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// Do nothing
	}
 	
	
	// Apply Preferences
	private void ApplyPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		cacheSize = settings.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
		userAccountId = settings.getString("accountId", "").trim();
		useNiceDates = settings.getBoolean("useNiceDates", true);
    	textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
    	Activity activity = getActivity();

    	TextView textview = (TextView)activity.findViewById(R.id.banner_listing_link);
    	textview.setTextSize(textSize);

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

		textview = (TextView)activity.findViewById(R.id.overview_header);
		textview.setTextSize(textSize*1.3f);

		textview = (TextView)activity.findViewById(R.id.overview);
		textview.setTextSize(textSize);
	}
}
