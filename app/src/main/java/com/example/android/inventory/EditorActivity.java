package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by diegog on 1/20/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mProviderEmail;
    private EditText mProviderName;
    private ImageButton mImageButton;
    private Bitmap mImageBitmap;


    private Uri currentUri;
    private int ITEM_LOADER = 0;
    public static final int GET_FROM_GALLERY = 0;

    private boolean itemChanged = false;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            itemChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentUri = intent.getData();

        if (currentUri ==  null ){
            setTitle(R.string.editor_activity_title_new_item);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_activity_title_edit_item);
        }


        mNameEditText = (EditText) findViewById(R.id.item_name);
        mPriceEditText = (EditText) findViewById(R.id.item_price);
        mQuantityEditText = (EditText) findViewById(R.id.item_quantity);
        mProviderEmail = (EditText) findViewById(R.id.item_provider_email);
        mProviderName = (EditText) findViewById(R.id.item_provider_name);
        mImageButton = (ImageButton) findViewById(R.id.item_image);


        mNameEditText.setOnTouchListener(onTouchListener);
        mPriceEditText.setOnTouchListener(onTouchListener);
        mQuantityEditText.setOnTouchListener(onTouchListener);
        mImageButton.setOnTouchListener(onTouchListener);
        mProviderName.setOnTouchListener(onTouchListener);
        mProviderEmail.setOnTouchListener(onTouchListener);

        mImageButton.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent,"Select Picture"),GET_FROM_GALLERY);
            }
        });



        getLoaderManager().initLoader(ITEM_LOADER, null,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            MenuItem orderItem = menu.findItem(R.id.action_order_item);
            menuItem.setVisible(false);
            orderItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_item:
                saveItem();
                finish();
                return true;
            case R.id.action_delete_item:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_order_item:
                orderItem(orderBody());
                return true;
            case android.R.id.home:
                if (!itemChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangeDialog(discardButtonClickListener);
                return true;
        }
        return  super.onOptionsItemSelected(item);
    }

    private void saveItem(){
        ContentValues values = new ContentValues();

        String name = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();
        String providerName = mProviderName.getText().toString().trim();
        String providerEmail = mProviderEmail.getText().toString().trim();

        if (mImageBitmap != null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mImageBitmap.compress(Bitmap.CompressFormat.JPEG,0, byteArrayOutputStream);
            byte[] img = byteArrayOutputStream.toByteArray();
            values.put(InventoryEntry.COLUMN_ITEM_PICTURE, img);
        }


        if (currentUri ==null && TextUtils.isEmpty(name) || TextUtils.isEmpty(price)|| TextUtils.isEmpty(quantity) || TextUtils.isEmpty(providerEmail) || TextUtils.isEmpty(providerName)) {
            return;
        }

        values.put(InventoryEntry.COLUMN_ITEM_NAME, name);
        values.put(InventoryEntry.COLUMN_PROVIDER_NAME, providerName);
        values.put(InventoryEntry.COLUMN_PROVIDER_EMAIL, providerEmail);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY,Integer.parseInt(quantity));
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, Double.parseDouble(price));




        if (currentUri == null){
            Uri newRowId = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newRowId == null) Toast.makeText(this, "Error Saving Item", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this,"Item Saved", Toast.LENGTH_SHORT).show();
        } else {
            int itemUpdated = getContentResolver().update(currentUri, values,null,null);

            if (itemUpdated != 0) Toast.makeText(this,"Item Saved", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Error Updating Item"+ String.valueOf(itemUpdated), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (currentUri != null){
            String [] projection = {
                    InventoryEntry._ID,
                    InventoryEntry.COLUMN_ITEM_NAME,
                    InventoryEntry.COLUMN_ITEM_PRICE,
                    InventoryEntry.COLUMN_ITEM_QUANTITY,
                    InventoryEntry.COLUMN_PROVIDER_NAME,
                    InventoryEntry.COLUMN_PROVIDER_EMAIL,
                    InventoryEntry.COLUMN_ITEM_PICTURE
            };
            return  new CursorLoader(this,
                    currentUri,
                    projection,
                    null,
                    null,
                    null);
        }else {
            return null;
        }
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            String name = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME));
            int quantity = data.getInt(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));
            float price = data.getFloat(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE));
            byte[] image = data.getBlob(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_PICTURE));
            String providerName = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_PROVIDER_NAME));
            String providerEmail = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_PROVIDER_EMAIL));

            mImageBitmap = BitmapFactory.decodeByteArray(image,0, image.length);

            DecimalFormat decimalFormat = new DecimalFormat(".00");

            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(decimalFormat.format(price));
            mProviderEmail.setText(providerEmail);
            mProviderName.setText(providerName);
            if (image.length != 2) mImageButton.setImageBitmap(mImageBitmap);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        if (!itemChanged){
            super.onBackPressed();
            return;
        }


        DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangeDialog(discardButtonListener);

    }

    private void  showUnsavedChangeDialog(DialogInterface.OnClickListener discardButtonListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void  showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null) dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem(){
        if (currentUri != null){
            int rowDeleted = getContentResolver().delete(currentUri, null, null);

            if (rowDeleted == 0 ) Toast.makeText(this, "Error deleting item",Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Item deleted",Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_FROM_GALLERY ){
            if (data != null) {
                Uri image = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                    bitmap = getResizedBitmap(bitmap, 500);
                    mImageButton.setImageBitmap(bitmap);
                    mImageBitmap = bitmap;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void orderItem(String body){
        String[]  providerEmail = {mProviderEmail.getText().toString().trim()};
        String itemName = mNameEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/html");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, providerEmail);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Order for "+itemName);
        intent.putExtra(Intent.EXTRA_TEXT,body);
        if(intent.resolveActivity(getPackageManager())!= null){startActivity(Intent.createChooser(intent, "Send Email")); }
    }

    public String orderBody(){
        String providerName = mProviderName.getText().toString().trim();
        String itemName = mNameEditText.getText().toString().trim();
        String body = "Hello "+providerName+",";
        body += "\nI need to reorder the following item: "+itemName;
        body += "\n Thanks. \n";
        return body;
    }
}
