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
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.database.getStringOrNull
import com.example.android.pets.data.PetContract.PetEntry

/**
 * Allows user to create a new pet or edit an existing one.
 */
class EditorActivity : AppCompatActivity() {

    /** Content URI for the existing pet (null if it's a new pet) */
    private var mCurrentPetUri: Uri? = null

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

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    var mPetHasChanged: Boolean = false

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    val mTouchListener = View.OnTouchListener { v, event ->
        mPetHasChanged = true
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        mCurrentPetUri = intent.data

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet.
        title = if (mCurrentPetUri == null) {
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

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText?.setOnTouchListener(mTouchListener)
        mBreedEditText?.setOnTouchListener(mTouchListener)
        mWeightEditText?.setOnTouchListener(mTouchListener)
        mGenderSpinner?.setOnTouchListener(mTouchListener)

        // Load pet from content URI if in edit mode.
        if (mCurrentPetUri != null) {
            loadPet(mCurrentPetUri!!)
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
     * Get user input from editor and save pet into database.
     */
    private fun savePet() {
        // Read input fields.
        // Use trim to remove leading or trailing white space.
        val nameString = mNameEditText?.text.toString().trim()
        val breedString = mBreedEditText?.text.toString().trim()
        val weightString = mWeightEditText?.text.toString().trim()
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        var weight = 0
        if (!weightString.isNullOrEmpty()) {
            weight = Integer.parseInt(weightString)
        }

        // Check that user didn't accidentally saved the pet with empty fields.
        if (mCurrentPetUri == null
            && nameString.isNullOrEmpty() && mGender == PetEntry.GENDER_UNKNOWN
            && breedString.isNullOrEmpty() && weightString.isNullOrEmpty()) {
                // user accidentally pressed save with empty dataset, hence exit activity.
                return
        }

        // Create a ContentValues object where column names are the keys
        // and pet attributes from the editor are the values.
        val values = ContentValues().apply {
            put(PetEntry.COLUMN_PET_NAME, nameString)
            put(PetEntry.COLUMN_PET_BREED, breedString)
            put(PetEntry.COLUMN_PET_WEIGHT, weight)
            put(PetEntry.COLUMN_PET_GENDER, mGender)
        }


        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentPetUri == null) {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            val newUri = contentResolver.insert(PetEntry.CONTENT_URI, values)

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the row ID is null,then there was an error with insertion.
                Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show()
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            val rowsAffected = contentResolver.update(mCurrentPetUri!!, values, null, null)

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                    Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                    Toast.LENGTH_SHORT).show()
            }
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
                savePet()
                // Exit activity.
                finish()
                return true
            }
            R.id.action_delete ->                 // Do nothing for now
                return true
            android.R.id.home -> {
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the CatalogActivity.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                    return true
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                val discardButtonClickListener = DialogInterface.OnClickListener { dialog, which ->
                    // User clicked "Discard" button, navigate up to parent activity.
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                }

                // Show a dialog that notifies the user they have unsaved changes.
                showUnsavedChangesDialog(discardButtonClickListener)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The [OnBackPressedDispatcher][.getOnBackPressedDispatcher] will be given a
     * chance to handle the back button before the default behavior of
     * [android.app.Activity.onBackPressed] is invoked.
     *
     * @see .getOnBackPressedDispatcher
     */
    override fun onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press.
        if (!mPetHasChanged) {
            super.onBackPressed()
            return
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        val discardButtonClickListener = DialogInterface.OnClickListener { dialog, which ->
            // User clicked the "Discard" button, close the current activity.
            finish()
        }

        // Show dialog that there are unsaved changes.
        showUnsavedChangesDialog(discardButtonClickListener)
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

    /**
     *  Show a dialog that warns the user there are unsaved changes that will be lost
     *  if they continue leaving the editor.
     *
     *  @param discardButtonClickListener   is the click listener for what to do when
     *                                      the user confirms they want to discard their changes.
     */
    private fun showUnsavedChangesDialog(
        discardButtonClickListener: DialogInterface.OnClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.unsaved_changes_dialog_msg))
            setPositiveButton(getString(R.string.discard), discardButtonClickListener)
            setNegativeButton(getString(R.string.keep_editing), DialogInterface.OnClickListener {
                    dialog, which ->
                // User clicked the "keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                dialog?.dismiss()
            })
        }

        // Create and show the AlertDialog.
        val alertDialog = builder.create()
        alertDialog.show()
    }
}