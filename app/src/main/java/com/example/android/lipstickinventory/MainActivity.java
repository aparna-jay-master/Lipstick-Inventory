package com.example.android.lipstickinventory;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lipstickinventory.data.LipstickContract.LipstickEntry;

import java.io.ByteArrayOutputStream;

import static android.R.attr.id;

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

        // Setup an Adapter to create a list item for each row of lipstick data in the Cursor.
        // There is no lipstick data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new LipstickCursorAdapter(this, null);
        lipstickGridView.setAdapter(mCursorAdapter);

        lipstickGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
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
     * Insert lipstick inventory
     */
    private void insertInventoryData() {
        //add first entry
        Drawable heroineImage = getDrawable(R.drawable.mac_heroine);
        addToDatabase(convertToByte(heroineImage), "Heroine", "MAC", 1500, 20);

        //add second entry
        Drawable furiousImage = getDrawable(R.drawable.estee_lauder_furious);
        addToDatabase(convertToByte(furiousImage), "Furious", "Estee Lauder", 1700, 53);

        //add third entry
        Drawable schiapImage = getDrawable(R.drawable.nars_schiap);
        addToDatabase(convertToByte(schiapImage), "Schiap", "NARS", 1300, 87);

        //add fourth entry
        Drawable parisianImage = getDrawable(R.drawable.bobbi_brown_parisian);
        addToDatabase(convertToByte(parisianImage), "Parisian", "Bobbi Brown", 2800, 47);
    }

    private byte[] convertToByte (Drawable imageFromFile) {
        //Convert to bitmap
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageFromFile);
        Bitmap bitmap = bitmapDrawable .getBitmap();
        //Convert to byte to store
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] imageByte = bos.toByteArray();
        return imageByte;
    }

    private void addToDatabase (byte[] image,
                                String colorString,
                                String brandString,
                                int priceInt,
                                int quantityInt) {
        // Create a ContentValues object where column names are the keys and we input inventory
        ContentValues values = new ContentValues();

        values.put(LipstickEntry.COLUMN_LIPSTICK_IMAGE, image);
        values.put(LipstickEntry.COLUMN_LIPSTICK_COLOR, colorString);
        values.put(LipstickEntry.COLUMN_LIPSTICK_BRAND, brandString);
        values.put(LipstickEntry.COLUMN_LIPSTICK_PRICE, priceInt);
        values.put(LipstickEntry.COLUMN_LIPSTICK_QUANTITY, quantityInt);

        // Insert a new row
        getContentResolver().insert(LipstickEntry.CONTENT_URI, values);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
            case R.id.action_search:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_inventory_data:
                insertInventoryData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
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
