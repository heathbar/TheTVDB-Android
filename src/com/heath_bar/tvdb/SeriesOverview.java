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
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
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
import com.heath_bar.tvdb.types.FavoriteSeriesInfo;
import com.heath_bar.tvdb.types.TvEpisode;
import com.heath_bar.tvdb.types.TvSeries;
import com.heath_bar.tvdb.util.ShareUtil;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;


public class SeriesOverview extends SherlockFragmentActivity  {

	ViewPager mViewPager;
    StaticFragmentPagerAdapter mPageAdapter;
    
	protected long seriesId;
	protected TvSeries seriesInfo;
	protected Boolean isFavorite = null;
	
	protected TvdbDAL tvdb;
	protected boolean castRefreshing = false;
	protected boolean summaryRefreshing = false;
	protected boolean episodesRefreshing = false;
	protected EpisodeListFragment episodeRefreshQueuedFragment;

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.series_overview);
        setProgressBarIndeterminateVisibility(true);
        
        Bundle extras = getIntent().getExtras();
	    if(extras != null) {
	    	seriesId = getIntent().getLongExtra("id", 0);
	    	tvdb = new TvdbDAL(this);
	        
	        final ActionBar bar = getSupportActionBar();
	        bar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
	        bar.setTitle("Series Overview");
	
//	        mPageAdapter = new StaticFragmentPagerAdapter(this, null);
//	        mPageAdapter.addTab("Cast", ActorsFragment.class, null);
//	        mPageAdapter.addTab("Summary", SummaryFragment.class, null);
//	        mPageAdapter.addTab("Episodes", EpisodeListFragment.class, null);
	        	        
//	        ArrayList<TabInfo> tabList = new ArrayList<TabInfo>();
//	        fragList.add(new ActorsFragment());
//	        fragList.add(new SummaryFragment());
//	        fragList.add(new EpisodeListFragment());
	        
	        mPageAdapter = new StaticFragmentPagerAdapter(getSupportFragmentManager());
	        mPageAdapter.addTab("Actors", new ActorsFragment());
	        mPageAdapter.addTab("Summary", new SummaryFragment());
	        mPageAdapter.addTab("Episodes", new EpisodeListFragment());
	        
	        mViewPager = (ViewPager)findViewById(R.id.pager);
	        mViewPager.setAdapter(mPageAdapter);
	        mViewPager.setOnPageChangeListener(mPageAdapter);
	        mViewPager.setCurrentItem(1);
	        
	        // Bind the title indicator to the adapter
	        TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
	        titleIndicator.setViewPager(mViewPager);
	    
	        // Make it stylish
	        final float density = getResources().getDisplayMetrics().density;
	        titleIndicator.setBackgroundColor(0xFF000000);
	        titleIndicator.setTopPadding(1 * density);
	        titleIndicator.setFooterColor(getResources().getColor(R.color.tvdb_green));
	        titleIndicator.setFooterLineHeight(1 * density);
	        titleIndicator.setFooterIndicatorHeight(3 * density);
	        titleIndicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
	        titleIndicator.setSelectedBold(true);
	    }
 
	}
	
	
	// Requests from the fragments to load data for them ///////////////////////////////////////////////////////////////
	
	public void requestCastRefresh(ActorsFragment fragment){
		castRefreshing = true;
		setProgressBarIndeterminateVisibility(true);

		tvdb.new LoadActorList(fragment).execute(seriesId);
		
		castRefreshing = false;
		clearIndeterminateProgressBar();
	}
	
	public void requestSummaryRefresh(SummaryFragment fragment){
		summaryRefreshing = true;
		setProgressBarIndeterminateVisibility(true);
		
		tvdb.new LoadSummaryTask(fragment).execute(seriesId);
		
		summaryRefreshing = false;
		clearIndeterminateProgressBar();
	}
	
	public void requestEpisodeRefresh(EpisodeListFragment fragment){
		episodesRefreshing = true;
		setProgressBarIndeterminateVisibility(true);
		
		// If we have the seriesInfo already available, load the episodes, else wait until we have seriesInfo available
		if (seriesInfo != null){
			tvdb.new LoadEpisodeList(fragment).execute(seriesId);
		}else {
			episodeRefreshQueuedFragment = fragment;
		}
		
		episodesRefreshing = false;
		clearIndeterminateProgressBar();
	}

	public void clearIndeterminateProgressBar(){
		if (!castRefreshing && !summaryRefreshing && !episodesRefreshing)
			setProgressBarIndeterminateVisibility(false);
	}

	
	// Callback from the LoadSummaryTask
	public void setTvSeriesInfo(TvSeries info) {
		getSupportActionBar().setTitle(info.getName());
		seriesInfo = info;
		
		isFavorite = seriesInfo.isFavorite(this);
		supportInvalidateOptionsMenu();
		
		// if an episode refresh was waiting for seriesInfo, kick it off now
		if (episodeRefreshQueuedFragment != null){
			requestEpisodeRefresh(episodeRefreshQueuedFragment);
			episodeRefreshQueuedFragment = null;
		}
	}
	
	public TvSeries getTvSeriesInfo(){
		return seriesInfo;
	}
	
	// Callback from LoadEpisodeTask
	public void setLastNextEpisodes(TvEpisode last, TvEpisode next){
		
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
			Intent i = ShareUtil.makeIntent(getApplicationContext(), seriesInfo.getImage().getId());
			if (i != null)
				startActivity(i);
		}catch (Exception e){
			Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
		}
	}
	

	
	
	
 	
 	/**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class StaticFragmentPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
        

        private final FragmentManager mFragmentManager;
        private FragmentTransaction mCurTransaction = null;
        private Fragment mCurrentPrimaryItem = null;
        private final ArrayList<TabInfo> mTabs;
               
        private ArrayList<Fragment.SavedState> mSavedState;
        
        static final class TabInfo {
        	private final String title;
            private final Fragment fragment;
            private boolean added;

            TabInfo(String _title, Fragment _frag) {
            	title = _title;
                fragment = _frag;
                added = false;
            }
        }

        public StaticFragmentPagerAdapter(FragmentManager fm) {
        	mFragmentManager = fm;
        	mTabs = new ArrayList<SeriesOverview.StaticFragmentPagerAdapter.TabInfo>();
        	mSavedState = new ArrayList<Fragment.SavedState>();
        }
        
        public void addTab(String tabTitle, Fragment frag){
        	mTabs.add(new TabInfo(tabTitle, frag));
        }
        
        @Override
        public Object instantiateItem(View container, int position) {
    		if (mTabs != null && mTabs.size() > position){
    			TabInfo t = mTabs.get(position);

    			// Add it if it's not already added
    			if (!t.added){
	        		if (mCurTransaction == null) {
	                    mCurTransaction = mFragmentManager.beginTransaction();
	                }	        		
	    			mCurTransaction.add(container.getId(), t.fragment, t.title);
	    			
	    			if (t.fragment != mCurrentPrimaryItem) {
	    	            t.fragment.setMenuVisibility(false);
	    	            t.fragment.setUserVisibleHint(false);
	    	        }
	    			t.added = true;
    			}
    		}
            
//        	if (mSavedState.size() > position){
//        		Fragment.SavedState fss = mSavedState.get(position);
//        		if (fss != null)
//        			fragment.setInitialSavedState(fss);
//        	}

    		return mTabs.get(position).fragment;
        }
        
        @Override
        public void destroyItem(View container, int position, Object object){
        	
        	// NOTHING TO DO HERE, EXCEPT MAYBE MANAGE STATE
        	// We like to hog all the memories
        }
        
        @Override
        public void setPrimaryItem(View Container, int position, Object object){
        	Fragment fragment = (Fragment) object;
        	if (fragment != mCurrentPrimaryItem){
        		if (mCurrentPrimaryItem != null){
        			mCurrentPrimaryItem.setMenuVisibility(false);
        		}
        		if (fragment != null){
        			fragment.setMenuVisibility(true);
        		}
        		mCurrentPrimaryItem = fragment;
        	}
        }
        
        @Override
        public void finishUpdate(View container) {
        	if(mCurTransaction != null){
        		mCurTransaction.commitAllowingStateLoss();
        		mCurTransaction = null;
        		mFragmentManager.executePendingTransactions();
        	}
        }
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
        	return ((Fragment)object).getView() == view;
        }
        
        @Override
        public Parcelable saveState() {
        	Bundle state = null;
        	if (mSavedState.size() > 0){
        		state = new Bundle();
        		Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
        		mSavedState.toArray(fss);
        		state.putParcelableArray("states", fss);
        	}
        	for (int i=0; i<mTabs.size(); i++){
        		Fragment f = mTabs.get(i).fragment;
        		if (f != null){
        			if(state == null)
        				state = new Bundle();
        			String key = "f" + i;
        			mFragmentManager.putFragment(state, key, f);
        		}
        	}
        	return state;
        }
        
        
        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        	if (state != null){
        		Bundle bundle = (Bundle)state;
        		bundle.setClassLoader(loader);
        		Parcelable[] fss = bundle.getParcelableArray("states");
        		mSavedState.clear();
        		mTabs.clear();
        		if (fss != null){
        			for (int i=0; i<fss.length; i++){
        				mSavedState.add((Fragment.SavedState)fss[i]);
        			}
        		}
        		Iterable<String> keys = bundle.keySet();
        		for (String key: keys){
        			if (key.startsWith("f")){
        				int index = Integer.parseInt(key.substring(1));
        				Fragment f = mFragmentManager.getFragment(bundle, key);
        				if (f != null){
        					while(mTabs.size() <= index){
        						mTabs.add(null);
        					}
        					f.setMenuVisibility(false);
        					mTabs.set(index, new TabInfo("TAB", f));
        				}
        			}
        		}
        	}
        }
        
        @Override
        public int getCount() {
            return mTabs.size();
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
        	return mTabs.get(position).title;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

        @Override
        public void onPageSelected(int position) { }

        @Override
        public void onPageScrollStateChanged(int state) { }
    }
	
	// ACTIONBAR MENU ////////////////////////////////////////////////
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
		if (isFavorite != null){
			if (isFavorite){
				menu.add("Remove Favorite")
				.setIcon(R.drawable.ic_discard)
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
				.setIcon(R.drawable.ic_favorite)
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

		
		menu.add("Search")
        .setIcon(R.drawable.ic_search)
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onSearchRequested();
				return false;
			}
		})
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

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