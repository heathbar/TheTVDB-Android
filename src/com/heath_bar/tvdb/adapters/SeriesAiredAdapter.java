package com.heath_bar.tvdb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class SeriesAiredAdapter extends SimpleCursorAdapter {

	public String[] _from;
	public int[] _to;
	public int[] _colors;
	
    public SeriesAiredAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int[] colors) throws Exception {
    	super(context, layout, c, from, to, flags);
    	if (to.length != 3 || from.length != 3)
    		throw new Exception("SeriesAiredAdapter requires exactly 3 'to' and 'from' elements.");
    	
    	_from = from.clone();
    	_to = to;
    	_colors = colors;
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
	        holder.textView2.setText("Last Aired: " + cursor.getString(holder.column2));
	        if (cursor.getString(holder.column3).equals("Unknown"))
	        	holder.textView3.setText("");
	        else
	        	holder.textView3.setText("Next Aired: " + cursor.getString(holder.column3));
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