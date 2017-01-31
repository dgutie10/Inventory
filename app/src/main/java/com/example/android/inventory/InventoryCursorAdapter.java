package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.text.DecimalFormat;

/**
 * Created by diegog on 1/19/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private Context mContext;
    private int mQuantity;
    private String mName;
    private Double mPrice;


    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final TextView name = (TextView) view.findViewById(R.id.name);
        final TextView price = (TextView) view.findViewById(R.id.price);
        final TextView quantity  = (TextView) view.findViewById(R.id.quantity);
        Button sellButton = (Button) view.findViewById(R.id.sell_button);

        int nameColumn = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int priceColumn = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
        int quantityColumn = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        final int item_id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        final Uri currentUri = Uri.withAppendedPath(InventoryEntry.CONTENT_URI, String.valueOf(item_id));

        DecimalFormat decimalFormat = new DecimalFormat(".00");
        final double priceDouble = Double.parseDouble(cursor.getString(priceColumn));

        name.setText(cursor.getString(nameColumn));
        price.setText("$" +decimalFormat.format(priceDouble));
        quantity.setText(cursor.getString(quantityColumn));
        mContext = context;

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrice = priceDouble;
                mQuantity = Integer.parseInt(quantity.getText().toString());
                mName = name.getText().toString();
                cursor.moveToPosition(item_id);
                if (mQuantity > 0){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, mQuantity-1);
                    contentValues.put(InventoryEntry.COLUMN_ITEM_PRICE, mPrice);
                    contentValues.put(InventoryEntry.COLUMN_ITEM_NAME, mName);
                    context.getContentResolver().update(currentUri,contentValues,null,null);
                    quantity.setText(String.valueOf(mQuantity-1));
                }else {
                    Toast.makeText(mContext,"Only 0 of '"+mName+"' in stock.",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}
