package com.heathpaddock.tvdb.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
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
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsListAdapter extends TvShowListAdapter {


    public SearchResultsListAdapter(Context context, List<TvShow> data) {
        super(context, data);
        _layout = R.layout.tv_show_card_search_result;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null){
            convertView = _inflater.inflate(_layout, null);
            holder = new ViewHolder();
            holder.tvShowId = 0;
            holder.tvShowName = (TextView) convertView.findViewById(R.id.tv_show_name);
            holder.tvShowBanner = (ImageView) convertView.findViewById(R.id.image);
            holder.tvShowBannerLoading = (ProgressBar) convertView.findViewById((R.id.progress));
            holder.tvShowNetwork = (TextView) convertView.findViewById(R.id.tv_show_network);
            holder.tvShowNetworkDivider = convertView.findViewById(R.id.tv_show_network_divider);
            holder.tvShowDescription = (TextView) convertView.findViewById(R.id.tv_show_description);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        TvShow show = _data.get(position);

        holder.tvShowId = Integer.parseInt(show.getId());
        holder.tvShowName.setText(show.getName());

        String network = show.getNetwork();
        if (network != null && !network.equals((""))) {
            holder.tvShowNetwork.setText(network);
            holder.tvShowNetwork.setVisibility(View.VISIBLE);
            holder.tvShowNetworkDivider.setVisibility(View.VISIBLE);
        } else {
            holder.tvShowNetwork.setVisibility(View.GONE);
            holder.tvShowNetworkDivider.setVisibility(View.GONE);
        }

        holder.tvShowDescription.setText(show.getDescription());


        String banner = show.getBanner();

        if (banner != null && !banner.equals("")) {
            String urlBase = "http://thetvdb.com/banners/";
            holder.tvShowBannerLoading.setVisibility((View.VISIBLE));
            //holder.tvShowBanner.setVisibility((View.INVISIBLE));
            holder.tvShowBanner.setImageDrawable(_loadingBanner);
            final ProgressBar progress = holder.tvShowBannerLoading;

            _imageLoader.displayImage(urlBase + banner, holder.tvShowBanner, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
                    a.setDuration(400);
                    view.setAnimation(a);
                    progress.setVisibility(View.GONE);
                }
            });
        } else {
            holder.tvShowBanner.setImageDrawable(_defaultBanner);
            holder.tvShowBannerLoading.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        int tvShowId;
        TextView tvShowName;
        ImageView tvShowBanner;
        ProgressBar tvShowBannerLoading;
        TextView tvShowNetwork;
        View tvShowNetworkDivider;
        TextView tvShowDescription;
    }
}
