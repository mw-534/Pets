package com.example.android.pets

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import androidx.core.database.getStringOrNull
import com.example.android.pets.data.PetContract.PetEntry

/**
 * [PetCursorAdapter] is an adapter for a list or grid view
 * that uses a [Cursor] of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the [Cursor].
 *
 * @constructor Constructs a new [PetCursorAdapter].
 *
 * @param context The context
 * @param c       The cursor from which to get the data.
 */
class PetCursorAdapter(context: Context?, c: Cursor?) :
    CursorAdapter(context, c, 0 /* flags */) {
    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     * moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        // Inflate a list item view using the layout specified in the list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     * correct row.
     */
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        // Find individual views that we want to modify in the list item layout.
        val nameTextView = view.findViewById<TextView>(R.id.name)
        val summaryTextView = view.findViewById<TextView>(R.id.summary)

        // Find the columns of pet attributes we're interested in.
        val nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)
        val breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)

        // Read the pet attributes from the Cursor for the current pet.
        val petName = cursor.getString(nameColumnIndex)
        val petBreed = cursor.getStringOrNull(breedColumnIndex)

        // Update the TextViews with the attributes for the current pet.
        nameTextView.text = petName
        summaryTextView.text = petBreed
    }
}