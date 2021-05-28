package com.example.android.pets.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

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
        return null
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        return null
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