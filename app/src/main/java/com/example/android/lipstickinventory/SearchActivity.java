package com.example.android.lipstickinventory;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lipstickinventory.Networking.Search;
import com.example.android.lipstickinventory.Networking.SearchLoader;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Search>> {

    //Constant claye for search loader ID
    private static final int SEARCH_LOADER_ID = 1;

    //URL for search data from Google API
    private String GOOGLE_API =
            "https://www.googleapis.com/customsearch/v1?key=AIzaSyA-TZNSV-1Uch5-" +
                    "QewnoCu7VJOl0J_hoCg&cx=016731500014156530570:b3tzwkplw9g&q=";

    //Adapter for list of results
    private SearchAdapter mAdapter;

    //TextView when it's empty
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        /**
         * Find a reference to the {@link android.widget.ListView} in the layout
         */
        ListView searchListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        searchListView.setEmptyView(mEmptyStateTextView);

        //find edit text
        final EditText editTextView = (EditText) findViewById(R.id.search_text_view);

        //Click listener
        editTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //Find search text and create variable
                String textQuery = editTextView.getText().toString();
                String adjustedQuery = textQuery.replace(" ", "+");
                GOOGLE_API = "https://www.googleapis.com/customsearch/v1?key=AIzaSyA-TZNSV-1Uch5-" +
                        "QewnoCu7VJOl0J_hoCg&cx=016731500014156530570:b3tzwkplw9g&q=lipstick+" +
                        adjustedQuery;

                if (checkConnectivity()) {
                    //configure loader manager
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.restartLoader(1, null, SearchActivity.this);
                } else {
                    //set list as invisible
                    View list = findViewById((R.id.list));
                    list.setVisibility(View.GONE);
                    //popup empty state
                    mEmptyStateTextView.setVisibility(View.VISIBLE);
                    mEmptyStateTextView.setText(R.string.no_internet_connection);
                }
                return false;
            }
        });

        //Create new search adapter that takes an empty list of results
        mAdapter = new SearchAdapter(this, new ArrayList<Search>());

        //Set adapter on the ListView so that list can be populated
        searchListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected result.
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the article that was clicked
                Search currentResult = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri articleUri = Uri.parse(currentResult.getWebUrl());

                // check for internet
                if (checkConnectivity()) {
                    // Create a new intent to view the news URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);
                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                } else {
                    Toast noInternetPopUP = Toast.makeText(getApplicationContext(), "Unable to retrieve result. \n" +
                            "Please check your network settings.", Toast.LENGTH_SHORT);
                    noInternetPopUP.show();
                }
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(SEARCH_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<Search>> onCreateLoader(int i, Bundle bundle) {
        return new SearchLoader(this, GOOGLE_API);
    }

    @Override
    public void onLoadFinished(Loader<List<Search>> loader, List<Search> searches) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No articles found."
        mEmptyStateTextView.setText(R.string.no_results);

        // Clear the adapter of previous news data
        mAdapter.clear();

        // If there is a valid list of {@link News}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (searches != null && !searches.isEmpty()) {
            mAdapter.addAll(searches);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Search>> loader) {
        mAdapter.clear();
    }

    public boolean checkConnectivity() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(SEARCH_LOADER_ID, null, this);
            return true;
        } else {
            // Otherwise, display error
            // First, hide loading indicator and list so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            return false;
        }
    }
}
