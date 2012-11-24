package com.heath_bar.tvdb.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class PopupMenuAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    String data[] = null;

    public PopupMenuAdapter(Context context, int layoutResourceId, String[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        // initialize a view first
        if (view == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
        }

        String item = data[position];
        TextView text = (TextView)view;
        text.setText(item);
 
        return view;
    }
}
