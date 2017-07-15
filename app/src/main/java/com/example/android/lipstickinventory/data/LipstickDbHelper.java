package com.example.android.lipstickinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.lipstickinventory.data.LipstickContract.LipstickEntry;

/**
 * Database helper for Lipstick Inventory app
 * Manages database creation and version management
 */

public class LipstickDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = LipstickDbHelper.class.getSimpleName();

    //Name of database file
    private static final String DATABASE_NAME = "makeup.db";

    //Database version
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link LipstickDbHelper}
     * @param context
     */
    public LipstickDbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Called when database created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_LIPSTICK_TABLE =  "CREATE TABLE " + LipstickEntry.TABLE_NAME + " ("
                + LipstickEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LipstickEntry.COLUMN_LIPSTICK_IMAGE + " TEXT, "
                + LipstickEntry.COLUMN_LIPSTICK_COLOR + " TEXT NOT NULL, "
                + LipstickEntry.COLUMN_LIPSTICK_BRAND + " TEXT, "
                + LipstickEntry.COLUMN_LIPSTICK_PRICE + " INTEGER NOT NULL, "
                + LipstickEntry.COLUMN_LIPSTICK_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_LIPSTICK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Still in version one so will not do anything here
    }
}
