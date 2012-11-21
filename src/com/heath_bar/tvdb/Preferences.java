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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.heath_bar.lazylistadapter.BitmapFileCache;

public class Preferences extends SherlockPreferenceActivity {
	
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        addPreferencesFromResource(R.xml.preferences);
        
        
        // Handle all of the preference changes/dependencies
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
    	  	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    	  		
    	  		if (key.equals("accountId")){
    	  			
    	  			// If accountId is blanked, we can't sync favorites
    	  			if (prefs.getString("accountId", "").trim().equals("")){
    	  				CheckBoxPreference cbp = (CheckBoxPreference)findPreference("syncFavorites");
    	  				cbp.setChecked(false);
    	  			}

    	  			
    	  		}else if (key.equals("cacheSize")){

    	  			// Trim the cache as needed
        	  		long cacheSize = prefs.getInt("cacheSize", AppSettings.DEFAULT_CACHE_SIZE) * 1000 * 1000;
        	  		BitmapFileCache fileCache = new BitmapFileCache(getApplicationContext(), cacheSize);
        	  		fileCache.trimCache();
    	  		}
    	  	}
        };
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(prefListener);
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
