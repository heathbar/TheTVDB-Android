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
package com.heath_bar.tvdb.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SeriesAiredListAdapter extends SimpleCursorAdapter {

	public String[] _from;
	public int[] _to;
	public int[] _colors;
	private float _textSize;
	
    public SeriesAiredListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int[] colors) throws Exception {
    	//super(context, layout, c, from, to, flags);
    	super(context, layout, c, from, to);
    	if (to.length != 3 || from.length != 3)
    		throw new Exception("SeriesAiredAdapter requires exactly 3 'to' and 'from' elements.");
    	
    	_from = from.clone();
    	_to = to;
    	_colors = colors;
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	_textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
    }

		@Override
	    public void bindView(View view, Context context, Cursor cursor) {
	        super.bindView(view, context, cursor);

	        ViewHolder holder = (ViewHolder) view.getTag();
	        if (holder == null) {
	            holder = new ViewHolder();
	            holder.textView1 = (TextView) view.findViewById(_to[0]);
	            holder.textView2 = (TextView) view.findViewById(_to[1]);
	            holder.textView3 = (TextView) view.findViewById(_to[2]);
	            holder.column1 = cursor.getColumnIndexOrThrow(_from[0]);
                holder.column2 = cursor.getColumnIndexOrThrow(_from[1]);
	            holder.column3 = cursor.getColumnIndexOrThrow(_from[2]);
	            
	            view.setTag(holder);
	            view.setBackgroundColor(_colors[cursor.getPosition() % _colors.length]);
	        }

	        holder.textView1.setText(cursor.getString(holder.column1));
	        holder.textView1.setTextSize(_textSize*1.6f);
	        
	        holder.textView2.setText("Last Aired: " + cursor.getString(holder.column2));
	        holder.textView2.setTextSize(_textSize*0.7f);
	        
	        if (cursor.getString(holder.column3).equals("Unknown"))
	        	holder.textView3.setText("");
	        else
	        	holder.textView3.setText("Next Aired: " + cursor.getString(holder.column3));
	        holder.textView3.setTextSize(_textSize*0.7f);
	    }

	    static class ViewHolder {
	        TextView textView1;
	        TextView textView2;
	        TextView textView3;
	        int column1; 
	        int column2;
	        int column3;
	    }
	}