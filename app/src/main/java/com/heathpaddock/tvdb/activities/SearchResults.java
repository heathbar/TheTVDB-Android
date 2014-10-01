package com.heathpaddock.tvdb.activities;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.heath_bar.tvdb.R;
import com.heathpaddock.tvdb.adapter.SearchResultsListAdapter;
import com.heathpaddock.tvdb.model.TvShow;
import com.heathpaddock.tvdb.service.SearchService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;


public class SearchResults extends ListActivity {

    private SearchResultsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            String languageCode = "en";
            new SearchTask(this).execute(query, languageCode);
        }
    }

    // Class to load the search response asynchronously
    private class SearchTask extends AsyncTask<String, Void, List<TvShow>> {
        private Context context;
        public SearchTask(Context context) {
            this.context = context;
        }
        @Override
        protected List<TvShow> doInBackground(String... query) {

            try {
                // Search the tvdb API
                SearchService tvdbApiSearch = new SearchService();
                return tvdbApiSearch.performSearch(query[0], query[1]);

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<TvShow> results){

            // Setup the list adapter with the data from the web call
            adapter = new SearchResultsListAdapter(context, results);

            setListAdapter(adapter);
            //getListView().setOnItemClickListener(new ItemClickedListener());

//            ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
//            progress.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_search) {
            onSearchRequested();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
