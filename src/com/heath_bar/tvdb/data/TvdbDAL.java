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
