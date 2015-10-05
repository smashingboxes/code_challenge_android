package com.smashingboxes.code_challenge_android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by NicholasCook on 7/1/15.
 */
public class DatabaseContentProvider extends ContentProvider {

    private DatabaseHelper databaseHelper;

    private static final String AUTHORITY = "com.smashingboxes.code_challenge_android";
    private static final String BASE_PATH = "code_challenge_android";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
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
        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection,
                selectionArgs, null, null, sortOrder);
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
        long id;
        switch (uriType) {
            case ALL_ITEMS:
                id = sqLiteDatabase.insertWithOnConflict(MainDatabase.TABLE_NAME, null,
                        contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] contentValues) {
        int numInserted = 0;
        int uriType = sURIMatcher.match(uri);
        String tableName;
        switch (uriType) {
            case ALL_ITEMS:
                tableName = MainDatabase.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        for (ContentValues cv : contentValues) {
            sqLiteDatabase.insertWithOnConflict(tableName, null,
                    cv, SQLiteDatabase.CONFLICT_IGNORE);
            numInserted++;
        }
        sqLiteDatabase.setTransactionSuccessful();
        getContext().getContentResolver().notifyChange(uri, null);
        sqLiteDatabase.endTransaction();
        return numInserted;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

}
