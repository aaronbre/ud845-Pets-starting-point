package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    ListView mListView;
    PetCursorAdapter cursorAdapter;
    Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mListView = (ListView)findViewById(R.id.pets_list_view);
        View emptyView = findViewById(R.id.empty_view);
        mListView.setEmptyView(emptyView);
    }

    @Override
    protected void onStart(){
        super.onStart();
        displayDatabaseInfo();
    }


    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        String[] projection = getProjection();
        //String[] selection = getSelection();
        //String[] SelectionArgs = getSelectionArgs();
        String sortOrder = PetEntry._ID + " ASC";

        // get the data from the content provider and store it in the global cursor object (probably does not need to be global...)
        mCursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, sortOrder);
        // create a new cursor adapter using the cursor data returned by the content provider
        cursorAdapter = new PetCursorAdapter(CatalogActivity.this, mCursor);
        // set the cursor adaptor to be the adaptor of the list view
        mListView.setAdapter(cursorAdapter);
    }

    /**
     * function to get the projection array - in this case it is simple...
     * @return
     */
    private String[] getProjection(){
        return new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_WEIGHT};
    }

    /**
     * Function to get the selection - simple for now
     * @return
     */
    private String getSelection(){
        return "";
    }

    /**
     * Function to get the selectionArgs - simple for now
     * @return
     */
//    private String[] getSelectionArgs(){
//        return "";
//    }

    private String genderToString(int genderNum){
        String gender;
        switch (genderNum){
            case PetEntry.GENDER_FEMALE:
                gender = "Female";
                break;
            case PetEntry.GENDER_MALE:
                gender = "Male";
                break;
            default:
                gender = "Unknown";
        }
        return gender;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // insert a dummy pet into the database
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData(){
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        getContentResolver().insert(PetEntry.CONTENT_URI, values);
        displayDatabaseInfo();
    }
}