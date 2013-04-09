package com.heath_bar.tvdb.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.heath_bar.tvdb.ActorsFragment;
import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.EpisodeListFragment;
import com.heath_bar.tvdb.SeriesOverview;
import com.heath_bar.tvdb.SummaryFragment;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapFileCache;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapWebUtil;
import com.heath_bar.tvdb.data.xmlhandlers.ActorHandler;
import com.heath_bar.tvdb.data.xmlhandlers.EpisodeListHandler;
import com.heath_bar.tvdb.data.xmlhandlers.GetRatingHandler;
import com.heath_bar.tvdb.data.xmlhandlers.SeriesDetailsHandler;
import com.heath_bar.tvdb.data.xmlhandlers.SetRatingHandler;
import com.heath_bar.tvdb.types.Actor;
import com.heath_bar.tvdb.types.Rating;
import com.heath_bar.tvdb.types.TvEpisodeList;
import com.heath_bar.tvdb.types.TvSeries;
import com.heath_bar.tvdb.types.exceptions.RatingNotFoundException;

public class TvdbDAL {

	protected Context mContext;
	protected float textSize;
	protected long cacheSize;
	protected String userAccountId;
	protected boolean useNiceDates;
	
	
	public TvdbDAL(Context context){
		mContext = context;
		ApplyPreferences();
	}
	
	public void ApplyPreferences(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		cacheSize = settings.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
		userAccountId = settings.getString("accountId", "").trim();
		useNiceDates = settings.getBoolean("useNiceDates", true);
    	textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
	}
	
	
	
	// TODO: Add Caching to all of these tasks. //
	
	
	
	
	
    public class LoadActorList extends AsyncTask<Long, Void, ArrayList<Actor>>{
    	
    	ActorsFragment fragment;    	
    	public LoadActorList(ActorsFragment fragment){
    		this.fragment = fragment;
    	}
    	
		@Override
		protected ArrayList<Actor> doInBackground(Long... seriesId) {
			
			try {
				// Lookup Actor info
				ActorHandler actorQuery = new ActorHandler();
				ArrayList<Actor> theActors = actorQuery.getActors(seriesId[0].toString());
								
				return theActors;
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Actor> theActors){
			fragment.setupAdapter(fragment.getActivity(), theActors);
		}
	}

    
	
	// Class to load the basic series info asynchronously
 	public class LoadSummaryTask extends AsyncTask<Long, Void, TvSeries>{
 		
 		long seriesId;
 		SummaryFragment fragment;
 		public LoadSummaryTask(SummaryFragment fragment){
 			this.fragment = fragment;
 		}
 		
 		@Override
 		protected TvSeries doInBackground(Long... id) {
 			
 			try {
 				// Lookup basic series info
 				seriesId = id[0];
 				SeriesDetailsHandler infoQuery = new SeriesDetailsHandler(mContext);
 	    		TvSeries seriesInfo = infoQuery.getInfo(seriesId);
 	    			    		
 	    		Bitmap bitmap;
 		    	BitmapFileCache fileCache = new BitmapFileCache(mContext, cacheSize);
 		    	
 		    	if (fileCache.contains(seriesInfo.getImage().getId())){
 		    		
 		    		bitmap = fileCache.get(seriesInfo.getImage().getId());

 		    	}else{
 		    	
 		    		BitmapWebUtil web = new BitmapWebUtil(mContext);
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
 			
 			if (fragment != null && fragment.getActivity() != null){
	 			// Set title
	 			((SeriesOverview)fragment.getActivity()).setTvSeriesInfo(info);
	
	 			// Populate the activity with the data we just loaded
	 			fragment.populateTheUI(fragment.getActivity(), info);
	 			
	 			// Load Rating
	 			if (!userAccountId.equals(""))
	 				new LoadRatingTask(fragment).execute(seriesId);
	 			else
	 				fragment.setUserRatingTextView(0);
 			}
 		}
 	}
 	
 	
 	
 	
	
	// User Rating Functions ////////////////////////////////////////////////////////
	
	// Load the user's rating asynchronously
	public class LoadRatingTask extends AsyncTask<Long, Void, Integer>{
		
		private Exception e;
 		SummaryFragment fragment;
		public LoadRatingTask(SummaryFragment fragment){
			this.fragment = fragment;
		}
		
		@Override
		protected Integer doInBackground(Long... id) {
			
			try {
	    		GetRatingHandler ratingAdapter = new GetRatingHandler();
	    		Rating r = ratingAdapter.getSeriesRating(userAccountId, id[0]);
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
				Toast.makeText(fragment.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			
			fragment.setUserRatingTextView(rating);
		}
	}
	
 	
 	
 	
    // Update the rating asynchronously
 	public class UpdateRatingTask extends AsyncTask<String, Void, Boolean>{
 		
 		SummaryFragment fragment;
		public UpdateRatingTask(SummaryFragment fragment){
			this.fragment = fragment;
		}
		
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
 				new LoadRatingTask(fragment).execute();
 				Toast.makeText(fragment.getActivity(), "Your rating has been saved", Toast.LENGTH_SHORT).show();
 			}else{
 				Toast.makeText(fragment.getActivity(), "A problem was encountered while trying to save your rating", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}

 	
 	
 	
 	// Class to load the episodes asynchronously
   	public class LoadEpisodeList extends AsyncTask<Long, Void, TvEpisodeList>{
   		
   		EpisodeListFragment fragment;
   		public LoadEpisodeList(EpisodeListFragment fragment){
   			this.fragment = fragment;
   		}
   		
   		@Override
   		protected TvEpisodeList doInBackground(Long... id) {
   			
   			try {
   				//Lookup Season/Episode listing
 	    		EpisodeListHandler episodeHandler = new EpisodeListHandler(fragment.getActivity());
 	    		return episodeHandler.getEpisodes(id[0]);
 	    		
     		}catch (Exception e){
   				e.printStackTrace();
   			}
   			return null;
   		}
   		
   		@Override
   		protected void onPostExecute(TvEpisodeList episodeList){
   			if (fragment != null && fragment.getActivity() != null){
	 			//((SeriesOverview)fragment.getActivity()).setTvSeriesInfo(info);
	 			fragment.setupAdapter(fragment.getActivity(), episodeList);
   			}
   		}
   		
   	}
 	
 	
 	
 	
 	
}
