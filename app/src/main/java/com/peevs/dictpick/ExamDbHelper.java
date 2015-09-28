package com.peevs.dictpick;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Random;

/**
 * Created by zarrro on 16.8.2015 г..
 */
public class ExamDbHelper extends SQLiteOpenHelper {

    private static final String TAG = ExamDbHelper.class.getSimpleName();
    private Random rand = new Random(System.currentTimeMillis());

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dictpick.db";

    public ExamDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // rely on ExamDbPrepared.createDatabase() to have been executed
        // db.execSQL(ExamDbContract.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop existing DB create new one
        db.execSQL(ExamDbContract.WordsTable.SQL_DELETE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // the same as upgrade
        onUpgrade(db, oldVersion, newVersion);
    }
}
