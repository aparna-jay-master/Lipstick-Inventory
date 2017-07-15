package com.example.android.lipstickinventory;


import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.GridView;

import com.example.android.lipstickinventory.data.LipstickContract;
import com.example.android.lipstickinventory.data.LipstickContract.LipstickEntry;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{

    //Identifier for lipstick data loader
    private static final int LIPSTICK_LOADER = 0;

    //Adapter for GridView
    LipstickCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open DetailActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        //Find GridView to populate lipstick data
        GridView lipstickGridView = (GridView) findViewById(R.id.grid);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        lipstickGridView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new LipstickCursorAdapter(this, null);
        lipstickGridView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        lipstickGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.v ("MainActivity", "this has been clicked");
                // Create new intent to go to details
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific lipstick that was clicked on
                Uri currentLipstickUri = ContentUris.withAppendedId(LipstickEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentLipstickUri);

                // Launch the {@link DetailActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(LIPSTICK_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertInventoryData() {
        // Create a ContentValues object where column names are the keys and we input inventory
        ContentValues values = new ContentValues();
        values.put(LipstickEntry.COLUMN_LIPSTICK_IMAGE, "no_image");
        values.put(LipstickEntry.COLUMN_LIPSTICK_COLOR, "Vibrant Red");
        values.put(LipstickEntry.COLUMN_LIPSTICK_BRAND, "Urban Decay");
        values.put(LipstickEntry.COLUMN_LIPSTICK_PRICE, 1500);
        values.put(LipstickEntry.COLUMN_LIPSTICK_QUANTITY, 7);

        // Insert a new row
        Uri newUri = getContentResolver().insert(LipstickEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all inventory in the database.
     */
    private void deleteAllInventory() {
        int rowsDeleted = getContentResolver().delete(LipstickEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from lipstick inventory");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_inventory_data:
                insertInventoryData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllInventory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        //Define projection for columns we care about
        String [] projection = {
                LipstickEntry._ID,
                LipstickEntry.COLUMN_LIPSTICK_IMAGE,
                LipstickEntry.COLUMN_LIPSTICK_COLOR,
                LipstickEntry.COLUMN_LIPSTICK_BRAND,
                LipstickEntry.COLUMN_LIPSTICK_PRICE,
                LipstickEntry.COLUMN_LIPSTICK_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                LipstickEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
