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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.heath_bar.lazylistadapter.BitmapFileCache;
import com.heath_bar.lazylistadapter.BitmapWebUtil;
import com.heath_bar.lazylistadapter.WebImage;

public class BannerViewer extends SherlockActivity {

	
	// OnCreate... display essentially just a loading screen while we call LoadImageTask in the background
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.banner_viewer);
        	
		try {
			
			Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	String seriesName = getIntent().getStringExtra("seriesName");
		    	String imageId = getIntent().getStringExtra("imageId");
		    	String url = getIntent().getStringExtra("url");

				// Set title
				getSupportActionBar().setTitle(seriesName);
		    	
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
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(WebImage wi){

			final String imageId = wi.getId();
			
			ImageView image = (ImageView)findViewById(R.id.image);
			image.setImageBitmap(wi.getBitmap());
			image.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// **
    				// SEE ActorDetails.java for alternate working code using the MediaStore
    				// **
															
    				Intent share = new Intent(Intent.ACTION_SEND);
    				share.setType("image/jpeg");
    				
    				BitmapFileCache cache = new BitmapFileCache(getApplicationContext());
    				if (cache.getCacheDir().getAbsolutePath().contains("sdcard")){
    					// I'm going to assume the image hasn't been trimmed from the cache...
    					String path = cache.makeJPG(imageId);
    					share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
    					startActivity(Intent.createChooser(share, "Share Image"));
    					
    				}else{
    					// Can't share if there is no sdcard... at least not reliably or without using the MediaStore
    					Toast.makeText(getApplicationContext(), "You must have an SD card mounted in order to share images", Toast.LENGTH_LONG).show();
    				}	
					
				}
			});
			
			// Hide the loading text
			findViewById(R.id.loading1).setVisibility(View.GONE);
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
		
	}

}
