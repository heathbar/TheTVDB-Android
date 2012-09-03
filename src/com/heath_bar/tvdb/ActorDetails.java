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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.heath_bar.tvdb.types.Actor;
import com.heath_bar.tvdb.util.NonUnderlinedClickableSpan;
import com.heath_bar.tvdb.util.StringUtil;
import com.heath_bar.tvdb.xml.handlers.ActorHandler;

public class ActorDetails extends SherlockActivity {

	private long seriesId;
	private String actorName;
	
	// OnCreate... display essentially just a loading screen while we call LoadInfoTask in the background
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.actor_details);
        	
		try {
			
			Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	seriesId = getIntent().getLongExtra("seriesId", 0);
		    	actorName = getIntent().getStringExtra("ActorName");
		    	String seriesName = getIntent().getStringExtra("seriesName");
		    	
				// Set title
				getSupportActionBar().setTitle(seriesName);
		    	
		    	// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(true);
				new LoadActorDetailsTask().execute(String.valueOf(seriesId), actorName);
		    	
				ApplyPreferences();
    		}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	private class LoadActorDetailsTask extends AsyncTask<String, Void, Actor>{
		@Override
		protected Actor doInBackground(String... name) {
			
			try {
				// Lookup episode info
				ActorHandler actorQuery = new ActorHandler();
				Actor theActor = actorQuery.getActor(name[0], name[1]);
				
				// Download the image while we're still in the background thread
				theActor.getImage().Load();
				
				return theActor;
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Actor theActor){

			// Populate the activity with the data we just found
			PopulateStuff(theActor);
			
			// Hide the loading text
			findViewById(R.id.loading1).setVisibility(View.GONE);
			setSupportProgressBarIndeterminateVisibility(false);
		}
	}
	
	private void PopulateStuff(Actor theActor){
		
		if (theActor == null){
			Toast.makeText(getApplicationContext(), "Unable to locate any information for that actor", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		// Set Title
		TextView textview = (TextView)findViewById(R.id.title);
		textview.setVisibility(View.VISIBLE);
		textview.setText(theActor.getName());

		// Set Role
		textview = (TextView)findViewById(R.id.role);
		textview.setVisibility(View.VISIBLE);
		textview.setText(StringUtil.commafy(theActor.getRole()));
		
		// Set Image
		if (theActor.getImage().getBitmap() == null || theActor.getImage().getUrl().equals("")){
			// do nothin
		} else {
			ImageView banner = (ImageView)findViewById(R.id.actor_image);
    		banner.setImageBitmap(theActor.getImage().getBitmap());
    		banner.setVisibility(View.VISIBLE);
		}
		
		textview = (TextView)findViewById(R.id.imdb_link);
		textview.setVisibility(View.VISIBLE);
		
		SpannableStringBuilder ssb = new SpannableStringBuilder(getResources().getString(R.string.imdb));
		ssb.setSpan(new NonUnderlinedClickableSpan(getResources().getString(R.string.imdb)) {
			@Override
			public void onClick(View v){
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/find?q=" + actorName.replace(' ', '+') + "&s=all"));
				startActivity(myIntent);	        		
			}
		}, 0, ssb.length(), 0);
		
		ssb.setSpan(new TextAppearanceSpan(this, R.style.episode_link), 0, ssb.length(), 0);	// Set the style of the text
		textview.setText(ssb, BufferType.SPANNABLE);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
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
	
	// Apply Preferences
	private void ApplyPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	float textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
    	
		TextView textview = (TextView)findViewById(R.id.title);
		textview.setTextSize(textSize*1.4f);

		// Set Role
		textview = (TextView)findViewById(R.id.role);
		textview.setTextSize(textSize);
		
		textview = (TextView)findViewById(R.id.imdb_link);
		textview.setTextSize(textSize);
	}
}
