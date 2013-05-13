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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.R;
import com.heath_bar.tvdb.data.adapters.lazylist.BitmapLoader;
import com.heath_bar.tvdb.data.adapters.lazylist.WebImage;
import com.heath_bar.tvdb.util.DateUtil;

public class SeriesAiredListAdapter extends SimpleCursorAdapter {

	private String[] _from;
	private int[] _to;
	private int[] _colors;
	private float _textSize;
	private boolean _useNiceDates;
	private BitmapLoader loader;
	
    @SuppressWarnings("deprecation")
	public SeriesAiredListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int[] colors, boolean useNiceDates) throws Exception {
    	super(context, layout, c, from, to);

    	if (to.length != 4 || from.length != 4)
    		throw new Exception("SeriesAiredAdapter requires exactly 3 'to' and 'from' elements.");
    	
    	_from = from.clone();
    	_to = to;
    	_colors = colors;
    	_useNiceDates = useNiceDates;
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	_textSize = Float.parseFloat(settings.getString("textSize", "18.0"));

    	loader = new BitmapLoader(context);
    }

		@Override
	    public void bindView(View view, Context context, Cursor cursor) {
			
	        ViewHolder holder = (ViewHolder) view.getTag();
	        if (holder == null) {
	            holder = new ViewHolder();
	            holder.textView1 = (TextView) view.findViewById(_to[0]);
	            holder.textView2 = (TextView) view.findViewById(_to[1]);
	            holder.textView3 = (TextView) view.findViewById(_to[2]);
	            holder.imageView = (ImageView) view.findViewById(_to[3]);
	            holder.columnTitle = cursor.getColumnIndexOrThrow(_from[0]);
                holder.columnLast = cursor.getColumnIndexOrThrow(_from[1]);
	            holder.columnNext = cursor.getColumnIndexOrThrow(_from[2]);
	            holder.columnPoster = cursor.getColumnIndexOrThrow(_from[3]);
	            view.setTag(holder);
	        }
	        view.setBackgroundColor(_colors[cursor.getPosition() % _colors.length]);

	        holder.textView1.setText(cursor.getString(holder.columnTitle));
	        holder.textView1.setTextSize(_textSize*1.6f);
	        
	        
	        if (_useNiceDates)
	        	holder.textView2.setText("Last Aired: " + DateUtil.toNiceString(cursor.getString(holder.columnLast)));
	        else
	        	holder.textView2.setText("Last Aired: " + cursor.getString(holder.columnLast));
	        holder.textView2.setTextSize(_textSize*0.7f);
	        
	        if (cursor.getString(holder.columnNext).equals("Unknown"))
	        	holder.textView3.setText("Next Aired: Unknown");
	        else if (cursor.getString(holder.columnNext).equals("ZZ"))		// ZZ = hack so that it shows up at the bottom when sorted
	        	holder.textView3.setText("Ended");
	        else if (_useNiceDates)
	        	holder.textView3.setText("Next Aired: " + DateUtil.toNiceString(cursor.getString(holder.columnNext)));
	        else
	        	holder.textView3.setText("Next Aired: " + cursor.getString(holder.columnNext));
	        holder.textView3.setTextSize(_textSize*0.7f);
	        
            String url = AppSettings.BANNER_URL + cursor.getString(holder.columnPoster);
            String filename = url.substring(url.lastIndexOf('/')+1);
            WebImage wi = new WebImage(filename, url, "");
            
            ViewGroup parent = (ViewGroup)holder.imageView.getParent();
            ProgressBar progress = (ProgressBar)parent.findViewById(R.id.progress_favorite_item);
            
            loader.setResampleSize(65);
            loader.Load(cursor.getPosition(), wi, holder.imageView, progress);
	    }
		
		

	    static class ViewHolder {
	        TextView textView1;
	        TextView textView2;
	        TextView textView3;
	        ImageView imageView;
	        int columnTitle; 
	        int columnLast;
	        int columnNext;
	        int columnPoster;
	    }
	}