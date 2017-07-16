package com.example.android.lipstickinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.lipstickinventory.data.LipstickContract.LipstickEntry;

import static android.R.attr.name;

/**
 * Created by aparnajayaraman on 7/13/17.
 */

public class LipstickProvider extends ContentProvider {

    public static final String LOG_TAG = LipstickProvider.class.getSimpleName();

    //URI matcher code for entire table
    private static final int LIPSTICKS = 100;
    //URI matcher code for single entry
    private static final int LIPSTICK_ID = 101;

    //UriMatcher object to match content URI for corresponding code
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The content URI for multiple rows
        sUriMatcher.addURI(LipstickContract.CONTENT_AUTHORITY, LipstickContract.PATH_LIPSTICK, LIPSTICKS);

        // The content URI for single row
        sUriMatcher.addURI(LipstickContract.CONTENT_AUTHORITY,
                LipstickContract.PATH_LIPSTICK + "/#", LIPSTICK_ID);
    }

    //Database helper
    private LipstickDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new LipstickDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LIPSTICKS:
                // Query the Lipstick table.
                // The cursor could contain multiple rows of the Lipstick table.
                cursor = database.query(LipstickEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case LIPSTICK_ID:
                // Extract out the ID from the URI.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = LipstickEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the lipstick table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(LipstickEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LIPSTICKS:
                return insertLipstick(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert lipstick into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertLipstick(Uri uri, ContentValues values) {

        // Check that the color is not null
        String color = values.getAsString(LipstickEntry.COLUMN_LIPSTICK_COLOR);
        if (color == null) {
            throw new IllegalArgumentException("Lipstick must have a color");
        }

        // Check price is there
        Integer price = values.getAsInteger(LipstickEntry.COLUMN_LIPSTICK_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        // Check quantity is there
        Integer quantity = values.getAsInteger(LipstickEntry.COLUMN_LIPSTICK_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(LipstickEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LIPSTICKS:
                return updateLipstick(uri, contentValues, selection, selectionArgs);
            case LIPSTICK_ID:
                // For the Lipstick entry code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = LipstickEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateLipstick(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update lipsticks in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more lipsticks).
     * Return the number of rows that were successfully updated.
     */
    private int updateLipstick(Uri uri, ContentValues values,
                               String selection, String[] selectionArgs) {

        // If the color key is present check that the color value is not null.
        if (values.containsKey(LipstickEntry.COLUMN_LIPSTICK_COLOR)) {
            String name = values.getAsString(LipstickEntry.COLUMN_LIPSTICK_COLOR);
            if (name == null) {
                throw new IllegalArgumentException("Lipstick must have a name");
            }
        }
        // If price is valid
        if (values.containsKey(LipstickEntry.COLUMN_LIPSTICK_PRICE)) {
            // Check that price is greater than or equal to 0
            Integer price = values.getAsInteger(LipstickEntry.COLUMN_LIPSTICK_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Lipstick must have a price");
            }
        }

        // If quantity is valid
        if (values.containsKey(LipstickEntry.COLUMN_LIPSTICK_QUANTITY)) {
            // Check that quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(LipstickEntry.COLUMN_LIPSTICK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Lipstick must have a price");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(LipstickEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

}
