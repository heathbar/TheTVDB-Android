package com.heath_bar.tvdb;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockListFragment;
import com.heath_bar.tvdb.data.adapters.CastAdapter;
import com.heath_bar.tvdb.types.Actor;

public class CastFragment extends SherlockListFragment {

	protected CastAdapter adapter;
	//protected int mCurrentPostion = 0;
	protected long seriesId;
	protected long cacheSize;
	protected float textSize;
	protected int mScrollPosition = 0;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (container == null){
            return null;
    	}else{
    		return inflater.inflate(R.layout.list, container, false);
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

	public void setupAdapter(FragmentActivity activity, ArrayList<Actor> theActors) {
		try {
			if (adapter == null){
				adapter = new CastAdapter(activity, R.layout.actor_row, theActors, textSize);
			}
			

			ListView list = (ListView)activity.findViewById(android.R.id.list);
			if (list != null){
				list.setAdapter(adapter);
			
				// restore state
				list.setSelection(mScrollPosition);
			}
		
			ProgressBar progress = ((ProgressBar)activity.findViewById(R.id.progress_actors));
			if (progress != null)
				progress.setVisibility(View.GONE);

		} catch (Exception e){
			Log.e("ActorsFragment", "Error: " + e.getMessage());
		}
		
	}

	
	// Save scroll position
    @Override
    public void onSaveInstanceState(Bundle icicle) {
    	super.onSaveInstanceState(icicle);
    	    	
    	// Save scroll position
    	ListView list = (ListView)getActivity().findViewById(android.R.id.list);
        if (icicle != null && list != null){
	        icicle.putInt("scrollPosition", list.getFirstVisiblePosition()+1);	// Why +1? Because it works.
        }
    } 

    // Restore scroll position
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
    	super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey("scrollPosition"))
        	mScrollPosition = savedInstanceState.getInt("scrollPosition");
    }
}
