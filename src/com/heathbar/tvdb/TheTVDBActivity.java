package com.heathbar.tvdb;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabWidget;

public class TheTVDBActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost(); // The activity's tabhost
        TabHost.TabSpec spec; 			// Reuseable TabSpec for each tab
        Intent intent;					// Reuseable intent for each tab
        
        //Create an Intent to launch an Activity for the tab
        intent = new Intent().setClass(this, FavoritesActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
    	spec = tabHost.newTabSpec("favorites").setIndicator("Favorites").setContent(intent);
        tabHost.addTab(spec);
        
        // Do the same for the other tabs
        intent = new Intent().setClass(this, TodaysEpisodesActivity.class);
        spec = tabHost.newTabSpec("today").setIndicator("Today's Episodes").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SearchActivity.class);
        spec = tabHost.newTabSpec("search").setIndicator("Search").setContent(intent);
        tabHost.addTab(spec);
                
        tabHost.setCurrentTab(0);
        
        // Since we are not using tab icons, shrink all of the tab buttons to 50px 
        // TODO: don't hard code 50px
        TabWidget widget = tabHost.getTabWidget();
        for (int i=0; i < widget.getChildCount(); i++)
        	tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 50;
    }
}