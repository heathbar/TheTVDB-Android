package com.heathpaddock.tvdb.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.heath_bar.tvdb.R;
import com.heathpaddock.tvdb.model.TvShow;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import java.util.List;

public class FavoritesListAdapter extends TvShowListAdapter {

    public FavoritesListAdapter(Context context, List<TvShow> data) {
        super(context, data);
    }

    private View inflateView(int position) {
        if (_data.get(position).getStatus().toLowerCase().equals("continuing")) {
            return _inflater.inflate(R.layout.tv_show_card_continuing, null);
        } else {
            return _inflater.inflate(R.layout.tv_show_card_ended, null);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        convertView = inflateView(position);

        holder = new ViewHolder();
        holder.id = 0;
        holder.name = (TextView) convertView.findViewById(R.id.tv_show_name);
        holder.banner = (ImageView) convertView.findViewById(R.id.image);
        holder.bannerLoading = (ProgressBar) convertView.findViewById(R.id.progress);
        holder.airsDay = (TextView) convertView.findViewById(R.id.airs_day);
        holder.airsNetwork = (TextView) convertView.findViewById(R.id.airs_network);
        holder.airsDuration = (TextView) convertView.findViewById(R.id.airs_duration);
        convertView.setTag(holder);

        TvShow show = _data.get(position);

        holder.id = Integer.parseInt(show.getId());
        holder.name.setText(show.getName());
        if (show.getStatus().equals("Continuing")) {
            holder.airsDay.setText(show.getAirDay());
        }
        holder.airsNetwork.setText(show.getNetwork());
        holder.airsDuration.setText(show.getRuntime() + " Minutes");

        String banner = show.getBanner();

        if (banner != null && !banner.equals("")) {
            String urlBase = "http://thetvdb.com/banners/";
            holder.bannerLoading.setVisibility((View.VISIBLE));
            //holder.Banner.setVisibility((View.INVISIBLE));
            holder.banner.setImageDrawable(_loadingBanner);
            final ProgressBar progress = holder.bannerLoading;

            _imageLoader.displayImage(urlBase + banner, holder.banner, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
                    a.setDuration(400);
                    view.setAnimation(a);
                    progress.setVisibility(View.GONE);
                }
            });
        } else {
            holder.banner.setImageDrawable(_defaultBanner);
            holder.bannerLoading.setVisibility(View.GONE);
        }

        if (show.getStatus().equals("Continuing")){
            // TODO: add last/next episodes
        }

        return convertView;
    }

    static class ViewHolder {
        int id;
        TextView name;
        ImageView banner;
        ProgressBar bannerLoading;
        TextView airsDay;
        TextView airsNetwork;
        TextView airsDuration;
        View divider1;
        TextView lastEpisodeName;
        TextView lastEpisodeNumber;
        TextView nextEpisodeName;
        TextView nextEpisodeNumber;
    }
}
