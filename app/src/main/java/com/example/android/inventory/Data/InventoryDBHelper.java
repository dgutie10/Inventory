package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by diegog on 1/13/2017.
 */

public class InventoryDBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "inventory.db";

    public static final int DATABASE_VERSION = 1;

    public InventoryDBHelper(Context context) {super (context,DATABASE_NAME,null,DATABASE_VERSION);}


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE "+ InventoryEntry.TABLE_NAME + "("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PROVIDER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PROVIDER_EMAIL + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_PICTURE + " TEXT DEFAULT 0);";
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
