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
package com.heath_bar.tvdb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;


/** Helper class to download/re-sample/copy/delete images used in this application*/ 
public class ImageUtil  {
	
	
	/** Load a URL into a Bitmap */
	public static Bitmap LoadImage(Context ctx, String url){
		return LoadImage(ctx, url, 0, 0);
	}
	
	/** Load a URL and re-sample it into a Bitmap */
	public static Bitmap LoadImage(Context ctx, String url, int maxWidth, int maxHeight){
		
		// 1. Check if the file is in the cache
		String cachedFile = getCachedFileName(ctx, url);
		File f = new File(cachedFile);
		if (!f.exists())
		{
			downloadBitmap(url, cachedFile);
		}else{
			// 2. Check if the file is more than a day old
			Date now = new Date();
			long oneDay = 86400;
			if (f.lastModified()/1000 + oneDay < now.getTime()/1000)
				downloadBitmap(url, cachedFile);
		}
		
		if (maxWidth == 0 || maxHeight == 0)
			return BitmapFactory.decodeFile(cachedFile);
		else
			return decodeSampledBitmapFromFile(cachedFile, maxWidth, maxHeight);
	}
	
	/** This function creates the HTTP request and saves the image to the application's cache folder */
	public static void downloadBitmap(String url, String targetPath) {
    	HttpParams params = new BasicHttpParams();
    	params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    	HttpClient client = new DefaultHttpClient(params);
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) { 
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
                return;
            }
            
            final HttpEntity entity = response.getEntity();
            if (entity != null){
            	InputStream iStream = null;
            	PatchInputStream pStream = null;
            	
                try {
                	               	               	
                    iStream = entity.getContent();
                    pStream = new PatchInputStream(iStream);
                    
                    createCachedFile(targetPath, pStream);
                    
                } catch(Exception e){
                	Log.e("ImageGetter", e.getMessage());
                } finally {
                	if (pStream != null) {
                    	pStream.close();  
                    }
                	if (iStream != null) {
                    	iStream.close();  
                    }
                	entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            Log.w("ImageDownloader", "Error while retrieving bitmap from " + url + e.toString());
        }
    }
	
	
	/* This fixes the issue outlined here: http://code.google.com/p/android/issues/detail?id=6066 */
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
	
	
	/** Given a URL, this method returns the path where TheTVDB would cache the image. 
	 * This method makes the dangerous assumption that the URL ends in a file name
	 * and does not contain a query string */
	public static String getCachedFileName(Context ctx, String url){
		String subdir = "misc";
        if (url.contains("fanart")){
        	subdir = "banners" + File.separator + "fanart";
        }else if (url.contains("blank")){
        	subdir = "banners" + File.separator + "blank";
        }else if (url.contains("graphical")){
        	subdir = "banners" + File.separator + "graphical";
        }else if (url.contains("text")){
        	subdir = "banners" + File.separator + "text";
        }else if (url.contains("actors")){
        	subdir = "actors";
        }
        
        if (url.contains("_cache"))
        	subdir = "thumbnail" + File.separator + subdir;
        
        // This code assumes the URL contains a filename at the end which is prone to cause problems
        int slashIndex = url.lastIndexOf('/');
        String baseName = url.substring(slashIndex + 1); 
        
        return ctx.getCacheDir() + File.separator + subdir + File.separator + baseName;
	}
	
	
	/** Given the*/
	private static void createCachedFile(String fileName, InputStream inStream) throws IOException {

	    File cacheFile = new File(fileName);
	    cacheFile.getParentFile().mkdirs();
	    cacheFile.createNewFile();
	 
	    FileOutputStream out = new FileOutputStream(cacheFile);
	    byte buf[]=new byte[1024];
	    int len;
	    while((len=inStream.read(buf))>0)
	    	out.write(buf,0,len);
	    out.close();
	    inStream.close();
	}
	
	
	
	
	
//	//** This function is used to remove old images that were cached when the user requested to share them */
//	public static void deleteJPGsFromFolder(File dir){
//		if (dir.isDirectory()){
//			try {
//				
//				FileFilter filter = new FileFilter() {
//					
//					@Override
//					public boolean accept(File pathname) {
//						Date now = new Date();
//						if (pathname.lastModified() + 86400 < now.getTime())			// if the file is older than a day
//							if (pathname.getName().toLowerCase().endsWith(".jpg"))		// if it's a jpg
//								return true;
//							else
//								return false;
//						else
//							return false;
//					}
//				};
//				
//				File[] jpegList = dir.listFiles(filter);
//				for (int i=0; i<jpegList.length; i++)
//					if (jpegList[i].isFile())
//						jpegList[i].delete();
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
//	public static void createCachedFile(Context context, String fileName, String content) throws IOException {
// 
//	    File cacheFile = new File(context.getCacheDir() + File.separator + fileName);
//	    cacheFile.createNewFile();
//	 
//	    FileOutputStream fos = new FileOutputStream(cacheFile);
//	    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
//	    PrintWriter pw = new PrintWriter(osw);
//	 
//	    pw.println(content);
//	 
//	    pw.flush();
//	    pw.close();
//	}

	
	
	//** METHODS FOR RESIZING/RESAMPLING IMAGES ******************************************************************************************************/
	
	
	/** Given a file and a new file name (read: new name, not new entire path), 
	 * copy the file to the publicly accessible shared folder on the sdcard. 
	 * Note: this is really just a hack for gmail. Most other applications
	 * can read from anywhere, but gmail insists that the file be located 
	 * under file:///mnt/sdcard/
	 * @param fromFile file to copy from
	 * @param newName new file name to be saved under Environment.getAbsolutePath()/tmp/thetvdb
	 * @return the absolute path to the new file
	 */
	public static String copyToShareFolder(String fromFile, String newName){
		
		String sharedFile = null;
		FileChannel source = null;
	    FileChannel destination = null;

		try{
			File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cache" + File.separator + "TheTVDB" + File.separator + newName);
			newFile.getParentFile().mkdirs();
			newFile.createNewFile();
			sharedFile = newFile.getAbsolutePath();
			
		    try {
		    	source = new FileInputStream(fromFile).getChannel();
		    	destination = new FileOutputStream(newFile).getChannel();
		        destination.transferFrom(source, 0, source.size());
		    } finally {
		        if(source != null)
		            source.close();
		        if(destination != null)
		        	destination.close();
		    }
		} catch (IOException e){
			e.printStackTrace();
		}
		return sharedFile;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromFile(String fileName, int reqWidth, int reqHeight){
		
		// First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(fileName, options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(fileName, options);
	}
	
	
	public static void emptyShareFolder(){
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tmp" + File.separator + "thetvdb");
		if (dir.isDirectory()){
			try {
				
				File[] fileList = dir.listFiles();
				for (int i=0; i<fileList.length; i++)
					if (fileList[i].isFile())
						fileList[i].delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
