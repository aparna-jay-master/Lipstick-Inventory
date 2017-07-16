package com.example.android.lipstickinventory.Networking;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Loads a list of search results over network request
 */

public class SearchLoader extends AsyncTaskLoader<List<Search>> {

    //Query URL
    private String mUrl;

    public SearchLoader (Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Search> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of articles.
        List<Search> searchResults = QueryUtils.fetchSearchData(mUrl);
        return searchResults;
    }
}
