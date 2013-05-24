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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

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
import com.heath_bar.tvdb.util.DateUtil;

public class EpisodeAdapter extends BaseExpandableListAdapter {

	protected Context mContext;
	protected TvEpisodeList mEpisodeList;
	protected ArrayList<Integer> mSeasonList;
	protected LayoutInflater mInflater;
	protected float mTextSize;
	
	public EpisodeAdapter(Context context, TvEpisodeList eps, float baseTextSize){
		mContext = context;
		mEpisodeList = eps;
		mSeasonList = mEpisodeList.getSeasonList();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTextSize = baseTextSize;
		Collections.sort(mSeasonList);
	}
	
	@Override
	public int getGroupCount() {
		return mSeasonList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mEpisodeList.getNumberOfEpisodesInSeason(mSeasonList.get(groupPosition));
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
			return mEpisodeList.getEpisode(mSeasonList.get(groupPosition), childPosition+1).getId();
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

		int color = AppSettings.listBackgroundColors[groupPosition % AppSettings.listBackgroundColors.length];
		convertView.setBackgroundColor(color);
		
		TextView text = (TextView)convertView.findViewById(R.id.text);
		text.setTextSize(mTextSize*1.6f);
		
		if (groupPosition == 0 && mSeasonList.get(0) == 0)
			text.setText("Specials");
		else
			text.setText("Season " + mSeasonList.get(groupPosition));
		
		if (isExpanded)
			text.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.ic_expanded), null, null, null);
		else
			text.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.ic_collapsed), null, null, null);
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null){
			convertView = mInflater.inflate(R.layout.episode_row, parent, false);
		}
		int color = AppSettings.listBackgroundColors[(groupPosition+childPosition+1) % AppSettings.listBackgroundColors.length];
		convertView.setBackgroundColor(color);
		
		final TvEpisode ep = mEpisodeList.getEpisode(mSeasonList.get(groupPosition), childPosition+1);
		String numText = String.format(Locale.getDefault(), "%02d", childPosition+1);
		String nameText = (ep != null) ? ep.getName(): "Episode " + numText;
		String dateText = (ep != null) ? DateUtil.toString(ep.getAirDate()) :"";
		
		TextView text = (TextView)convertView.findViewById(R.id.episode_number);
		text.setText(numText);
		text.setTextSize(mTextSize*2.0f);
		
		text = (TextView)convertView.findViewById(R.id.episode_name);
		text.setText(nameText);
		text.setTextSize(mTextSize);
		
		text = (TextView)convertView.findViewById(R.id.episode_date);
		text.setText(dateText);
		text.setTextSize(mTextSize*0.7f);

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}



}
