package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int PET_LOADER = 1;

    ListView mListView;
    PetCursorAdapter mCursorAdapter;
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

        // set up the adaptor to a new petCursorAdapter, set it to null for now will be occupied by the cursorLoader
        mCursorAdapter = new PetCursorAdapter(this, null);

        //set the listView's adapter to the new petCursorAdapter
        mListView.setAdapter(mCursorAdapter);

        //initialize the cursor loader
        getLoaderManager().initLoader(PET_LOADER, null, this);

    }

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
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId){
            case PET_LOADER:
                return new CursorLoader(
                        CatalogActivity.this,
                        PetEntry.CONTENT_URI,
                        getProjection(),
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    /**
     * function to get the projection array - in this case it is simple...
     * @return
     */
    private String[] getProjection(){
        return new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_NAME,};
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}