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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapFileCache;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapWebUtil;
import com.heath_bar.tvdb.data.xmlhandlers.SeriesDetailsHandler;

public class LoadSeriesDataTask extends ManageableTask {

	TaskManagementFragment mTaskFragment;
    long seriesId;
    
    public LoadSeriesDataTask(long seriesId) {
		this.seriesId = seriesId;
	}
        
	@Override
	protected TvSeries doInBackground(TaskManagementFragment... taskFragment) {
		try {
			mTaskFragment = taskFragment[0];
			Context ctx = mTaskFragment.getActivity();
			
			if (ctx == null)
				return null;
		
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
			int cacheSize = settings.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
		
		
			// Lookup basic series info
			SeriesDetailsHandler infoQuery = new SeriesDetailsHandler(ctx);
    		TvSeries seriesInfo = infoQuery.getInfo(seriesId);

    		Bitmap bitmap;
	    	BitmapFileCache fileCache = new BitmapFileCache(ctx, cacheSize);
	    	
	    	if (fileCache.contains(seriesInfo.getBanner().getId())){
	    		bitmap = fileCache.get(seriesInfo.getBanner().getId());
	    	}else{
	    		BitmapWebUtil web = new BitmapWebUtil(ctx);
	    		bitmap = web.downloadBitmap(seriesInfo.getBanner().getUrl());
				fileCache.put(seriesInfo.getBanner().getId(), bitmap);
			}
			seriesInfo.getBanner().setBitmap(bitmap);
	
//			//There is no need to load the poster at this time.			
//			
//			if (fileCache.contains(seriesInfo.getPoster().getId())){
//	    		bitmap = fileCache.get(seriesInfo.getPoster().getId());
//	    	}else{
//	    		BitmapWebUtil web = new BitmapWebUtil(ctx);
//	    		bitmap = web.downloadBitmap(seriesInfo.getPoster().getUrl());
//				fileCache.put(seriesInfo.getPoster().getId(), bitmap);
//			}
//			seriesInfo.getPoster().setBitmap(bitmap);
			
			return seriesInfo;

		}catch (Exception e){
			Log.e("LoadSeriesDataTask", "doInBackground:" + e.getMessage());
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Object info){
		if (mTaskFragment != null)
			mTaskFragment.taskFinished(getTaskId(), info);
	}
}
