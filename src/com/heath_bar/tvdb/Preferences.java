package com.heath_bar.tvdb;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class Preferences extends SherlockPreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
	}
}
