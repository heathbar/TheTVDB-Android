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
package com.heath_bar.tvdb.data.adapters.lazylist;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.heath_bar.tvdb.R;

public class BitmapLoader {
	
	private BitmapMemoryCache memCache;
	private BitmapFileCache fileCache;
	private BitmapWebUtil web;
	private WebImageList images;						// Holds a map of ids (keys) and urls (values)
	private Map<ImageView, String> lastAssignments; 	// keep track of what URL was last assigned to a given imageview
	final int loading_placeholder = R.drawable.loading;
	private boolean preferThumbnails;
    ExecutorService asyncQueue; 

	
	public BitmapLoader(Context ctx, WebImageList images, boolean preferThumbnails){
		memCache = new BitmapMemoryCache();
		fileCache = new BitmapFileCache(ctx);
		web = new BitmapWebUtil(ctx);
		lastAssignments = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
		this.images = images;
		this.preferThumbnails = preferThumbnails;
		
		asyncQueue = Executors.newFixedThreadPool(5);
	}

	/** Load the bitmap from the cache into the specified imageview; on cache miss load it asynchronously */
	public void Load(int position, ImageView image) {
		String id = images.get(position).getId();
		lastAssignments.put(image, id);
		Bitmap bitmap = memCache.get(id);

		if (bitmap != null)
			image.setImageBitmap(bitmap);
		else
			LoadAsync(position, image);
	}


	/** Load the bitmap by id into the specified imageview. Try to load from disk first, web second */
	private void LoadAsync(int position, ImageView image) {

		try {
			image.setImageResource(loading_placeholder);									// 1. Display the loading image for now
			asyncQueue.submit(new BitmapLoaderRunnable(images.get(position), image));		// 2. Load image asynchronously from file/web
		} catch (Throwable e) {
			e.printStackTrace();
			if(e instanceof OutOfMemoryError)
	               memCache.clear();
		}
	}
	
	
	/** Class to submit to the queue so that it will be run on a background thread. */
	class BitmapLoaderRunnable implements Runnable {
		WebImage wi;
		ImageView image;
        public BitmapLoaderRunnable(WebImage wi, ImageView image){
        	this.wi = wi;
            this.image = image;
        }
        
        @Override
        public void run() {
    	
        	Bitmap bitmap = null;
        	
    		try {
    			// If a different url has been assigned to this imageview, just quit now; don't waste time downloading the image
    			if (lastAssignments.get(image) == null || !lastAssignments.get(image).equals(wi.getId()))
	        		return;
    			
    			
    			if (preferThumbnails && wi.HasThumb()){						// Use the thumbnails if appropriate
    				if (!fileCache.containsThumb(wi.getId())){				// If the image isn't in the fileCache, download it and save it to the cache
    					bitmap = web.downloadBitmap(wi.getThumbUrl());
    					fileCache.putThumb(wi.getId(), bitmap);
    				}

    				bitmap = fileCache.getThumbResampled(wi.getId());		// Get the resampled Bitmap
    			
    			}else {														// Else use the original image
    				if (!fileCache.contains(wi.getId())){					// If the image isn't in the fileCache, download it and save it to the cache
	    				bitmap = web.downloadBitmap(wi.getUrl());
	    				fileCache.put(wi.getId(), bitmap);
    				}
    			
    				// Get the resampled Bitmap
    				bitmap = fileCache.getResampled(wi.getId());			// Get the resampled Bitmap
    			
    			}
    		} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (Throwable e){
				e.printStackTrace();
				if(e instanceof OutOfMemoryError)
					Log.e("BitmapLoader", "Encountered OutOfMemory Error when cache was " + memCache.getSizeMB() + "MB");
					memCache.clear();
			}
        	
        	
        	if (bitmap != null){
        		// Display the bitmap in the image view on the UI thread
        		((Activity)image.getContext()).runOnUiThread(new BitmapDisplayer(bitmap, image));

        		// Add the bitmap to the memory cache
        		memCache.put(wi.getId(), bitmap);
			}
        }
        

       //Used to display bitmap in the UI thread
        class BitmapDisplayer implements Runnable
        {
            Bitmap bitmap;
            ImageView image;
            public BitmapDisplayer(Bitmap bitmap, ImageView image){
            	this.bitmap = bitmap;
            	this.image = image;
            }
            public void run()
            {
            	String lastId = lastAssignments.get(image);			// Only display the bitmap in the imageview if another  
				if (lastId != null && lastId.equals(wi.getId()))	// bitmap hasn't started loading into this imageview 
				{
					if(bitmap != null)
	                    image.setImageBitmap(bitmap);
	                else
	                    image.setImageResource(loading_placeholder);
				}
            }
        }
    }
	
	
	
	public WebImageList getWebImageList(){
		return images;
	}
	
	public void clearMemoryCache(){
    	memCache.clear();
    }
	
	public void setFileCacheMaxSize(long maxSize){
		fileCache.setMaxSize(maxSize);
	}

}
