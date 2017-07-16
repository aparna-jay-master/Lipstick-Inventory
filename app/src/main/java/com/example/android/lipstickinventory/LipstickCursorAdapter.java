package com.example.android.lipstickinventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lipstickinventory.data.LipstickContract.LipstickEntry;

import org.w3c.dom.Text;

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
    public void bindView(View view, Context context, Cursor cursor) {
        //Find text views we want to modify
        TextView colorView = (TextView) view.findViewById(R.id.grid_color);
        TextView quantityView = (TextView) view.findViewById(R.id.grid_quantity);
        TextView priceView = (TextView) view.findViewById(R.id.grid_price);

        //Find image view to modify
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.grid_image);

        //Find columns on lipstick table for each attribute
        int imageColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_IMAGE);
        int colorColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_COLOR);
        int priceColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_QUANTITY);

        //Read Lipstick attributes from Cursor of current entry
        byte[] lipstickImage = cursor.getBlob(imageColumnIndex);
        String lipstickColor = cursor.getString(colorColumnIndex);
        int lipstickQuantity = cursor.getInt(quantityColumnIndex);
        int lipstickPrice = cursor.getInt(priceColumnIndex);

        //Convert price into dollars
        int lipstickDollars = lipstickPrice/100;

        //Update Text Views
        colorView.setText(lipstickColor);
        quantityView.setText("Quantity: " + lipstickQuantity);
        priceView.setText("$" + lipstickDollars);

        //Update ImageView by converting to bitmap
        Bitmap lipstickBitmap = BitmapFactory.decodeByteArray(lipstickImage, 0, lipstickImage.length);
        pictureImageView.setImageBitmap(lipstickBitmap);
    }
}
