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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;
import com.heath_bar.tvdb.data.FavoritesDAL;
import com.heath_bar.tvdb.data.TvdbDAL;
import com.heath_bar.tvdb.data.adapters.SeriesOverviewPagerAdapter;
import com.heath_bar.tvdb.types.Actor;
import com.heath_bar.tvdb.types.FavoriteSeriesInfo;
import com.heath_bar.tvdb.types.LoadCastDataTask;
import com.heath_bar.tvdb.types.LoadEpisodeListTask;
import com.heath_bar.tvdb.types.LoadSeriesDataTask;
import com.heath_bar.tvdb.types.TaskManagementFragment;
import com.heath_bar.tvdb.types.TvEpisodeList;
import com.heath_bar.tvdb.types.TvSeries;
import com.heath_bar.tvdb.util.ShareUtil;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;


public class SeriesOverview extends SherlockFragmentActivity implements RatingFragment.NoticeDialogListener, TaskManagementFragment.TaskFinishedListener {

	ViewPager mViewPager;
	SeriesOverviewPagerAdapter mPageAdapter;
    
	protected long seriesId;
	protected TvSeries seriesInfo;
	protected Boolean isFavorite = null;
	
	protected TvdbDAL tvdb;
	protected int refreshing = 0;
	
	// define the cast task fragments (the workers)
	protected static final String TASK_FRAGMENT_TAG = "myTaskFragment";
	
