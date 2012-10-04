package com.heath_bar.tvdb;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.heath_bar.lazylistadapter.BitmapFileCache;
import com.heath_bar.tvdb.types.WebImage;

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
	
	
	private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{
		@Override
		protected Bitmap doInBackground(String... input) {
			
			try {
								
				Bitmap bitmap = null;
				BitmapFileCache fileCache = new BitmapFileCache(getApplicationContext());
				
				if (!fileCache.contains(input[0]))
				{
					//download
					WebImage theImage = new WebImage();
					theImage.setUrl(input[1]);
					theImage.Load(getApplicationContext());
					bitmap = theImage.getBitmap();
				}
				else
				{
					bitmap = fileCache.get(input[0]);					
				}

				return bitmap;
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap){

			ImageView iv = (ImageView)findViewById(R.id.image);
			iv.setImageBitmap(bitmap);
			
			// Hide the loading text
			findViewById(R.id.loading1).setVisibility(View.GONE);
			setSupportProgressBarIndeterminateVisibility(false);
		}
	}

}
