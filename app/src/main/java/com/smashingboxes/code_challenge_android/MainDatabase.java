package com.smashingboxes.code_challenge_android;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by NicholasCook on 7/1/15.
 */
public class MainDatabase {

    public static final String TABLE_NAME = "code_challenge";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UPC = "upc";
    public static final String COLUMN_ITEM = "item_description";
    public static final String COLUMN_MANUFACTURER = "manufacturer";
    public static final String COLUMN_BRAND = "brand";

    private final static String CLASSNAME = "MainDatabase";

    private static final String CREATE_DATABASE = "create table if not exists "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_UPC + " text, "
            + COLUMN_ITEM + " text, "
            + COLUMN_MANUFACTURER + " text, "
            + COLUMN_BRAND + " text);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_DATABASE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(CLASSNAME, "Upgrading database from version " + oldVersion + " to version " + oldVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

}