	// define the tasks IDs hat will be sent to the task fragments (the work) 
	protected static final int CAST_TASK_ID = 0;
	protected static final int SUMMARY_TASK_ID = 1;
	protected static final int EPISODE_DATA_TASK_ID = 2;
	protected static final int RATING_TASK_ID = 3;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.series_overview);
        
        final ActionBar bar = getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        bar.setTitle("Series Overview");
        
        Bundle extras = getIntent().getExtras();
	    if(extras != null) {
	    	seriesId = getIntent().getLongExtra("id", 0);
	    	seriesInfo = new TvSeries();
	    	seriesInfo.setId(seriesId);
	    	seriesInfo.setName(getIntent().getStringExtra("seriesName"));
	    	
	    	bar.setTitle(seriesInfo.getName());
	    	
	    	tvdb = new TvdbDAL(this);
	        
	        mPageAdapter = new SeriesOverviewPagerAdapter(getSupportFragmentManager());
	        
	        // At this point the activity may have been recreated due to a rotation,
	        // and there may be a TaskFragment lying around. So see if we can find it.
	        // Check to see if we have retained the worker fragment.
	        TaskManagementFragment taskFragment = (TaskManagementFragment)getSupportFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);

	        if (taskFragment == null){
	        	taskFragment = new TaskManagementFragment();
	            
	            // And create tasks for it to monitor
	        	taskFragment.addTask(CAST_TASK_ID, new LoadCastDataTask(seriesId));
	        	taskFragment.addTask(SUMMARY_TASK_ID, new LoadSeriesDataTask(seriesId));
	        	taskFragment.addTask(EPISODE_DATA_TASK_ID, new LoadEpisodeListTask(seriesId));
	        	
	            // Start the fragment.
	            getSupportFragmentManager().beginTransaction().add(taskFragment, TASK_FRAGMENT_TAG).commit();
	        }
	        
	        
	        mViewPager = (ViewPager)findViewById(R.id.pager);
	        mViewPager.setAdapter(mPageAdapter);
	        mViewPager.setOnPageChangeListener(mPageAdapter);
	        mViewPager.setCurrentItem(1);
	        mViewPager.setOffscreenPageLimit(2);
	        
	        // Bind the title indicator to the adapter
	        TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
	        titleIndicator.setViewPager(mViewPager);
	    
	        final float density = getResources().getDisplayMetrics().density;
	        titleIndicator.setBackgroundColor(getResources().getColor(R.color.blue2));
	        titleIndicator.setTopPadding(1 * density);
	        titleIndicator.setFooterColor(getResources().getColor(R.color.tvdb_green));
	        titleIndicator.setFooterLineHeight(1 * density);
	        titleIndicator.setFooterIndicatorHeight(3 * density);
	        titleIndicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
	        titleIndicator.setSelectedBold(true);
	    
	    }
 	}
	


	@Override
	@SuppressWarnings("unchecked")
	public void onTaskFinished(int taskId, Object resultData) {
		try {
			if (taskId == CAST_TASK_ID){
				CastFragment castFragment = mPageAdapter.getCastFragment();
				castFragment.setupAdapter(this, (ArrayList<Actor>)resultData);
				
			} else if (taskId == SUMMARY_TASK_ID){
				seriesInfo = (TvSeries)resultData;
				
				// redraw the options menu with the correct icon
				FavoritesDAL db = new FavoritesDAL(this);
				isFavorite = db.isFavoriteSeries(seriesInfo.getId());
				supportInvalidateOptionsMenu();
				
				// Update the summary fragment
				SummaryFragment summaryFragment = mPageAdapter.getSummaryFragment();
				summaryFragment.populateTheUI(this, seriesInfo); 
			
			} else if (taskId == EPISODE_DATA_TASK_ID){
				TvEpisodeList epList = (TvEpisodeList)resultData;
				EpisodeListFragment episodeFragment = mPageAdapter.getEpisodeListFragment();
				episodeFragment.setupAdapter(this, seriesInfo, epList);
				
				SummaryFragment summaryFragment = mPageAdapter.getSummaryFragment();
				summaryFragment.populateTheUIPart2(this, epList.getLastAired(), epList.getNextAired());
				
			} else if (taskId == RATING_TASK_ID){
				// do something
			}
			
		} catch (ClassCastException e) {
			Log.e("SeriesOverview", "onTaskFinished:" + e.getMessage());
		}
	}
	
	
	
    
    
    
    // Ratings ///////////////////////////////////////////////////////////////////////////////////
    
    // Called when the user clicks Rate from the dialog 
 	@Override
 	public void onDialogPositiveClick(DialogFragment dialog) {
 		TextView valueText = (TextView)dialog.getDialog().findViewById(R.id.value);
 		SummaryFragment sf = mPageAdapter.getSummaryFragment();
 	    sf.updateRating(valueText.getText().toString());
 	}

 	@Override
 	public void onDialogNegativeClick(DialogFragment dialog) {
 		// Do nothing
 	}
  	
    
    
    
 	
 	
 	

	// Favorites functions /////////////////////////////////////////////////////////////////

	public void addToFavorites(){
		new AddFavoriteTask().execute(seriesInfo);		
	}
	
	private class AddFavoriteTask extends AsyncTask<TvSeries, Void, Void>{
		@Override
		protected Void doInBackground(TvSeries... params) {
			FavoriteSeriesInfo info = new FavoriteSeriesInfo(Long.valueOf(params[0].getId()), params[0].getName(), 0, 0);
			FavoritesDAL favorites = new FavoritesDAL(getApplicationContext());
			favorites.createFavoriteSeries(info);
			favorites.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			Toast.makeText(getApplicationContext(), "This show will now appear in your favorites list.", Toast.LENGTH_SHORT).show();
			isFavorite = true;
			supportInvalidateOptionsMenu();
		}
	}
	
	private class RemoveFavoriteTask extends AsyncTask<Long, Void, Boolean>{
		@Override
		protected Boolean doInBackground(Long... params) {
			FavoritesDAL favorites = new FavoritesDAL(getApplicationContext());
			favorites.removeSeries(params[0]);
			favorites.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			Toast.makeText(getApplicationContext(), "The show has been removed from your favorites.", Toast.LENGTH_SHORT).show();
			isFavorite = false;
			supportInvalidateOptionsMenu();
		}
	}
	

	/** Launch the share menu for the series banner */
	public void shareImage(){
		try{
			Intent i = ShareUtil.makeIntent(getApplicationContext(), seriesInfo.getBanner().getId());
			if (i != null)
				startActivity(i);
		}catch (Exception e){
			Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
		}
	}
	

	
    
    
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	// Instance state has been restored. Send data to the fragments.
    	
    	TaskManagementFragment castTaskFragment = (TaskManagementFragment)getSupportFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);
    	Object data = castTaskFragment.getResultData(CAST_TASK_ID);
    	if (data != null)
    		onTaskFinished(CAST_TASK_ID, data);
    	
    	TaskManagementFragment seriesTaskFragment = (TaskManagementFragment)getSupportFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);
    	data = seriesTaskFragment.getResultData(SUMMARY_TASK_ID);
    	if (data != null)
    		onTaskFinished(SUMMARY_TASK_ID, data);
    	
    	TaskManagementFragment episodeTaskFragment = (TaskManagementFragment)getSupportFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);
    	data = episodeTaskFragment.getResultData(EPISODE_DATA_TASK_ID);
    	if (data != null)
    		onTaskFinished(EPISODE_DATA_TASK_ID, data);
    }
    
    
    
    
	// ACTIONBAR MENU ////////////////////////////////////////////////
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
		if (isFavorite != null){
			if (isFavorite){
				menu.add("Remove Favorite")
				.setIcon(R.drawable.ic_favorite)
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
				.setIcon(R.drawable.ic_not_favorite)
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

	
}