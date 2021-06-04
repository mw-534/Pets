package com.example.android.pets.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.android.pets.data.PetContract.PetEntry


/**
 * [ContentProvider] for Pets app.
 */
class PetProvider : ContentProvider() {
    /** Database helper object to access the pets database */
    private var mDbHelper: PetDbHelper? = null

    /**
     * Initialize the provider and the database helper object.
     */
    override fun onCreate(): Boolean {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        // For 'let' the 'it' is the object it is called upon (here the context).
        // 'let' returns the result of the block (here the result of PetDbHelper).
        mDbHelper = context?.let { PetDbHelper(it) }

        return true
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        // Get readable database
        val database = mDbHelper!!.readableDatabase

        // This cursor will hold the result of the query
        var cursor: Cursor? = null

        // Figure out if the URI matcher can match the URI to a specific code
        val match = sUriMatcher.match(uri)
        when (match) {
            PETS -> {
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                null, null, sortOrder)
            }
            PET_ID -> {
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                val selectionId = PetEntry._ID + "=?"
                val selectionArgsId = arrayOf(ContentUris.parseId(uri).toString())

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(
                    PetEntry.TABLE_NAME, projection, selectionId, selectionArgsId,
                    null, null, sortOrder
                )
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }
        return cursor
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val match = sUriMatcher.match(uri)
        return when (match) {
            PETS -> insertPet(uri, contentValues!!)
            else -> throw java.lang.IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private fun insertPet(uri: Uri, values: ContentValues): Uri? {
        // Check that the name is not null.
        val name = values.getAsString(PetEntry.COLUMN_PET_NAME)
        if (name == null) {
            throw java.lang.IllegalArgumentException("Pet requires a name")
        }

        // Check that gender is valid.
        val gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER)
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw java.lang.IllegalArgumentException("Pet requires valid gender")
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg.
        val weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT)
        if (weight != null && weight < 0) {
            throw java.lang.IllegalArgumentException("Pet requires valid weight")
        }

        // No need to check the breed, any value is valid (including null).

        // Get writeable database.
        val database = mDbHelper?.writableDatabase

        // Insert the new pet with the given values.
        val id = database?.insert(PetEntry.TABLE_NAME, null, values) ?: -1
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1L) {
            Log.e(LOG_TAG, "Failed to insert row for ${uri.toString()}")
            return null
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it.
        return ContentUris.withAppendedId(uri, id)
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    override fun update(
        uri: Uri,
        contentValues: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val match = sUriMatcher.match(uri)
        when (match) {
            PETS -> return updatePet(uri, contentValues, selection, selectionArgs)
            PET_ID -> {
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and
                // selection arguments will be a String array containing the actual ID.
                val selection = "${PetEntry._ID}=?"
                val selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                return updatePet(uri, contentValues, selection, selectionArgs)
            }
            else -> throw java.lang.IllegalArgumentException("Update is not supported for $uri.")
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number or rows that were successfully updated.
     */
    private fun updatePet(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        // If the PetEntry.COLUMN_PET_NAME key is present,
        // check that the name value is not null.
        if (values?.containsKey(PetEntry.COLUMN_PET_NAME) == true) {
            val name = values.getAsShort(PetEntry.COLUMN_PET_NAME)
            if (name == null) {
                throw java.lang.IllegalArgumentException("Pet requires a name")
            }
        }

        // If the PetEntry.COLUMN_PET_GENDER key is present,
        // check that the gender value is valid.
        if (values?.containsKey(PetEntry.COLUMN_PET_GENDER) == true) {
            val gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER)
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw java.lang.IllegalArgumentException("Pet requires valid gender")
            }
        }

        // If the PetEntry.COLUMN_PET_WEIGHT key is present,
        // check that the weight value is valid.
        if (values?.containsKey(PetEntry.COLUMN_PET_WEIGHT) == true) {
            // Check that the weight is greater than or equal to 0 kg
            val weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT)
            if (weight != null && weight < 0) {
                throw java.lang.IllegalArgumentException("Pet requires valid weight")
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database.
        if (values?.size() == 0) {
            return 0
        }

        // Otherwise, get writeable database to update the data.
        val database = mDbHelper?.writableDatabase

        // Update the selected pets in the pets database table with the given ContentValues and
        // return the number of rows that were affected.
        return database?.update(PetEntry.TABLE_NAME, values, selection, selectionArgs) ?: 0
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // Get writeable database
        val database = mDbHelper!!.writableDatabase

        val match = sUriMatcher.match(uri)
        return when (match) {
            PETS ->
                // Delete all rows that match the selection and selection args
                database.delete(PetEntry.TABLE_NAME, selection, selectionArgs)
            PET_ID -> {
                // Delete a single row given by the ID in the URI
                val selection = PetEntry._ID + "=?"
                val selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                database.delete(PetEntry.TABLE_NAME, selection, selectionArgs)
            }
            else -> throw java.lang.IllegalArgumentException("Deletion is not supported for $uri")
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    override fun getType(uri: Uri): String {
        val match = sUriMatcher.match(uri)
        return when (match) {
            PETS -> PetEntry.MIME_LIST_TYPE
            PET_ID -> PetEntry.MIME_ITEM_TYPE
            else -> throw java.lang.IllegalStateException("Unknown URI $uri with match $match")
        }
    }

    companion object {
        /** Tag for the log messages  */
        val LOG_TAG = PetProvider::class.java.simpleName
    }
}