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

package com.heath_bar.tvdb.data.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.ImageViewer;
import com.heath_bar.tvdb.R;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapLoader;
import com.heath_bar.tvdb.data.adapters.lazylist.WebImage;
import com.heath_bar.tvdb.data.adapters.lazylist.WebImageList;
import com.heath_bar.tvdb.types.Actor;

public class CastAdapter extends BaseAdapter {

	protected Context context;
	protected int layout;
	protected List<Actor> actors;
	protected float textSize;
	private BitmapLoader loader;
	
	public CastAdapter(Context context, int layout, List<Actor> actors, float textSize){
		this.context = context;
		this.layout = layout;
		this.actors = actors;
		this.textSize = textSize;
		
		WebImageList imageList = new WebImageList();
		for (int i=0; i<actors.size(); i++)
			imageList.add(actors.get(i).getImage());

		loader = new BitmapLoader(context, imageList, false);
		loader.setResampleSize(100);
	}
	
	@Override
	public int getCount() {
		return actors.size();
	}

	@Override
	public Object getItem(int position) {
		return actors.get(position);
	}

	@Override
	public long getItemId(int position) {
		return actors.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	convertView = inflater.inflate(layout, null);
		}
		
		final Actor theActor = actors.get(position);
		TextView t = (TextView)convertView.findViewById(R.id.actor_name);
		t.setText(theActor.getName());
		t.setTextSize(textSize*1.3f);
		
		t = (TextView)convertView.findViewById(R.id.character_name);
		t.setText(theActor.getRole());
		t.setTextSize(textSize);
		
		
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/find?q=" + theActor.getName().replace(' ', '+') + "&s=all"));
				context.startActivity(myIntent);				
			}
		});
		int color = AppSettings.listBackgroundColors[position % AppSettings.listBackgroundColors.length];
		if (color != context.getResources().getColor(R.color.background1))
			convertView.setBackgroundColor(color);
		
		ProgressBar progress = (ProgressBar)convertView.findViewById(R.id.progress_actors);
		ImageView image = (ImageView)convertView.findViewById(R.id.image);
		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WebImage webImage = (WebImage)theActor.getImage();		
	        	Intent myIntent = new Intent(context, ImageViewer.class);
	        	myIntent.putExtra("imageTitle", theActor.getName());
	        	myIntent.putExtra("imageId", webImage.getId());
	        	myIntent.putExtra("imageUrl", webImage.getUrl());
	        	loader.clearMemoryCache();
	    		context.startActivity(myIntent);
			}
		});
		loader.Load(position, image, progress);
		
		return convertView;
	}
	
	public void setFileCacheMaxSize(long maxSize){
		loader.setFileCacheMaxSize(maxSize);
	}

}
