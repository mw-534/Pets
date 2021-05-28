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

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = PetDbHelper(this)

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
        // Create and/or open a database to read from it
        val db = mDbHelper?.readableDatabase

        // This projection could be replaced by null since it includes all columns.
        val projection = arrayOf(
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT
        )

        val cursor = db?.query(
            PetEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        // Always close the cursor when you're done reading from it. This releases all its
        // resources and makes it invalid.
        cursor?.use { cursor ->
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            val displayView = findViewById<TextView>(R.id.text_view_pet)

            // Create a header in the Text View that looks like this:
            //
            // The pets table contains <number of rows in Cursor> pets.
            // _id - name - breed - gender - weight
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.text =
                "The pets table contains " + cursor.count.toString() + " pets.\n\n"
            displayView.append(
                "${PetEntry._ID} - ${PetEntry.COLUMN_PET_NAME} - ${PetEntry.COLUMN_PET_BREED}" +
                        " - ${PetEntry.COLUMN_PET_GENDER} - ${PetEntry.COLUMN_PET_WEIGHT}\n"
            )

            // Figure out the index of each column
            val idColumnIndex = cursor.getColumnIndex(PetEntry._ID)
            val nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)
            val breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)
            val genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)
            val weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                val currentID = cursor.getInt(idColumnIndex)
                val currentName = cursor.getString(nameColumnIndex)
                val currentBreed = cursor.getString(breedColumnIndex)
                val currentGender = cursor.getInt(genderColumnIndex)
                val currentWeight = cursor.getInt(weightColumnIndex)
                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(
                    "\n$currentID - $currentName - $currentBreed - $currentGender - $currentWeight"
                )
            }
        }
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private fun insertPet() {
        // Get the database in write mode.
        val db = mDbHelper?.writableDatabase

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attribute are the values.
        val values = ContentValues().apply {
            put(PetEntry.COLUMN_PET_NAME, "Toto")
            put(PetEntry.COLUMN_PET_BREED, "Terrier")
            put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE)
            put(PetEntry.COLUMN_PET_WEIGHT, 7)
        }

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the pets table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        val newRowId = db?.insert(PetEntry.TABLE_NAME, null, values)

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