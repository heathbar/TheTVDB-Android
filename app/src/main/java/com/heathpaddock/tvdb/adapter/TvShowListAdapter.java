package com.heathpaddock.tvdb.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.heath_bar.tvdb.R;
import com.heathpaddock.tvdb.model.TvShow;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

public class TvShowListAdapter extends BaseAdapter {

    protected int _layout;
    protected List<TvShow> _data;
    protected static LayoutInflater _inflater = null;
    protected ImageLoader _imageLoader;
    protected Drawable _loadingBanner;
    protected Drawable _defaultBanner;

    public TvShowListAdapter(Context context, List<TvShow> data) {
        _data = data;
        _inflater = LayoutInflater.from(context);
        _imageLoader = ImageLoader.getInstance();
        _loadingBanner = context.getResources().getDrawable(R.drawable.banner_loading);
        _defaultBanner = context.getResources().getDrawable(R.drawable.tvdb_banner);

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
            //return Long.valueOf(_data.get(position-1).getId()); // minus 1 for header row
            return Long.valueOf(_data.get(position).getId());
        else
            return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        return convertView;
    }
}
