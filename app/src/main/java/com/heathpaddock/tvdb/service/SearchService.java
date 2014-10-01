package com.heathpaddock.tvdb.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
import com.heathpaddock.tvdb.data.GetSeriesParser;
import com.heathpaddock.tvdb.model.TvShow;

public class SearchService extends ServiceBase {

    private static final String URL = "http://www.thetvdb.com/api/GetSeries.php?seriesname=";

    public List<TvShow> performSearch(String query, String language) {
        try {
            InputStream stream = null;
            List<TvShow> seriesList = null;

            try {
                stream = downloadUrl(URL + URLEncoder.encode(query, "utf-8"));

                GetSeriesParser parser = new GetSeriesParser();
                seriesList = parser.parse(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return seriesList;
        } catch (IOException e) {
            return new ArrayList<TvShow>();
        } catch (XmlPullParserException e) {
            return new ArrayList<TvShow>();
        }
    }
}
