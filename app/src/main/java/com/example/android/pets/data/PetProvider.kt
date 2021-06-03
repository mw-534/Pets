package com.example.android.pets.data

import android.R.id
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
        return 0
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    override fun getType(uri: Uri): String? {
        return null
    }

    companion object {
        /** Tag for the log messages  */
        val LOG_TAG = PetProvider::class.java.simpleName
    }
}