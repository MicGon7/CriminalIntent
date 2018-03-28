package com.bignerdranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.bignerdranch.android.criminalintent.database.CrimeBaseHelper;
import com.bignerdranch.android.criminalintent.database.CrimeCursorWrapper;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

/**
 * Created by michaelgonzalez on 2/27/18.
 */

public class CrimeLab {
    // Singleton
    private static CrimeLab sCrimeLab;
    private Context mContext;

    // SQLIteOpenHelper is a class designed to get rid of the grunt work of opening
    // a SQLiteDatabase
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    // Load list with temporary crime objects.
    private CrimeLab(Context context) {

        // Store activity context so it will never be cleaned up by the garbage collector
        // even if the user navigated away from that activity.
        // This works because CrimeLab is a Singleton so it will be around until the app is shut
        // down.
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

    }

    public void addCrime(Crime c) {
        ContentValues values = getContentValues(c);

        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public void deleteCrime(Crime c) {
        String uuidString = c.getId().toString();

        mDatabase.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ?", new String[]{uuidString});
    }

    // Walk the cursor and populate Crime list.
    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        // Database cursor are called cursors because they always have their finger
        // on a particular place in a query.
        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst(); // move cursor to the first row.
            while (!cursor.isAfterLast()) { // if their is a next observation.
                crimes.add(cursor.getCrime()); // add the crime data at that observation to the list
                cursor.moveToNext(); // Then move to the next observation.
            }
        } finally {
            cursor.close(); // Close the cursor regardless of try result.
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {

        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " =?",
                new String[]{id.toString()}
        );

        try {
            if (cursor.getCount() == 0) { // if cursor does not have any observations then ABORT!
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    // This code does not create any files on the filesystem.
    // It only returns File objects that point to the right locations.
    public File getPhotoFile(Crime crime) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?", new String[]{uuidString});
    }

    // Use query(...) in a convenience method to call this on your CrimeTable.
    // Wrap the cursor received from query into a CrimeCursorWrapper.
    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new CrimeCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }
}
