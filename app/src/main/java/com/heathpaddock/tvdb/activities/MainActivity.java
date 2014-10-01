package com.heathpaddock.tvdb.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.heath_bar.tvdb.R;
import com.heathpaddock.tvdb.adapter.FavoritesListAdapter;
import com.heathpaddock.tvdb.adapter.SearchResultsListAdapter;
import com.heathpaddock.tvdb.model.TvShow;
import com.heathpaddock.tvdb.service.FavoritesService;
import com.heathpaddock.tvdb.service.SearchService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;


public class MainActivity extends ListActivity {

    private FavoritesListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Init the image loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(5)
                .memoryCacheSizePercentage(40)
                .build();
        ImageLoader.getInstance().init(config);

        new LoadFavoritesTask(this).execute("BFB8F86F8C7FB1C0");
    }

    // Class to load the search response asynchronously
    private class LoadFavoritesTask extends AsyncTask<String, Void, List<TvShow>> {
        private Context context;

        public LoadFavoritesTask(Context context) {
            this.context = context;
        }
        @Override
        protected List<TvShow> doInBackground(String... query) {

            try {
                // Search the tvdb API
                FavoritesService tvdbFavoritesService = new FavoritesService();
                return tvdbFavoritesService.getFavorites(query[0]);

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<TvShow> results){

            // Setup the list adapter with the data from the web call
            adapter = new FavoritesListAdapter(context, results);

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
