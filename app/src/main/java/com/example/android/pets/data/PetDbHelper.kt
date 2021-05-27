package com.example.android.pets.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.android.pets.data.PetContract.PetEntry

/** Name of the database file. */
private const val DATABASE_NAME = "shelter.db"

/** Databse version. If you change the database schema, you must increment the database version. */
private const val DATABASE_VERSION = 1

/** SQL statement to create the pets table. */
private const val SQL_CREATE_PETS_TABLE = "CREATE TABLE ${PetEntry.TABLE_NAME} (" +
        "${PetEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "${PetEntry.COLUMN_PET_NAME} TEXT NOT NULL, " +
        "${PetEntry.COLUMN_PET_BREED} TEXT, " +
        "${PetEntry.COLUMN_PET_GENDER} INTEGER NOT NULL, " +
        "${PetEntry.COLUMN_PET_WEIGHT} INTEGER NOT NULL DEFAULT 0);"

/**
 * Database helper for Pets app. Manages database creation and version management.
 *
 * @constructor Constructs a new instance of [PetDbHelper].
 *
 * @param context is the Context of the app.
 */
class PetDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        // Execute the SQL statement to create the pets table.
        db?.execSQL(SQL_CREATE_PETS_TABLE)
        Log.d(PetDbHelper::class.java.simpleName, "SQL-CREATE statement\n: $SQL_CREATE_PETS_TABLE")
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     *
     *
     * The SQLite ALTER TABLE documentation can be found
     * [here](http://sqlite.org/lang_altertable.html). If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     *
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // The database is still at version 1, so there's nothing to be done here.
    }
}
