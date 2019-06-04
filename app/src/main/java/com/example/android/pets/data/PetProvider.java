/** MIME TYPE for Content Provider
 * https://stackoverflow.com/questions/7157129/what-is-the-mimetype-attribute-in-data-used-for
 * */
/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /** Database helper object */
    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {//It tells what type of data is present in input URI
        int retId = sUriMatcher.match(uri);
        switch (retId)
        {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;//Multiple Rows are selected by this URI which is the returned one.

            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;//Unique row is selected by this uri which is the returned one.

            default:
                throw new IllegalArgumentException("Unknown Uri "+ uri);

        }

    }
    public Uri insertPet(Uri uri,ContentValues values)
    {
        String petName = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(petName == null)
            throw new IllegalArgumentException("Pet requires a name");
        Integer petGender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        while(petGender==null || !PetEntry.isValidGender(petGender))
        {
            throw new IllegalArgumentException("Pet requires valid Gender");
        }
        Integer petWeight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        while(petWeight<0&&petWeight != null)
        {
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(PetEntry.TABLE_NAME,null,values);
        if(id==-1)
            Toast.makeText(getContext(),"Failed",Toast.LENGTH_SHORT);
        else
            Toast.makeText(getContext(),"success",Toast.LENGTH_SHORT);
        return (ContentUris.withAppendedId(uri,id));
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int code = sUriMatcher.match(uri);
        Uri newUri;
        switch(code)
        {
            case PETS:
                newUri = insertPet(uri,contentValues);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: "+ uri);

        }

        return newUri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {

            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            final int match = sUriMatcher.match(uri);
            switch (match) {
                case PETS:
                    // Delete all rows that match the selection and selection args
                    return database.delete(PetEntry.TABLE_NAME, s, strings);
                case PET_ID:
                    // Delete a single row given by the ID in the URI
                    s = PetEntry._ID + "=?";
                    strings = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                    return database.delete(PetEntry.TABLE_NAME, s, strings);
                default:
                    throw new IllegalArgumentException("Deletion is not supported for " + uri);
            }


    }
    public  int updatePet(Uri uri, ContentValues values, String s, String[] strings)
    {
        String petName = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(petName == null)
            throw new IllegalArgumentException("Pet requires a name");
        Integer petGender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        while(petGender==null || !PetEntry.isValidGender(petGender))
        {
            throw new IllegalArgumentException("Pet requires valid Gender");
        }
        Integer petWeight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        while(petWeight<0&&petWeight != null)
        {
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int id = db.update(PetEntry.TABLE_NAME,values,s,strings);
        if(id==-1)
            Toast.makeText(getContext(),"Failed",Toast.LENGTH_SHORT);
        else
            Toast.makeText(getContext(),"success",Toast.LENGTH_SHORT);
        return (id);
    }
    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        final int retId = sUriMatcher.match(uri);
        switch(retId)
        {
            case PETS:
                //type 1 update
                return updatePet(uri,contentValues,s,strings);

            case PET_ID:
                s = PetEntry._ID+"=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,contentValues,s,strings);
                //type 2 update

            default:
                throw new IllegalArgumentException("Invalid Update Arguments");
        }

    }
}