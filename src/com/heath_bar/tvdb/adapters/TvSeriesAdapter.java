package com.heath_bar.tvdb.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.heath_bar.tvdb.R;
import com.heath_bar.tvdb.types.TvSeries;

public class TvSeriesAdapter extends BaseAdapter
{

		private int _layout;
        private ArrayList<TvSeries> _data;
        private static LayoutInflater _inflater = null;
        private int[] _colors;
        private float _textSize;
       
        public TvSeriesAdapter(Context context, int layout, ArrayList<TvSeries> data, int[] colors){
            _layout = layout;
            _data = data;
            _inflater = LayoutInflater.from(context);
            _colors = colors;
            
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
         	_textSize = Float.parseFloat(settings.getString("textSize", "18.0"));
        }
        
        @Override
        public int getCount() 
        {
        	if (_data != null)
        		return _data.size();
        	else
        		return 0;        	
        }

        @Override
        public Object getItem(int position) 
        {
        	if (_data != null)
        		return _data.get(position);
        	else
        		return 0;
        }

        @Override
        public long getItemId(int position) 
        {
        	if (_data != null && position > 0 && position <= _data.size())
        		return Long.valueOf(_data.get(position-1).getId()); // minus 1 for header row
        	else
        		return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) 
        {
            ViewHolder holder;
            if (convertView == null){
            	convertView = _inflater.inflate(_layout, null);
                holder = new ViewHolder();
                holder.tvSeriesName = (TextView) convertView.findViewById(R.id.text);
                holder.tvSeriesId = (TextView) convertView.findViewById(R.id.hidden_text);

                convertView.setTag(holder);
           } 
           else 
           {
               holder = (ViewHolder) convertView.getTag();
           }
            holder.tvSeriesName.setText(_data.get(position).getName());
            holder.tvSeriesName.setTextSize(_textSize);
            holder.tvSeriesId.setText(String.valueOf(_data.get(position).getId()));
            holder.tvSeriesId.setTextSize(_textSize);
           
           convertView.setBackgroundColor(_colors[position % _colors.length]);
           
           return convertView;
     }
        
     static class ViewHolder {
    	 TextView tvSeriesName;
         TextView tvSeriesId;
     }
}