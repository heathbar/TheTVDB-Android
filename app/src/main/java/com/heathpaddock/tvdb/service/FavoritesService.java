package com.heathpaddock.tvdb.service;

import android.util.Log;

import com.heathpaddock.tvdb.data.GetSeriesParser;
import com.heathpaddock.tvdb.data.UserFavoritesParser;
import com.heathpaddock.tvdb.model.TvShow;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FavoritesService extends ServiceBase {

    private static final String URL = "http://thetvdb.com/api/User_Favorites.php?accountid=";

    public List<TvShow> getFavorites(String userId) {
        InputStream stream = null;
        List<TvShow> seriesList = null;
        DetailsService detailsService = new DetailsService();

        List<Integer> favIds = getFavoriteIds(userId);

        Log.e("FavoritesService", "Found " + favIds.size() + " favorites!");

        seriesList = new ArrayList<TvShow>();
        int i = 0;
        for (Integer id : favIds) {
            TvShow show = detailsService.getSeriesDetails(id.toString());
            seriesList.add(show);
            Log.e("Loaded", "Loaded " + show.getName());
        }
        return seriesList;
    }

    private List<Integer> getFavoriteIds(String userId) {
        try {
            InputStream stream = null;
            List<Integer> favoritesList = null;

            try {
                stream = downloadUrl(URL + userId);

                UserFavoritesParser parser = new UserFavoritesParser();
                favoritesList = parser.parse(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return favoritesList;
        } catch (IOException e) {
            return new ArrayList<Integer>();
        } catch (XmlPullParserException e) {
            return new ArrayList<Integer>();
        }
    }
}
