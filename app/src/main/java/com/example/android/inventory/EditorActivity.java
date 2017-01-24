package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by diegog on 1/20/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;

    private Uri currentUri;
    private int ITEM_LOADER = 0;

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


        mNameEditText.setOnTouchListener(onTouchListener);
        mPriceEditText.setOnTouchListener(onTouchListener);
        mQuantityEditText.setOnTouchListener(onTouchListener);

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
            menuItem.setVisible(false);
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
        }
        return  super.onOptionsItemSelected(item);
    }

    private void saveItem(){
        ContentValues values = new ContentValues();

        String name = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();

        if (currentUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(price) && TextUtils.isEmpty(quantity)) return;

        values.put(InventoryEntry.COLUMN_ITEM_NAME, name);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY,Integer.parseInt(quantity));
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, Double.parseDouble(price));


        if (currentUri == null){
            Uri newRowId = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newRowId == null) Toast.makeText(this, "Error Saving Item", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this,"Item Saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (currentUri != null){
            String [] projection = {
                    InventoryEntry._ID,
                    InventoryEntry.COLUMN_ITEM_NAME,
                    InventoryEntry.COLUMN_ITEM_PRICE,
                    InventoryEntry.COLUMN_ITEM_QUANTITY,
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

            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Float.toString(price));

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

        showUnsavedChnageDialog(discardButtonListener);

    }

    private void  showUnsavedChnageDialog(DialogInterface.OnClickListener discardButtonListener){
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
}
