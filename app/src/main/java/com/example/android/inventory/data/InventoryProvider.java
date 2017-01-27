package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by diegog on 1/13/2017.
 */

public class InventoryProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int ITEMS = 100;
    private static final int ITEM_ID = 101;
    private InventoryDBHelper mDBHelper;

    static {
        URI_MATCHER.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEMS);
        URI_MATCHER.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY+"/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new InventoryDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDBHelper.getReadableDatabase();

        Cursor cursor;

        int match = URI_MATCHER.match(uri);

        switch (match){
            case ITEMS:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, null, null, null, null, null);
                return  cursor;
            case ITEM_ID:
                selection = InventoryEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection,selection,selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);

        switch (match){
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI "+ uri + " with match "+ match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final int match = URI_MATCHER.match(uri);

        switch (match){
            case ITEMS:
                return insertItem(uri,values);
            default:
                throw new IllegalArgumentException("Insertion is nor supported for "+uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values){
        if (values.getAsString(InventoryEntry.COLUMN_ITEM_NAME) == null) throw new IllegalArgumentException("Item name is required.");
        if (values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE) == null) throw new IllegalArgumentException("Item price is required.");
        if (values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE) == null) throw new IllegalArgumentException("Item quantity is required.");

        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert new row for " + uri);
            return null;
        }

        Log.e("InventoryProvider","Sending notification");

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int rowsDeleted;

        switch (match){
            case ITEMS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME,selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete unknown URI "+ uri);
        }
        Log.e("InventoryProvider","Sending notification");
        if (rowsDeleted != 0 ) getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = URI_MATCHER.match(uri);

        switch (match){
            case ITEMS:
                return updateItem(uri, values,selection, selectionArgs);
            case ITEM_ID:

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is nor supported in "+ uri);
        }
    }

    private int updateItem (Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.getAsString(InventoryEntry.COLUMN_ITEM_NAME) == null) throw new IllegalArgumentException("Item name is required.");
        if (values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE) == null) throw new IllegalArgumentException("Item price is required.");
        if (values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE) == null) throw new IllegalArgumentException("Item quantity is required.");


        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        int rowsUpdated =  database.update(InventoryEntry.TABLE_NAME,values,selection,selectionArgs);

        Log.e("InventoryProvider","Sending notification "+String.valueOf(rowsUpdated));

        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri,null);
            Log.e("InventoryProvider","Sending notification Sent: "+uri);
        }

        return rowsUpdated;
    }
}
