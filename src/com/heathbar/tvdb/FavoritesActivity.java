package com.heathbar.tvdb;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class FavoritesActivity extends ListActivity {
			
	private ArrayList<TvSeries> seriesList;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list);
        
        // Get the list of shows
        TvDb tvdb = new TvDb();
        seriesList = tvdb.getSeriesList("bang");
	
        if (seriesList !=null){

        	// Extract the series names
			String[] nameList = new String[seriesList.size()];
			for (int i=0; i<seriesList.size(); i++)
				nameList[i] = seriesList.get(i).getName();
			
			// Bind the list of names to the UI
			setListAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.generic_text_row, nameList));
		    getListView().setOnItemClickListener(new ItemClickedListener());
        }
    }
	
	/** Handle item clicks */
    private class ItemClickedListener implements OnItemClickListener {
    	@Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
    		//Toast.makeText(getApplicationContext(), seriesList.get(position).getId(), Toast.LENGTH_SHORT).show();
    		
    		Intent myIntent = new Intent(arg0.getContext(), SeriesActivity.class);
    		startActivityForResult(myIntent, 0);
    		
    	}
    }
	
}
