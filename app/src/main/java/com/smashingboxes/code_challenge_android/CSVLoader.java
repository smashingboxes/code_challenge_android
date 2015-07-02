package com.smashingboxes.code_challenge_android;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by NicholasCook on 7/1/15.
 */
public class CSVLoader extends IntentService {

    public CSVLoader() {
        super("CSVLoader");
    }

    public final class Constants {
        public static final String BROADCAST_ACTION = "com.smashingboxes.code_challenge_android.BROADCAST";
        public static final String EXTENDED_DATA_STATUS = "com.smashingboxes.code_challenge_android.STATUS";
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        AssetManager assetManager = getBaseContext().getAssets();
        boolean success = true;
        try {
            InputStream inputStream = assetManager.open("items.csv");
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String line;
            String[] values;
            boolean firstLine = true;
            while ((line = bufferedReader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                for (int i = 2; i < 5; i++) {
                    if (!values[i].startsWith("\"")) {
                        values[i] = "\"" + values[i] + "\"";
                    }
                }
                String insertCommand = "insert into " + MainDatabase.TABLE_NAME + " ("
                        + MainDatabase.COLUMN_ID + ", "
                        + MainDatabase.COLUMN_UPC + ", "
                        + MainDatabase.COLUMN_ITEM + ", "
                        + MainDatabase.COLUMN_MANUFACTURER + ", "
                        + MainDatabase.COLUMN_BRAND
                        + ") values("
                        + values[0] + ", "
                        + values[1] + ", "
                        + values[2] + ", "
                        + values[3] + ", "
                        + values[4] + ")";
                sqLiteDatabase.execSQL(insertCommand);
            }
            inputStream.close();
            streamReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        if (success) {
            localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, "Load complete");
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        } else {
            localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, "Error loading data");
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }

    }
}
