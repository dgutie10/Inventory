package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventory.data.InventoryContract.IventoryEntry;

/**
 * Created by diegog on 1/13/2017.
 */

public class InventoryDBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "inventory.db";

    public static final int DATABASE_VERSION = 1;

    public InventoryDBHelper(Context context) {super (context,DATABASE_NAME,null,DATABASE_VERSION);}


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE "+ IventoryEntry.TABLE_NAME + "("
                + IventoryEntry._ID + " INTERGER PRIMARY KEY AUTOINCREMENT, "
                + IventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + IventoryEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL "
                + IventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL "
                + IventoryEntry.COLUMN_ITEM_PICTURE + " BLOB DEFAULT 0);";
        Log.e("DBHelper", SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
