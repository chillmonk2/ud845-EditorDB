package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

import org.w3c.dom.Text;

public class PetCursorAdapter extends CursorAdapter {
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_list,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.pet_name);
        TextView breedTextView = (TextView) view.findViewById(R.id.pet_breed);
        String mNameString = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME)).trim();
        String mBreedString = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED)).trim();
        nameTextView.setText(mNameString);
        breedTextView.setText(mBreedString);
    }
}
