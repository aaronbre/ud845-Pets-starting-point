package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;


/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    /** Member variable for the helper for the sqlite database */
    PetDbHelper mDbHelper;

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /** Static for the Pets table to be used by UriMatcher */
    private static final int PETS = 100;

    /** Static for the Pets table 1 row to be used by UriMatcher */
    private static final int PET_ID = 101;

    /** Member variable for the UriMatcher */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /** add Uri for the whole Pets database */
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        /** add Uri for a specific row in the pets table */
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
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
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

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

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues contentValues){
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        if (checkValidValues(contentValues)) {
            long id = database.insert(PetEntry.TABLE_NAME, null, contentValues);

            if (id == -1) {
                Log.e(LOG_TAG, "insertPet: failed to insert row for" + uri);
                return null;
            }
            // Once we know the ID of the new row in the table,
            // return the new URI with the ID appended to the end of it
            return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }

    private boolean checkValidValues(ContentValues values){
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(name == null || name.isEmpty()) throw new IllegalArgumentException("Pet requires a name");

        int gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender != PetEntry.GENDER_FEMALE
                && gender != PetEntry.GENDER_MALE
                && gender != PetEntry.GENDER_UNKNOWN) throw new IllegalArgumentException("Not a valid gender") ;

        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight == null || weight < 0) throw new IllegalArgumentException("Pet weight cannot be negative");

        return true;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (checkValidValues(contentValues)) {
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case PETS:
                    return updatePet(uri, contentValues, selection, selectionArgs);
                case PET_ID:
                    // we are only updating one row so set selection to the id field
                    selection = PetEntry._ID + "=?";
                    // get the id from the uri
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //update the pet using the updated selection and selectionArgs
                    return updatePet(uri, contentValues, selection, selectionArgs);
                default:
                    throw new IllegalArgumentException("update not supported for " + uri);
            }
        }
        return 0;
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete not allowed for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unkown Uri " + uri + "with match" + match);
        }
    }
}