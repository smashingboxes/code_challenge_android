package com.smashingboxes.code_challenge_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

public class MainActivity extends Activity implements SearchView.OnQueryTextListener {

    SearchView searchView;
    ProgressBar progressBar;
    ViewAdapter adapter;
    MenuItem searchMenuItem;
    QueryTask queryTask;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean csvLoaded = sharedPref.getBoolean(getString(R.string.csv_loaded), false);
        progressBar = (ProgressBar) findViewById(R.id.loading);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setVisibility(View.GONE);
        if (!csvLoaded) {
            Intent intent = new Intent(this, CSVLoader.class);
            startService(intent);
            IntentFilter intentFilter = new IntentFilter(CSVLoader.Constants.BROADCAST_ACTION);
            ParseStateReceiver parseStateReceiver = new ParseStateReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(parseStateReceiver,
                    intentFilter);
        } else {
            loadData();
        }

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (recyclerView != null && View.VISIBLE == recyclerView.getVisibility()) {
            if (queryTask == null) {
                queryTask = new QueryTask();
            } else {
                queryTask.cancel(true);
                queryTask = new QueryTask();
            }
            queryTask.execute(query);
        }
        return true;
    }

    private class ParseStateReceiver extends BroadcastReceiver {

        private ParseStateReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(CSVLoader.Constants.EXTENDED_DATA_STATUS);
            SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
            if (CSVLoader.Constants.LOAD_COMPLETE.equals(status)) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.csv_loaded), true);
                editor.apply();
                loadData();
            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.csv_loaded), false);
                editor.apply();
                displayAlertDialog();
            }
        }
    }

    public void loadData() {
        setUpAdapter();
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setUpAdapter() {
        Cursor cursor = getAllItems();
        if (cursor != null) {
            adapter = new ViewAdapter(cursor);
            recyclerView.setAdapter(adapter);
        }
    }

    public void displayAlertDialog() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        if (!this.isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.loading_error))
                    .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, CSVLoader.class);
                            startService(intent);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void setupSearchView() {
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_title));
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text",
                null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        setupSearchView();

        return true;
    }

    public Cursor getWordMatches(String query) {
        if (TextUtils.isEmpty(query)) {
            return getAllItems();
        } else {
            String[] projection = new String[]{MainDatabase.COLUMN_ID, MainDatabase.COLUMN_ITEM};
            String selection = MainDatabase.COLUMN_ITEM + " LIKE ?";
            String[] selectionArgs = new String[]{"%" + query + "%"};
            Cursor cursor = getContentResolver().query(DatabaseContentProvider.CONTENT_URI,
                    projection,
                    selection, selectionArgs, null);
            if (cursor == null) {
                return null;
            } else if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }
            return cursor;
        }
    }

    private class QueryTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {
            String query = params[0];
            if (isCancelled()) {
                return null;
            }
            Cursor cursor = getWordMatches(query);
            if (cursor == null) {
                return null;
            } else if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (!isCancelled()) {
                adapter.cursor = cursor;
                adapter.notifyDataSetChanged();
            }
        }
    }

    private Cursor getAllItems() {
        Uri uri = DatabaseContentProvider.CONTENT_URI;
        String[] projection = {MainDatabase.COLUMN_ID, MainDatabase.COLUMN_ITEM};
        return getContentResolver().query(uri, projection, null, null, null);
    }

}
