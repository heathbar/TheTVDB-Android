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

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.heath_bar.tvdb.types.WebImage;
import com.heath_bar.tvdb.xml.handlers.BannerHandler;

public class BannerViewer extends SherlockListActivity {
	
	private long seriesId;
	private ArrayList<WebImage> imageList;
	private ArrayList<WebImage> imageListDos;
	
	private int limit = 5;
	private int count = 0;
	
	// OnCreate... display essentially just a loading screen while we call LoadInfoTask in the background
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.banner_viewer);
        	
		try {
			
			Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	seriesId = getIntent().getLongExtra("seriesId", 0);
		    	String seriesName = getIntent().getStringExtra("seriesName");
		    	
				// Set title
				getSupportActionBar().setTitle(seriesName);
		    	
		    	// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(false);
				new LoadBannerListTask().execute(String.valueOf(seriesId));
		    	
//				
//				// Apply Preferences
//				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//		    	float textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
//		    	
//				TextView textview = (TextView)findViewById(R.id.loading1);
//				textview.setTextSize(textSize);
				

    		}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
		
		
	private class LoadBannerListTask extends AsyncTask<String, Void, ArrayList<WebImage>>{
		@Override
		protected ArrayList<WebImage> doInBackground(String... series) {
			
			try {
				// Lookup episode info
				BannerHandler bannerQuery = new BannerHandler();
				ArrayList<WebImage> images = bannerQuery.getImageList(series[0]);
				
				return images;
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<WebImage> images){

			imageList = images;

			LoadNextImage();
		}
	}
	
	protected void LoadNextImage(){
	
		imageListDos = new ArrayList<WebImage>();
		
		if (imageList.size() > 0){
			WebImage i = imageList.remove(0);
			
			new LoadBannerTask().execute(i);
			imageListDos.add(i);
			count++;
		}
	}
	
	
	private class LoadBannerTask extends AsyncTask<WebImage, Void, WebImage>{
		@Override
		protected WebImage doInBackground(WebImage... image) {
			
			try {
				image[0].Load(getApplicationContext());
				
				return image[0];
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(WebImage image){

//			// Draw the image
//			ImageView iv = new ImageView(getApplicationContext());
//			iv.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
//			iv.setAdjustViewBounds(true);
//			iv.setImageBitmap(image.getBitmap());
//						
//			LinearLayout linLay = (LinearLayout)findViewById(R.id.banner_linear_layout);
//			linLay.addView(iv);
//			

			
							      
			

			if (imageList.size() > 0){
				LoadNextImage();
			}else{
				
				try{
					setListAdapter(new ArrayAdapter<WebImage>(getApplicationContext(), R.layout.image, imageListDos));
					
					
				}catch (Exception e){
					if(AppSettings.LOG_ENABLED)
						Log.e("TheTVDBActivity","Failed to set the cursor");
					Toast.makeText(getApplicationContext(), "There was a problem loading your favorite shows from the database", Toast.LENGTH_SHORT).show();
				}
				
				//findViewById(R.id.loading1).setVisibility(View.GONE);
				//setSupportProgressBarIndeterminateVisibility(false);
			}
		}
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
