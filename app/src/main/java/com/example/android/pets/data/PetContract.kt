package com.example.android.pets.data

import android.content.UriMatcher
import android.net.Uri
import android.provider.BaseColumns

/** URI matcher code for the content URI for the pets table. */
const val PETS = 100

/** URI matcher code for the content URI for a single pet in the pets table. */
const val PET_ID = 101

val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
    addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS)
    addURI(PetContract.CONTENT_AUTHORITY, "${PetContract.PATH_PETS}/#", PET_ID)
}


/**
 * API contract for the pets app.
 */
object PetContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    const val CONTENT_AUTHORITY = "com.example.android.pets"

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    const val PATH_PETS = "pets"

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    object PetEntry : BaseColumns {
        /** The content URI to access the pet data in the provider. */
        val CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS)

        /** Name of database table for pets. */
        const val TABLE_NAME = "pets"

        /**
         * Unique ID number for the pet (only for use in the database table).
         *
         * Type: INTEGER
         */
        const val _ID = BaseColumns._ID

        /**
         * Name of the pet.
         *
         * TYPE: TEXT
         */
        const val COLUMN_PET_NAME = "name"

        /**
         * Breed of the pet.
         *
         * TYPE: TEXT
         */
        const val COLUMN_PET_BREED = "breed"

        /**
         * Gender of the pet.
         *
         * The only possible values are [GENDER_MALE], [GENDER_FEMALE]
         * or [GENDER_UNKNOWN].
         *
         * TYPE: INTEGER
         */
        const val COLUMN_PET_GENDER = "gender"

        /**
         * Weight of the pet.
         *
         * TYPE: INTEGER
         */
        const val COLUMN_PET_WEIGHT = "weight"

        // Possible values for the gender of the pet.
        const val GENDER_UNKNOWN = 0
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 2

        /**
         * Returns whether or not the given gender is [GENDER_UNKNOWN], [GENDER_MALE]
         * or [GENDER_FEMALE].
         */
        fun isValidGender(gender: Int): Boolean {
            return gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE
        }
    }
}