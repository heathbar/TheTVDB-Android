package com.heath_bar.tvdb.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;



public class ImageGetter  {

	private static Map<String, Bitmap> cachedFiles = new HashMap<String, Bitmap>();
	
	/** Load a url into an ImageView */
	public static Bitmap LoadImage(String url){
		
		// 1. Check the cache
		if (cachedFiles.containsKey(url) && cachedFiles.get(url) != null){
			return cachedFiles.get(url);
		}else{
			return downloadBitmap(url);		
		}
	}
	
	/** Clear the cache */
	public static void clearCache(){
		cachedFiles = new HashMap<String, Bitmap>();
	}
	
	/** This function creates the http request and retrieves the image */
	public static Bitmap downloadBitmap(String url) {
    	HttpParams params = new BasicHttpParams();
    	params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    	HttpClient client = new DefaultHttpClient(params);
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) { 
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
                return null;
            }
            
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent(); 
                	final Bitmap bitmap = BitmapFactory.decodeStream(new PatchInputStream(inputStream));                    
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();  
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            Log.w("ImageDownloader", "Error while retrieving bitmap from " + url + e.toString());
        } finally {
            if (client != null) {
                //client.close();
            }
        }
        return null;
    }
	
	
	
	public static class PatchInputStream extends FilterInputStream {
	  public PatchInputStream(InputStream in) {
	    super(in);
	  }
	  public long skip(long n) throws IOException {
	    long m = 0L;
	    while (m < n) {
	      long _m = in.skip(n-m);
	      if (_m == 0L) break;
	      m += _m;
	    }
	    return m;
	  }
	}
}
