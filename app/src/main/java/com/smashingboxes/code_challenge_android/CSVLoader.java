package com.smashingboxes.code_challenge_android;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NicholasCook on 7/1/15.
 */
public class CSVLoader extends IntentService {

    public CSVLoader() {
        super(CSVLoader.class.getCanonicalName());
    }

    public final class Constants {
        public static final String BROADCAST_ACTION = "com.smashingboxes.code_challenge_android" +
                ".BROADCAST";
        public static final String EXTENDED_DATA_STATUS = "com.smashingboxes" +
                ".code_challenge_android.STATUS";
        public static final String LOAD_COMPLETE = "com.smashingboxes.code_challenge_android" +
                ".LOAD_COMPLETE";
        public static final String ERROR_LOADING = "com.smashingboxes.code_challenge_android" +
                ".ERROR_LOADING";
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AssetManager assetManager = getBaseContext().getAssets();
        boolean success = true;
        try {
            InputStream inputStream = assetManager.open("items.csv");
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String line;
            String[] values;
            boolean firstLine = true;
            List<ContentValues> allItems = new ArrayList<ContentValues>();
            while ((line = bufferedReader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MainDatabase.COLUMN_ID, values[0]);
                    contentValues.put(MainDatabase.COLUMN_UPC, values[1]);
                    contentValues.put(MainDatabase.COLUMN_ITEM, values[2]);
                    contentValues.put(MainDatabase.COLUMN_MANUFACTURER, values[3]);
                    contentValues.put(MainDatabase.COLUMN_BRAND, values[4]);
                    allItems.add(contentValues);
                }
            }
            ContentValues[] itemsToInsert = new ContentValues[allItems.size()];
            allItems.toArray(itemsToInsert);
            getContentResolver().bulkInsert(DatabaseContentProvider.CONTENT_URI, itemsToInsert);

            inputStream.close();
            streamReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        if (success) {
            localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, Constants.LOAD_COMPLETE);
        } else {
            localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, Constants.ERROR_LOADING);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
