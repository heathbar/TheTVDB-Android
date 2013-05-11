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

package com.heath_bar.tvdb.types;

import android.widget.Toast;

import com.heath_bar.tvdb.data.xmlhandlers.GetRatingHandler;
import com.heath_bar.tvdb.types.exceptions.RatingNotFoundException;

public class LoadRatingTask extends ManageableTask {

	TaskManagementFragment mTaskFragment;
    long seriesId;
    String userAccountId;
    private Exception e;
    
    public LoadRatingTask(long seriesId, String userAccountId) {
		this.seriesId = seriesId;
		this.userAccountId = userAccountId;
	}
        
	@Override
	protected Integer doInBackground(TaskManagementFragment... taskFragment) {
		mTaskFragment = taskFragment[0];
				
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
	protected void onPostExecute(Object data){
		
		if (mTaskFragment != null){
			
			if (e != null)
				Toast.makeText(mTaskFragment.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			
			mTaskFragment.taskFinished(getTaskId(), data);
		}
			
	}
}
