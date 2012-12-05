package com.heath_bar.tvdb.util;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.heath_bar.tvdb.data.adapters.lazylist.BitmapFileCache;


public class ShareUtil {

	public static Intent makeIntent(Context context, String subject, String url){
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		i.putExtra(Intent.EXTRA_TEXT, url);
		return Intent.createChooser(i,  "Share URL");
	}
	
	public static Intent makeIntent(Context context, String imageId) throws FileNotFoundException {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		   					
		/*
		* ALTERNATE WORKING CODE 
		* This is working code using the MediaStore
		* I chose not to use the MediaStore since dropbox auto uploads every image added to the MediaStore.  
		*
	    FileOutputStream fos = openFileOutput(filename + ".jpg", MODE_WORLD_READABLE);
	    fos.write(bytes.toByteArray());
        fos.close();    				    
        File jpg = getFileStreamPath(filename + ".jpg");    				    
        String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), jpg.getAbsolutePath(), jpg.getName(), filename);
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path)); 
        */
		
		BitmapFileCache cache = new BitmapFileCache(context);
		if (cache.getCacheDir().getAbsolutePath().contains("sdcard")){
			// I'm going to assume the image hasn't been trimmed from the cache...
			String path = cache.makeJPG(imageId);
			share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
			
			
			return Intent.createChooser(share, "Share Image");
			
		}else{
			Toast.makeText(context, "You must have an SD card mounted in order to share images", Toast.LENGTH_LONG).show();
			return null;
		}
	}
}
