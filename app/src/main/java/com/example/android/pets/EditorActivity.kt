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
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.database.getStringOrNull
import com.example.android.pets.data.PetContract.PetEntry
import com.example.android.pets.data.PetDbHelper
import com.example.android.pets.data.PetProvider

/**
 * Allows user to create a new pet or edit an existing one.
 */
class EditorActivity : AppCompatActivity() {
    /** EditText field to enter the pet's name  */
    private var mNameEditText: EditText? = null

    /** EditText field to enter the pet's breed  */
    private var mBreedEditText: EditText? = null

    /** EditText field to enter the pet's weight  */
    private var mWeightEditText: EditText? = null

    /** EditText field to enter the pet's gender  */
    private var mGenderSpinner: Spinner? = null

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private var mGender = PetEntry.GENDER_UNKNOWN
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an exsiting one.
        val currentPetUri = intent.data

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet.
        title = if (currentPetUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet".
            getString(R.string.editor_activity_title_new_pet)
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet".
            getString(R.string.editor_activity_title_edit_pet)
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById<View>(R.id.edit_pet_name) as EditText
        mBreedEditText = findViewById<View>(R.id.edit_pet_breed) as EditText
        mWeightEditText = findViewById<View>(R.id.edit_pet_weight) as EditText
        mGenderSpinner = findViewById<View>(R.id.spinner_gender) as Spinner
        setupSpinner()

        // Load pet from content URI if in edit mode.
        if (currentPetUri != null) {
            loadPet(currentPetUri)
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private fun setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        val genderSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this, R.array.array_gender_options, android.R.layout.simple_spinner_item
        )

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        // Apply the adapter to the spinner
        mGenderSpinner?.adapter = genderSpinnerAdapter

        // Set the integer mSelected to the constant values
        mGenderSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selection = parent.getItemAtPosition(position) as String
                if (selection.isNotEmpty()) {
                    mGender = when (selection) {
                        getString(R.string.gender_male) -> PetEntry.GENDER_MALE
                        getString(R.string.gender_female) -> PetEntry.GENDER_FEMALE
                        else -> PetEntry.GENDER_UNKNOWN
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            override fun onNothingSelected(parent: AdapterView<*>?) {
                mGender = PetEntry.GENDER_UNKNOWN
            }
        }
    }

    /**
     * Get user input from editor and save new pet into database.
     */
    private fun insertPet() {
        // Read input fields.
        // Use trim to remove leading or trailing white space.
        val nameString = mNameEditText?.text.toString().trim()
        val breedString = mBreedEditText?.text.toString().trim()
        val weightString = mWeightEditText?.text.toString().trim()
        val weight = Integer.parseInt(weightString)

        // Create a ContentValues object where column names are the keys
        // and pet attributes from the editor are the values.
        val values = ContentValues().apply {
            put(PetEntry.COLUMN_PET_NAME, nameString)
            put(PetEntry.COLUMN_PET_BREED, breedString)
            put(PetEntry.COLUMN_PET_WEIGHT, weight)
            put(PetEntry.COLUMN_PET_GENDER, mGender)
        }

        // Insert a new row for pet in the database, returning the ID of that new row.
        val newUri = contentResolver.insert(PetEntry.CONTENT_URI, values)

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {
            // If the row ID is null,then there was an error with insertion.
            Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show()
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            R.id.action_save -> {
                // Save pet to database.
                insertPet()
                // Exit activity.
                finish()
                return true
            }
            R.id.action_delete ->                 // Do nothing for now
                return true
            android.R.id.home -> {
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Load pet from the given content URI and populates the
     * fields of the [EditorActivity] with the pet attributes.
     */
    fun loadPet(currentPetUri: Uri) {
        // Get cursor from content resolver.
        // Since the editor shows all attributes no projection is needed.
        val cursor = contentResolver.query(
            currentPetUri, null, null, null, null
        )
        cursor?.use {
            // Move to the first row of the cursor and read data from it
            // (This should be the only row in the cursor).
            if (it.moveToFirst()) {
                // Fill name view from cursor.
                val nameColumnIndex = it.getColumnIndex(PetEntry.COLUMN_PET_NAME)
                val name = it.getString(nameColumnIndex)
                mNameEditText?.setText(name)

                // Fill breed view from cursor.
                val breedColumnIndex = it.getColumnIndex(PetEntry.COLUMN_PET_BREED)
                val breed = it.getStringOrNull(breedColumnIndex)
                mBreedEditText?.setText(breed)

                // Fill gender spinner from cursor.
                val genderColumnIndex = it.getColumnIndex(PetEntry.COLUMN_PET_GENDER)
                mGender = it.getInt(genderColumnIndex)
                // Gender is a dropdown spinner, so map the constant value from the database
                // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
                // Then call setSelection() so that option is displayed on screen as the current selection.
                val selection = when (mGender) {
                    PetEntry.GENDER_MALE -> 1
                    PetEntry.GENDER_FEMALE -> 2
                    else -> 0
                }
                mGenderSpinner?.setSelection(selection)

                // Fill weight view from cursor.
                val weightColumnIndex = it.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)
                val weight = it.getInt(weightColumnIndex)
                mWeightEditText?.setText(weight.toString())
            }
        }

    }
}