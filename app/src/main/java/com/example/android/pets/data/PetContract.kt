package com.example.android.pets.data

import android.provider.BaseColumns

/**
 * API contract for the pets app.
 */
object PetContract {

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    object PetEntry : BaseColumns {
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
    }
}