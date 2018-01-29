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
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Final number for the pet loader is the same as catalog activity */
    private final int PET_EDITOR_LOADER = 2;

    /** Member variable for the uri will be provided by intent */
    private Uri mCurrentPetUri;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /** boolean to check if any data has been entered - to be used for whether a dialog should be shown */
    private boolean mPetHasChanged = false;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        /** get the uri passed in through the intent will be
         * null if the data was not set
         */
        mCurrentPetUri = getIntent().getData();

        //set the title of the page if there is a uri - set to edit otherwise set to add
        if (mCurrentPetUri == null){
            setTitle(R.string.editor_label_add);
        } else {
            setTitle(R.string.editor_label_edit);
            invalidateOptionsMenu();
            //initialize the cursor loader - only call if in edit mode or else null pointer issue!!
            getLoaderManager().initLoader(PET_EDITOR_LOADER, null, this);
        }


        setupSpinner();
    }

    /**
     * On touch listener to be used to check if user has begun editing
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    /**
     * function to add the new pet to the db
     */
    private void insertPet(){
        //get values to add to database
        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();

        // if breed is empty add default text Breed Unkown
        if (petBreed.isEmpty()) petBreed = getString(R.string.breed_unknown);

        //get the gender was already set in the spinner
        int petGender = mGender;
        Integer petWeight = getWeight();

        if(petName.isEmpty() || petWeight == null){
            Toast.makeText(this, getString(R.string.invalid_entries_database_add_toast), Toast.LENGTH_SHORT).show();
            return;
        }
        //create a ContentValues object occupied by those contents
        ContentValues values = setUpValues(petName, petBreed, petGender, petWeight);
        // Uri object to determine if insert/update was succesfull
        Uri uri = null;
        int row = -1;

        //if this is an insert will insert a new pet using the contentResolver
        if (mCurrentPetUri == null){
            uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            //display the id to the user
            if (uri != null) Toast.makeText(this, getString(R.string.successfull_database_add_toast) , Toast.LENGTH_SHORT).show();
        }
        // If in edit moder will update the pet using the petUri, where and selectionArgs are null as
        // using a uri with an id will set it up for us
        else {
            row = getContentResolver().update(mCurrentPetUri, values, null, null);
            if (row != -1) Toast.makeText(this, "Pet updated", Toast.LENGTH_SHORT);
        }
        if (uri == null && row == -1) Toast.makeText(this, getString(R.string.unsuccessfull_database_add_toast), Toast.LENGTH_SHORT).show();

    }

    /** simple function to set up a contentValues for the query */
    private ContentValues setUpValues(String name, String breed, int gender, int weight){
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, gender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        return values;
    }

    /** function to get the weight of the pet, will return null if it is empty or not an integer
     *@return
     */
    private Integer getWeight(){
        Integer weight;
        try{
            weight = Integer.parseInt(mWeightEditText.getText().toString().trim());
            return weight;
        }catch (NumberFormatException e){
            return null;
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        //do the delete method through using the content provider returns the row number if successfull otherwise -1
        int rowNum = getContentResolver().delete(mCurrentPetUri,null,null);
        //check that the delete was a success and show a toast otherwise show not success toast
        if (rowNum != -1){
            Toast.makeText(this,R.string.editor_delete_pet_successful, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, R.string.editor_delete_pet_failed, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // add to the database and send back to previous screen
                insertPet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // for the PetEditorLoader set up a new loader using the uri - containing the id of the pet to edit
        switch (i){
            case PET_EDITOR_LOADER:
                return new CursorLoader(
                        EditorActivity.this,
                        mCurrentPetUri,
                        getProjection(),
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    // create a simple projection recieving all the data to update the fields
    private String[] getProjection(){
        return new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //check that there is an entry to the cursor
        if (cursor.moveToFirst()){
            //set the name field to be equal to the name column in the cursor - uses the getColumnIndex to find the column to searc
            int nameIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            mNameEditText.setText(cursor.getString(nameIndex));

            //set the breed field - same as above
            int breedIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            mBreedEditText.setText(cursor.getString(breedIndex));

            //set the weight field - same as above but need to convert to string using string.value of
            int weightIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            mWeightEditText.setText(String.valueOf(cursor.getInt(weightIndex)));

            //set the gender spinner
            int gender = cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER));
            mGenderSpinner.setSelection(gender);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}