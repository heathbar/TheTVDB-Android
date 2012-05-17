package com.heathbar.tvdb;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

public class FavoritesFragment extends SherlockListFragment {
	
	private ArrayList<TvSeries> seriesList;
	private String[] nameList;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
        // Inflate the ListView layout file.
        return inflater.inflate(R.layout.list_fragment, null);
    }

	@Override
    public void onViewCreated(View arg0, Bundle arg1) {
        super.onViewCreated(arg0, arg1);
        
        // Get the list of shows
        TvDb tvdb = new TvDb();
        seriesList = tvdb.getSeriesList("bang");
	
        
        if (seriesList !=null){

        	// Extract the series names
			nameList = new String[seriesList.size()];
			for (int i=0; i<seriesList.size(); i++)
				nameList[i] = seriesList.get(i).getName();
			
		}
        Arrays.sort(nameList);
        
        setListAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, nameList));
        getListView().setOnItemClickListener(new ItemClickedListener());

	}


	/** Handle item clicks */
    private class ItemClickedListener implements OnItemClickListener {
    	@Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
    		Toast.makeText(getActivity().getApplicationContext(), seriesList.get(position).getId(), Toast.LENGTH_SHORT).show();
    	}
    }
}
