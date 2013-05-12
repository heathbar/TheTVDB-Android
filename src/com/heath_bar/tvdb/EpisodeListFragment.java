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
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragment;
import com.heath_bar.tvdb.data.adapters.EpisodeAdapter;
import com.heath_bar.tvdb.types.TvEpisodeList;
import com.heath_bar.tvdb.types.TvSeries;

public class EpisodeListFragment extends SherlockFragment {

	protected EpisodeAdapter adapter;
	protected long cacheSize;
	protected float textSize;
	protected Parcelable mListState = null;
	protected int mScrollPosition = 0;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (container == null){
            return null;
    	}else{
    		return inflater.inflate(R.layout.episode_list, container, false);
        }
    }
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	// Apply Preferences
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
  		cacheSize = settings.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
  		textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
    }
    
  	
	public void setupAdapter(FragmentActivity activity, final TvSeries info, TvEpisodeList episodeList) {
		try {
			
			adapter = new EpisodeAdapter(activity, episodeList, textSize);
			
			ExpandableListView list = (ExpandableListView)activity.findViewById(R.id.expandable_list);
			list.setAdapter(adapter);
			
			list.setOnChildClickListener(new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					long episodeId = adapter.getChildId(groupPosition, childPosition);
					Intent myIntent = new Intent(getActivity(), EpisodeDetails.class);
					myIntent.putExtra("id", episodeId);
		        	myIntent.putExtra("seriesId", info.getId());
	        		myIntent.putExtra("seriesName", info.getName());
		    		startActivity(myIntent);
					return false;
				}
			});

			// restore state
			if (mListState != null)
				list.onRestoreInstanceState(mListState);
			list.setSelection(mScrollPosition);
			
			
			ProgressBar progress = ((ProgressBar)activity.findViewById(R.id.progress_episode));
			if (progress != null)
				progress.setVisibility(View.GONE);	
			
		} catch (Exception e){
			Log.e("EpisodeListFragment", "Error: " + e.getMessage());
		}
	}
	
	
	
	
	// Save which items are expanded & scroll position
    @Override
    public void onSaveInstanceState(Bundle icicle) {
    	super.onSaveInstanceState(icicle);
    	    	
    	// Save list state (which groups are expanded) & scroll position
    	ExpandableListView list = (ExpandableListView)getActivity().findViewById(R.id.expandable_list);
        if (icicle != null && list != null){
	    	icicle.putParcelable("listState", list.onSaveInstanceState());
	        icicle.putInt("scrollPosition", list.getFirstVisiblePosition()+1);	// Why +1? Because it works.
        }
    } 

    // Restore scroll position and any items that were expanded
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
    	super.onViewStateRestored(savedInstanceState);

    	if (savedInstanceState != null && savedInstanceState.containsKey("listState"))
    		mListState = savedInstanceState.getParcelable("listState");
        
        if (savedInstanceState != null && savedInstanceState.containsKey("scrollPosition"))
        	mScrollPosition = savedInstanceState.getInt("scrollPosition");
    }
}
