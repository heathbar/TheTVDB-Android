package com.heath_bar.tvdb.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.SummaryFragment;
import com.heath_bar.tvdb.data.xmlhandlers.GetRatingHandler;
import com.heath_bar.tvdb.data.xmlhandlers.SetRatingHandler;
import com.heath_bar.tvdb.types.Rating;
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
	
//		
//		*** This is no longer needed; moved to LoadCastDataTask ***
//    public class LoadActorList extends AsyncTask<Long, Void, ArrayList<Actor>>{
//    	
//    	CastFragment fragment;    	
//    	public LoadActorList(CastFragment fragment){
//    		this.fragment = fragment;
//    	}
//    	
//		@Override
//		protected ArrayList<Actor> doInBackground(Long... seriesId) {
//			
//			try {
//				// Lookup Actor info
//				ActorHandler actorQuery = new ActorHandler();
//				ArrayList<Actor> theActors = actorQuery.getActors(seriesId[0].toString());
//								
//				return theActors;
//			}catch (Exception e){
//				e.printStackTrace();
//			}
//			return null;
//		}
//		
//		@Override
//		protected void onPostExecute(ArrayList<Actor> theActors){
//			if (fragment != null){
//				FragmentActivity activity = fragment.getActivity();
//				if (activity != null){
//					fragment.setupAdapter(activity, theActors);
//				}
//			}				
//		}
//	}
    
    
    
//	*** This is no longer needed; moved to LoadSeriesDataTask ***	
//	public TvSeries LoadTvSeries(long seriesId){
//		try {
//
//			// Lookup basic series info
//			SeriesDetailsHandler infoQuery = new SeriesDetailsHandler(mContext);
//    		TvSeries seriesInfo = infoQuery.getInfo(seriesId);
//
//    		Bitmap bitmap;
//	    	BitmapFileCache fileCache = new BitmapFileCache(mContext, cacheSize);
//	    	
//	    	if (fileCache.contains(seriesInfo.getImage().getId())){
//	    		bitmap = fileCache.get(seriesInfo.getImage().getId());
//	    	}else{
//	    		BitmapWebUtil web = new BitmapWebUtil(mContext);
//	    		bitmap = web.downloadBitmap(seriesInfo.getImage().getUrl());
//				fileCache.put(seriesInfo.getImage().getId(), bitmap);
//			}
//			seriesInfo.getImage().setBitmap(bitmap);
//			
//			return seriesInfo;
//
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		return null;
//	}

// 		*** No longer needed; moved to LoadEpisodeListTask ***	
// 	// Class to load the episodes asynchronously
//   	protected class LoadEpisodeList extends AsyncTask<Void, Void, TvEpisodeList>{
//   		
//   		SummaryFragment summaryFragment;
//   		EpisodeListFragment episodeFragment;
//   		TvSeries info;
//   		
//   		public LoadEpisodeList(SummaryFragment fragment1, EpisodeListFragment fragment2, TvSeries info){
//   			this.summaryFragment = fragment1;
//   			this.episodeFragment = fragment2;
//   			this.info = info;
//   		}
//   		
//   		@Override
//   		protected TvEpisodeList doInBackground(Void... v) {
//   			
//   			try {
//   				//Lookup Season/Episode listing
// 	    		EpisodeListHandler episodeHandler = new EpisodeListHandler(summaryFragment.getActivity());
// 	    		return episodeHandler.getEpisodes(info.getId());
// 	    		
//     		}catch (Exception e){
//   				e.printStackTrace();
//   			}
//   			return null;
//   		}
//   		
//   		@Override
//   		protected void onPostExecute(TvEpisodeList episodeList){
//   			if (summaryFragment != null){   				
//   				summaryFragment.populateTheUIPart2(summaryFragment.getActivity(), episodeList.getLastAired(), episodeList.getNextAired());
//   			}
//   			
//   			if (episodeFragment != null){
//   				FragmentActivity activity = episodeFragment.getActivity();
//   				if (activity != null){
//   			
//   					episodeFragment.setupAdapter(activity, info, episodeList);
//   				}
//   			}
//   		}   		
//   	}
 	
	
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
			
			Activity activity = fragment.getActivity();
			
			if (activity != null){
				if (e != null)
					Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
				
				fragment.setUserRatingTextView(rating);
			}else{
				Toast.makeText(activity, "There was a problem saving the rating", Toast.LENGTH_LONG).show();
			}
		}
	}
	
 	
 	
 	
    // Update the rating asynchronously
 	public class UpdateRatingTask extends AsyncTask<String, Void, Boolean>{
 		
 		SummaryFragment fragment;
 		long seriesId;
		public UpdateRatingTask(SummaryFragment fragment){
			this.fragment = fragment;
		}
		
 		@Override
 		protected Boolean doInBackground(String... params) {
 			try {
 				seriesId = Long.valueOf(params[1]);
 				SetRatingHandler ra = new SetRatingHandler();
 		        return ra.setSeriesRating(params[0], params[1], Integer.valueOf(params[2]));
 			}catch (Exception e){
 				return false;
 			} 			
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean result){
 			if (result){
 				new LoadRatingTask(fragment).execute(seriesId);
 				Toast.makeText(fragment.getActivity(), "Your rating has been saved", Toast.LENGTH_SHORT).show();
 			}else{
 				Toast.makeText(fragment.getActivity(), "A problem was encountered while trying to save your rating", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}

 	
 	
 	
 	
 	
 	
 	
 	
}
