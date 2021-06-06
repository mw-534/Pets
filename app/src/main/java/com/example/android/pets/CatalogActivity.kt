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
package com.example.android.pets

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.android.pets.data.PetContract.PetEntry
import com.example.android.pets.data.PetDbHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * Displays list of pets that were entered and stored in the app.
 */
class CatalogActivity : AppCompatActivity() {

    /** Database helper that will provide us access to the database. */
    private var mDbHelper: PetDbHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Setup FAB to open EditorActivity
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
            startActivity(intent)
        }
        // Find the ListView which will be populated with the pet data.
        val petListView = findViewById<ListView>(R.id.list)

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        val emptyView = findViewById<RelativeLayout>(R.id.empty_view)
        petListView.emptyView = emptyView

        displayDatabaseInfo()
    }

    override fun onStart() {
        super.onStart()
        displayDatabaseInfo()
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private fun displayDatabaseInfo() {
        // This projection could be replaced by null since it includes all columns.
        val projection = arrayOf(
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT
        )

        val cursor = contentResolver.query(PetEntry.CONTENT_URI,
            projection, null, null, null)

        // Find the ListView which will be populated with the pet data.
        val petListView = findViewById<ListView>(R.id.list)

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        val adapter = PetCursorAdapter(this, cursor)

        // Attach the adapter to the ListView.
        petListView.adapter = adapter

        // Setup the item click listener.
        petListView.setOnItemClickListener { parent, view, position, id ->
            // Create new intent to go to EditorActivity
            val i = Intent(this@CatalogActivity, EditorActivity::class.java)

            // Form the content URI that represents the specific pet that was clicked on,
            // by appending the "id" (passed as input to this method) onto the
            // PetEntry.CONTENT_URI.
            // For example the URI would be "content://com.example.android.pets/pets/2"
            // if the pet with ID 2 was clicked on.
            val currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id)

            // Set the URI on the data field of the intent.
            i.data = currentPetUri

            // Launch the EditorActivity to display the data for the current pet.
            startActivity(i)
        }
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private fun insertPet() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attribute are the values.
        val values = ContentValues().apply {
            put(PetEntry.COLUMN_PET_NAME, "Toto")
            put(PetEntry.COLUMN_PET_BREED, "Terrier")
            put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE)
            put(PetEntry.COLUMN_PET_WEIGHT, 7)
        }

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the PetEntry.CONTENT_URI to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        val newRowId = contentResolver.insert(PetEntry.CONTENT_URI, values)

        Log.d(this::class.java.simpleName, "New Row ID: $newRowId")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            R.id.action_insert_dummy_data -> {
                insertPet()
                displayDatabaseInfo()
                return true
            }
            R.id.action_delete_all_entries ->                 // Do nothing for now
                return true
        }
        return super.onOptionsItemSelected(item)
    }
}