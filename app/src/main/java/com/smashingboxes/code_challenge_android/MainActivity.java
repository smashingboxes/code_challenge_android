package com.smashingboxes.code_challenge_android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class MainActivity extends Activity {

    ListView listView;
    ProgressBar progressBar;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> allItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupListView();
        progressBar = (ProgressBar) findViewById(R.id.loading);
        Intent intent = new Intent(this, CSVLoader.class);
        startService(intent);
        IntentFilter intentFilter = new IntentFilter(CSVLoader.Constants.BROADCAST_ACTION);
        ParseStateReceiver parseStateReceiver = new ParseStateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(parseStateReceiver, intentFilter);
    }

    private class ParseStateReceiver extends BroadcastReceiver {

        private ParseStateReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(CSVLoader.Constants.EXTENDED_DATA_STATUS);
            if ("Load complete".equals(status)) {
                progressBar.setVisibility(View.GONE);
                Uri uri = DatabaseContentProvider.CONTENT_URI;
                String[] projection = {MainDatabase.COLUMN_ID, MainDatabase.COLUMN_ITEM};
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                listView.setVisibility(View.VISIBLE);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            String current = cursor.getString(cursor.getColumnIndex(MainDatabase.COLUMN_ITEM));
                            allItems.add(current);
                            adapter.notifyDataSetChanged();
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + MainDatabase.TABLE_NAME);
    }

    @Override
    public void onStop() {
        super.onStop();
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + MainDatabase.TABLE_NAME);
    }

    private void setupListView() {
        listView = (ListView) findViewById(R.id.list);
        listView.setVisibility(View.GONE);
        allItems = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, allItems);
        listView.setAdapter(adapter);
    }

    /*private class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }
        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                new LoadItems().execute(currentPage + 1);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    private class LoadItems extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            System.out.println(objects);
            return null;
        }




    }*/

}
