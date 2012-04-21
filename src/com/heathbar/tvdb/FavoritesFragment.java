package com.heathbar.tvdb;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FavoritesFragment extends ListFragment {
	
	private ArrayList<TvSeries> seriesList;
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	    		
	}
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    /*
	    // Get the list of shows
        TvDb tvdb = new TvDb();
        seriesList = tvdb.getSeriesList("bang");
	
        if (seriesList !=null){

        	// Extract the series names
			String[] nameList = new String[seriesList.size()];
			for (int i=0; i<seriesList.size(); i++)
				nameList[i] = seriesList.get(i).getName();
			
			// Bind the list of names to the UI
			setListAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.generic_text_row, nameList));
		    getListView().setOnItemClickListener(new ItemClickedListener());
        }
        */
	}

	/** Handle item clicks */
    private class ItemClickedListener implements OnItemClickListener {
    	@Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
    		Toast.makeText(getActivity().getApplicationContext(), seriesList.get(position).getId(), Toast.LENGTH_SHORT).show();
    	}
    }
}
