package com.smashingboxes.code_challenge_android;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by NicholasCook on 7/1/15.
 */
public class DatabaseContentProvider extends ContentProvider {

    private DatabaseHelper databaseHelper;

    private static final String AUTHORITY = "com.smashingboxes.code_challenge_android";
    private static final String BASE_PATH = "code_challenge_android";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + BASE_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/" + BASE_PATH;

    private static final int ALL_ITEMS = 1;
    private static final int SINGLE_ITEM = 2;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ALL_ITEMS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SINGLE_ITEM);
    }


    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(MainDatabase.TABLE_NAME);

        switch (sURIMatcher.match(uri)) {
            case ALL_ITEMS:
                break;
            case SINGLE_ITEM:
                String id = uri.getPathSegments().get(1);
                sqLiteQueryBuilder.appendWhere(MainDatabase.COLUMN_ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, strings, s, strings1, null, null, s1);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case ALL_ITEMS:
                id = sqLiteDatabase.insert(MainDatabase.TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case ALL_ITEMS:
                rowsUpdated = sqlDB.update(MainDatabase.TABLE_NAME,
                        contentValues,
                        s,
                        strings);
                break;
            case SINGLE_ITEM:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(s)) {
                    rowsUpdated = sqlDB.update(MainDatabase.TABLE_NAME,
                            contentValues,
                            MainDatabase.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(MainDatabase.TABLE_NAME,
                            contentValues,
                            MainDatabase.COLUMN_ID + "=" + id
                                    + " and "
                                    + s,
                            strings);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}
