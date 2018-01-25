package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by aaronbrecher on 1/24/18.
 */

public final class PetContract {

    public PetContract() {
    }

    public final class PetEntry implements BaseColumns{
        //table name
        public static final String TABLE_NAME = "pets";

        //table columns
        public static final String COLUMN_NAME_ID = BaseColumns._ID;
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_BREED = "breed";
        public static final String COLUMN_NAME_GENDER = "gender";
        public static final String COLUMN_NAME_WEIGHT = "weight";

        //possible gender values
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
        public static final int GENDER_UNKNOWN = 0;
    }
}