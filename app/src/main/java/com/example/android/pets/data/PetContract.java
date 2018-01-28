package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by aaronbrecher on 1/24/18.
 */

public final class PetContract {

    //content authority string - uses the package of our app
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";

    //base Uri built from the CONTENT_AUTHORITY -  basically just preppend the content schema to the content authority
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //specific table paths to append to the base Uri
    public static final String PATH_PETS = "pets";

    public PetContract() {
    }

    public static final class PetEntry implements BaseColumns {

        //the content Uri to access the pets table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        //MIME types for the table and single row
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        //table name
        public static final String TABLE_NAME = "pets";

        //table columns
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        //possible gender values
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
        public static final int GENDER_UNKNOWN = 0;
    }
}
