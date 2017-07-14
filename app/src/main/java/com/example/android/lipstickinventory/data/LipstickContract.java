package com.example.android.lipstickinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for Lipstick Inventory app
 */

public class LipstickContract {

    //To prevent someone from accidentally instantiating contract class
    private LipstickContract () {}

    //Content authority
    public static final String CONTENT_AUTHORITY = "com.example.android.lipstickinventory";

    //Create base URIs to content with content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Possible path to lipstick data
    public static final String PATH_LIPSTICK = "lipstick";

    /**
     * Inner class to define constant vlaues for database table
     * Each entry is a single lipstick product
     */
    public static final class LipstickEntry implements BaseColumns {

        //content URI to access data
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LIPSTICK);

        //MIME type of the {@link #CONTENT_URI} for a list of lipstick
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIPSTICK;

        //MIME type for single lipstick
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIPSTICK;

        //Name of database
        public final static String TABLE_NAME = "lipsticks";

        //Unique ID number (used by table only), integer
        public final static String _ID = BaseColumns._ID;

        //Lipstick color (also name), text
        public final static String COLUMN_LIPSTICK_COLOR = "color";

        //Brand of lipstick, text
        public final static String COLUMN_LIPSTICK_BRAND = "brand";

        //Price of lipstick, integer
        public final static String COLUMN_LIPSTICK_PRICE = "price";

        //Quantity of lipstick, integer
        public final static String COLUMN_LIPSTICK_QUANTITY = "quantity";
    }
}
