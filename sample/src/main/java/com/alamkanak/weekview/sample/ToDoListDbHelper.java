package com.alamkanak.weekview.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Ming on 14/5/2015.
 */
public class ToDoListDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ToDoListDbContract.ToDoListDbEntry.TABLE_NAME + " (" +
                    ToDoListDbContract.ToDoListDbEntry._ID + " INTEGER PRIMARY KEY, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STARTDATETIME + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_ENDDATETIME + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_JOBDESC + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_PHOTO + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_AUDIO + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_SMS + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_COLOR + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STATUS + " TEXT, " +
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_YEAR + " TEXT, "+
                    ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_MONTH + " TEXT "
                    + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ToDoListDbContract.ToDoListDbEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version
    public static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "todoListDb.db";

    public ToDoListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("HELPER", "BookDbHelper constructor called");
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d("CREATE", SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
