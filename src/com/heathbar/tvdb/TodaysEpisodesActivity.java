package com.heathbar.tvdb;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class TodaysEpisodesActivity extends ListActivity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list);
        
	    String listItems[]={"Apples", "Bananas", "Coconuts", "Dates", "Eggs"};
		setListAdapter(new ArrayAdapter<String>(this, R.layout.generic_text_row, listItems));
	    getListView().setOnItemClickListener(new ItemClickedListener());
    }
	
	/** Handle item clicks */
    private class ItemClickedListener implements OnItemClickListener {
    	@Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
    		
    	}
    }
}
