package com.heathbar.tvdb;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TheTVDBActivity extends SherlockFragmentActivity {
	
	TabListener<FavoritesFragment> favoritesTabListener;
	TabListener<SearchFragment> searchTabListener;
	TabListener<TodaysEpisodesFragment> todaysTabListener;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);
        bar.setTitle("The TVDB");
        
        favoritesTabListener  = new TabListener<FavoritesFragment>(this, "favs", FavoritesFragment.class, null);
        bar.addTab(bar.newTab()
        		.setText("Favorites")
        		.setTabListener(favoritesTabListener));
        
        searchTabListener = new TabListener<SearchFragment>(this, "search", SearchFragment.class, null);
        bar.addTab(bar.newTab()
        		.setText("Search")
        		.setTabListener(searchTabListener));
        
        todaysTabListener = new TabListener<TodaysEpisodesFragment>(this, "today", TodaysEpisodesFragment.class, null);
        bar.addTab(bar.newTab()
        		.setText("Today's Episodes")
        		.setTabListener(todaysTabListener));
        
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }
    
    
//    public void setFragment(int tabIndex, Fragment newFragment){
//    	if (tabIndex == 0)
//    		favoritesTabListener.setFragment(newFragment);
//    	else if (tabIndex == 1)
//    		searchTabListener.setFragment(newFragment);
//    	else if (tabIndex == 2)
//    		todaysTabListener.setFragment(newFragment);
//    }
    
    
    
    
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case android.R.id.home:
//            // app icon in action bar clicked; go home
//                        Intent intent = new Intent(this, TodaysEpisodesFragment.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intent);
//                        return true;
//        default:
//            return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar()
                .getSelectedNavigationIndex());
    }

}