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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.heath_bar.tvdb.R;

public class LazyBitmapAdapter extends BaseAdapter {

	private int layout;
    private int imageview;    
    private BitmapLoader loader;
    private	 static LayoutInflater inflater = null;
 
    public LazyBitmapAdapter(Activity activity, WebImageList banners, int layout, int imageview){
	
    	this.layout = layout;
    	this.imageview = imageview;

        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        loader = new BitmapLoader(activity, banners, true);
    }
    
	@Override
	public int getCount() {
		if (loader.getWebImageList() != null)
			return loader.getWebImageList().size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		if (loader.getWebImageList() != null)
			return loader.getWebImageList().get(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
        	convertView = inflater.inflate(layout, null);

        ImageView image = (ImageView)convertView.findViewById(imageview);
        ProgressBar progress = (ProgressBar)convertView.findViewById(R.id.progress);
        loader.Load(position, image, progress);

        return convertView;
	}

	public void clearMemoryCache(){
		loader.clearMemoryCache();
	}
	
	public void setFileCacheMaxSize(long maxSize){
		loader.setFileCacheMaxSize(maxSize);
	}
}
