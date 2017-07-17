package com.example.android.lipstickinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lipstickinventory.data.LipstickContract.LipstickEntry;

/**
 * adapter for grid view that uses lipstick data as source
 */

public class LipstickCursorAdapter extends CursorAdapter {

    //Construct LipstickCursorAdapter
    public LipstickCursorAdapter (Context context, Cursor c) {
        super(context, c, 0);
    }

    //Make new grid item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.grid_item,parent,false);
    }

    //binds data to grid item
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //Find text views we want to modify
        TextView colorView = (TextView) view.findViewById(R.id.grid_color);
        TextView quantityView = (TextView) view.findViewById(R.id.grid_quantity);
        TextView priceView = (TextView) view.findViewById(R.id.grid_price);

        //Find image view to modify
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.grid_image);

        //Find columns on lipstick table for each attribute
        int idColumnIndex = cursor.getColumnIndex(LipstickEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_IMAGE);
        int colorColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_COLOR);
        int priceColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_QUANTITY);

        //Read Lipstick attributes from Cursor of current entry
        byte[] lipstickImage = cursor.getBlob(imageColumnIndex);
        String lipstickColor = cursor.getString(colorColumnIndex);
        final int lipstickQuantity = cursor.getInt(quantityColumnIndex);
        int lipstickPrice = cursor.getInt(priceColumnIndex);

        //get row id
        final int rowID = cursor.getInt(idColumnIndex);

        //Convert price into dollars
        int lipstickDollars = lipstickPrice/100;

        //Update Text Views
        colorView.setText(lipstickColor);
        quantityView.setText("Quantity: " + lipstickQuantity);
        priceView.setText("$" + lipstickDollars);

        //Update ImageView by converting to bitmap
        Bitmap lipstickBitmap = BitmapFactory.decodeByteArray(lipstickImage, 0, lipstickImage.length);
        pictureImageView.setImageBitmap(lipstickBitmap);

        Button saleButton = (Button) view.findViewById(R.id.grid_sale_button);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentLipstickUri = ContentUris.withAppendedId(LipstickEntry.CONTENT_URI, rowID);
                makeSale(context, lipstickQuantity, currentLipstickUri);
            }
        });
    }

    /**
     * Reduces quantity by 1
     *
     * @param context activity context
     * @param uriLipstick URI to update lipstick
     * @param quantity current quantity
     */
    private void makeSale (Context context, int quantity, Uri uriLipstick) {
        if (quantity == 0) {
            Log.v("LipstickCursorAdpter", "quantity cannot be reduced");
            //Toast.makeText(this, context.getString(R.string.detail_quantity_negative),
                    //Toast.LENGTH_SHORT).show();
        } else {
            int newQuantity = quantity - 1;
            Log.v("LipstickCursorAdpter", "new quantity is " + newQuantity);

            //Create content value
            ContentValues values = new ContentValues();
            values.put(LipstickEntry.COLUMN_LIPSTICK_QUANTITY, newQuantity);
            int rowsAffected = context.getContentResolver().update(uriLipstick, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                Log.v("LipstickCursorAdapter", "no rows changed");
                // If no rows were affected, then there was an error with the update.
                //Toast.makeText(this,
                        //context.getString(R.string.update_lipstick_fail_message),
                        //Toast.LENGTH_SHORT).show();
            } else {
                Log.v("LipstickCursorAdpter", "rows changed = " + rowsAffected);
                // Otherwise, the update was successful and we can display a toast.
                //Toast.makeText(this,
                        //context.getString(R.string.update_lipstick_success_message),
                        //Toast.LENGTH_SHORT).show();
            }
        }
    }
}
