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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapWebUtil {

	Context context;
	
	public BitmapWebUtil(Context ctx){
		context = ctx;
	}
	
	/** This function creates the HTTP request and saves the image to the application's cache folder 
	 * @throws IOException */
	public Bitmap downloadBitmap(String url) throws IOException {
    	 
	     URL bitmapUrl = new URL(url);
	     HttpURLConnection conn = (HttpURLConnection)bitmapUrl.openConnection();
	     conn.setConnectTimeout(10000);
	     conn.setReadTimeout(30000);
	     conn.setInstanceFollowRedirects(true);
	     
	     InputStream iStream = conn.getInputStream();
	     PatchedInputStream pStream = new PatchedInputStream(iStream);
	
	     return BitmapFactory.decodeStream(pStream);
         
    }
	
	
	 
	
	
	/* This fixes the issue outlined here: http://code.google.com/p/android/issues/detail?id=6066 */
	public static class PatchedInputStream extends FilterInputStream {
	  public PatchedInputStream(InputStream in) {
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
