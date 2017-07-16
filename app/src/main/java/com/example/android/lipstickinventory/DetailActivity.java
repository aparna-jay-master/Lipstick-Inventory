package com.example.android.lipstickinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.app.LoaderManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Loader;

import com.example.android.lipstickinventory.data.LipstickContract.LipstickEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.id.message;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //Identifier for lipstick data
    private static final int EXISTING_LIPSTICK_LOADER = 0;

    //Content URI for existing lipstick (and if null it's a new lipstick)
    private Uri mCurrentLipstickUri;

    //Image field to add image
    private ImageView mImageView;

    //Edit Texts
    private EditText mColorView;
    private EditText mBrandView;
    private EditText mPriceView;
    private EditText mQuantityView;

    //Buttons
    private Button mPlusButton;
    private Button mMinusButton;
    private Button mOrderButton;

    private boolean mLipstickHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mLipstickHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new lipstick or editing an existing one.
        Intent intent = getIntent();
        mCurrentLipstickUri = intent.getData();

        // If the intent DOES NOT contain a lipstick content URI, then we know that we are
        // creating a new one.
        if (mCurrentLipstickUri == null) {
            // This is a new lipstick, so change the app bar to say "Add Lipstick"
            setTitle(getString(R.string.detail_activity_title_new_lipstick));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a lipstick that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing lipstick, so change app bar to say "Edit Lipstick"
            setTitle(getString(R.string.detail_activity_title_current_lipstick));

            // Initialize a loader to read the lipstick data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_LIPSTICK_LOADER, null, this);
        }

        //Find all the relevant data views
        mImageView = (ImageView) findViewById(R.id.detail_lipstick_image_view);
        mColorView = (EditText) findViewById(R.id.detail_color_view);
        mBrandView = (EditText) findViewById(R.id.detail_brand_view);
        mQuantityView = (EditText) findViewById(R.id.detail_quantity_view);
        mPriceView = (EditText) findViewById(R.id.detail_lipstick_price);

        //Identify all button views
        mPlusButton = (Button) findViewById(R.id.detail_plus_button);
        mMinusButton = (Button) findViewById(R.id.detail_minus_button);
        mOrderButton = (Button) findViewById(R.id.detail_order_button);

        mImageView.setOnTouchListener(mTouchListener);
        mColorView.setOnTouchListener(mTouchListener);
        mBrandView.setOnTouchListener(mTouchListener);
        mPriceView.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);

        //Open camera when you press on image
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        //Plus and minus buttons
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityView.getText().toString().trim());
                quantity++;
                mQuantityView.setText(Integer.toString(quantity));
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityView.getText().toString().trim());
                if (quantity == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.detail_quantity_negative),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity = quantity - 1;
                    mQuantityView.setText(Integer.toString(quantity));
                }
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get text for color and brand to put in email
                String colorEmail = mColorView.getText().toString().trim();
                String brandEmail = mBrandView.getText().toString().trim();

                //Create email message
                String message = getString(R.string.email_message) +
                        "\n" + colorEmail +
                        " - " + brandEmail;

                //Send intent
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            mImageView.setImageBitmap(photo);
        }
    }

    private void saveLipstick() {
        //Read input fields
        //Use trim to eliminate leading or trailing white space
        Drawable imageImage = mImageView.getDrawable();
        //Convert to bitmap
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageImage);
        Bitmap bitmap = bitmapDrawable .getBitmap();
        //Convert to byte to store
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] imageByte = bos.toByteArray();

        //All the rest
        String colorString = mColorView.getText().toString().trim();
        String brandString = mBrandView.getText().toString().trim();
        String priceString = mPriceView.getText().toString().trim();
        String quantityString = mQuantityView.getText().toString().trim();

        if (mCurrentLipstickUri == null &&
                TextUtils.isEmpty(colorString) && TextUtils.isEmpty(brandString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)) {
            //Since nothing was edited no need to do anything
            return;
        }

        //Create ContentValues where column names are the keys and lipstick
        //attributes from the editor are values
        ContentValues values = new ContentValues();
        values.put(LipstickEntry.COLUMN_LIPSTICK_IMAGE, imageByte);
        values.put(LipstickEntry.COLUMN_LIPSTICK_COLOR, colorString);
        //if there is no price
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(LipstickEntry.COLUMN_LIPSTICK_PRICE, price);
        //if there is no quantity
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(LipstickEntry.COLUMN_LIPSTICK_QUANTITY, quantity);
        //if there's no brand
        String brand = "none";
        if (!TextUtils.isEmpty(brandString)) {
            brand = brandString;
        }
        ;
        values.put(LipstickEntry.COLUMN_LIPSTICK_BRAND, brand);

        //Determine if this is new or existing lipstick
        if (mCurrentLipstickUri == null) {
            //This is a new lipstick so insert lipstick
            Uri newUri = getContentResolver().insert(LipstickEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.new_lipstick_fail_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.new_lipstick_success_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //Otherwise if an existing lipstick update with content URI
            int rowsAffected = getContentResolver().update(mCurrentLipstickUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.update_lipstick_fail_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.update_lipstick_success_message),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new lipstick, hide the "Delete" menu item.
        if (mCurrentLipstickUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveLipstick();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the lipstick hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mLipstickHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mLipstickHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define projection with all items
        String[] projection = {
                LipstickEntry._ID,
                LipstickEntry.COLUMN_LIPSTICK_IMAGE,
                LipstickEntry.COLUMN_LIPSTICK_COLOR,
                LipstickEntry.COLUMN_LIPSTICK_BRAND,
                LipstickEntry.COLUMN_LIPSTICK_PRICE,
                LipstickEntry.COLUMN_LIPSTICK_QUANTITY};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentLipstickUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int imageColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_IMAGE);
            int colorColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_COLOR);
            int brandColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_BRAND);
            int priceColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(LipstickEntry.COLUMN_LIPSTICK_QUANTITY);

            byte[] image = cursor.getBlob(imageColumnIndex);
            String color = cursor.getString(colorColumnIndex);
            String brand = cursor.getString(brandColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            //Update views
            Bitmap lipstickBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            mImageView.setImageBitmap(lipstickBitmap);
            mColorView.setText(color);
            mBrandView.setText(brand);
            mPriceView.setText(Integer.toString(price));
            mQuantityView.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If loader is invalidated, clear out all data
        mColorView.setText("");
        mBrandView.setText("");
        mPriceView.setText("");
        mQuantityView.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
     * Prompt the user to confirm that they want to delete this lipstick.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteLipstick();
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
     * Perform the deletion of the lipstick in the database.
     */
    private void deleteLipstick() {
        // Only perform the delete if this is an existing lipstick.
        if (mCurrentLipstickUri != null) {
            // Call the ContentResolver to delete the lipstick at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentLipstickUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_lipstick_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_lipstick_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
