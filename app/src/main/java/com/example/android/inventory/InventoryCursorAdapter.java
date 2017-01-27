package com.example.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.text.DecimalFormat;

/**
 * Created by diegog on 1/19/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView quantity  = (TextView) view.findViewById(R.id.quantity);

        int nameColumn = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int priceColumn = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
        int quantityColumn = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

        DecimalFormat decimalFormat = new DecimalFormat(".00");
        double priceDouble  = Double.parseDouble(cursor.getString(priceColumn));
        name.setText(cursor.getString(nameColumn));
        price.setText("$" +decimalFormat.format(priceDouble));
        quantity.setText(cursor.getString(quantityColumn));

    }
}
