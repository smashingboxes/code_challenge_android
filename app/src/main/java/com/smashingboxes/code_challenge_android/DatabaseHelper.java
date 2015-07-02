package com.smashingboxes.code_challenge_android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by NicholasCook on 7/1/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "itemstable.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        MainDatabase.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        MainDatabase.onUpgrade(sqLiteDatabase, i, i1);
    }
}
