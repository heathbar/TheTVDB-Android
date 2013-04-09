package com.heath_bar.tvdb.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.heath_bar.tvdb.AppSettings;
import com.heath_bar.tvdb.R;
import com.heath_bar.tvdb.types.TvEpisode;
import com.heath_bar.tvdb.types.TvEpisodeList;

public class EpisodeAdapter extends BaseExpandableListAdapter {

	protected Context mContext;
	protected TvEpisodeList mEpisodeList;
	protected LayoutInflater mInflater;
	protected float mTextSize;
	
	public EpisodeAdapter(Context context, TvEpisodeList eps, float baseTextSize){
		mContext = context;
		mEpisodeList = eps;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTextSize = baseTextSize;
	}
	
	@Override
	public int getGroupCount() {
		return mEpisodeList.getNumberOfSeasons();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mEpisodeList.getNumberOfEpisodesInSeason(groupPosition);
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mEpisodeList.getSeasonList().get(groupPosition);	// I think this will just return the season number or explode if the season doesn't exist
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		try {
			return mEpisodeList.getEpisode(groupPosition, childPosition+1).getId();
		}catch (NullPointerException e){
			return 0;
		}
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null){
			convertView = mInflater.inflate(R.layout.season_row, parent, false);
		}
		convertView.setBackgroundColor(AppSettings.listBackgroundColors[groupPosition % AppSettings.listBackgroundColors.length]);
		
		TextView text = (TextView)convertView.findViewById(R.id.text);
		text.setTextSize(mTextSize*1.6f);
		
		if (groupPosition == 0)
			text.setText("Specials");
		else
			text.setText("Season " + groupPosition);
		
		if (isExpanded)
			text.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.arrow_down), null, null, null);
		else
			text.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.arrow_right), null, null, null);
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null){
			convertView = mInflater.inflate(R.layout.episode_row, parent, false);
		}
		convertView.setBackgroundColor(AppSettings.listBackgroundColors[(groupPosition+childPosition+1) % AppSettings.listBackgroundColors.length]);
		
		TextView text = (TextView)convertView.findViewById(R.id.text);
		final TvEpisode ep = mEpisodeList.getEpisode(groupPosition, childPosition+1);
		String nameText = (ep != null) ? String.format("%02d", childPosition+1) + " " + ep.getName(): String.format("%02d", childPosition+1) + " Unknown";
		text.setText(nameText);
		text.setTextSize(mTextSize);

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}



}
