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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapFileCache;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapWebUtil;
import com.heath_bar.tvdb.data.adapters.lazylist.WebImage;
import com.heath_bar.tvdb.types.ZoomableImageView;
import com.heath_bar.tvdb.util.ShareUtil;

public class ImageViewer extends SherlockActivity {

	protected WebImage webImage;
	
	// OnCreate... display essentially just a loading screen while we call LoadImageTask in the background
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.image_viewer);

		try {
			
			Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	String title = getIntent().getStringExtra("imageTitle");
		    	String imageId = getIntent().getStringExtra("imageId");
		    	String url = getIntent().getStringExtra("imageUrl");

				// Set title
				getSupportActionBar().setTitle(title);
		    	
		    	// Start the asynchronous load process
		    	setSupportProgressBarIndeterminateVisibility(true);
				new LoadImageTask().execute(imageId, url);
		    	    			    		
		    	
    		}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private class LoadImageTask extends AsyncTask<String, Void, WebImage>{
		@Override
		protected WebImage doInBackground(String... input) {
			
			try {

		    	Bitmap bitmap;
		    	BitmapFileCache fileCache = new BitmapFileCache(getApplicationContext());
		    	
		    	if (fileCache.contains(input[0])){
		    		
		    		bitmap = fileCache.get(input[0]);

		    	}else{
		    	
		    		BitmapWebUtil web = new BitmapWebUtil(getApplicationContext());
		    		bitmap = web.downloadBitmap(input[1]);
					fileCache.put(input[0], bitmap);
				}
				
		    	WebImage wi = new WebImage();
		    	wi.setId(input[0]);
		    	wi.setBitmap(bitmap);
		    	
				return wi;
			}catch (Exception e){
				Log.e("ImageViewer.LoadImageTask", "Error: " + e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(WebImage wi){

			if (wi != null){
				
				// Keep a pointer to the image so we can potentially share it later
				webImage = wi;
				
				ZoomableImageView image = (ZoomableImageView)findViewById(R.id.image);
				image.setImageBitmap(wi.getBitmap());
				image.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
			}else{
				Toast.makeText(getApplicationContext(), "Oops! The image failed to load.", Toast.LENGTH_SHORT).show();
			}
			
			// Hide the loading text
			findViewById(R.id.loading1).setVisibility(View.GONE);
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
		
	}
	
	/** Launch the share menu */
	public void shareImage(){
		try {
			Intent i = ShareUtil.makeIntent(getApplicationContext(), webImage.getId());
			if (i != null)
				startActivity(i);
		}catch (Exception e){
			Toast.makeText(getApplicationContext(), "There was a problem sharing the content.", Toast.LENGTH_SHORT).show();
		}
	}
	
	// ACTIONBAR MENU
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    	
		menu.add("Share")
	    	.setIcon(R.drawable.ic_share)
	    	.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					shareImage();
					return false;
				}
			})
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

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
