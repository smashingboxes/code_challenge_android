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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity implements SearchView.OnQueryTextListener {

    ListView listView;
    SearchView searchView;
    ProgressBar progressBar;
    private ArrayAdapter<String> adapter;
    public ArrayList<String> allItems = new ArrayList<String>(); //Everything in the DB
    public ArrayList<String> currentItems = new ArrayList<String>(); //Everything currently loaded in the UI
    public ArrayList<String> overallItems = new ArrayList<String>(); //Preserves non-queried items
    MenuItem searchMenuItem;
    private QueryTask queryTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean csvLoaded = sharedPref.getBoolean(getString(R.string.csv_loaded), false);
        progressBar = (ProgressBar) findViewById(R.id.loading);
        setupListView();
        if (!csvLoaded) {
            Intent intent = new Intent(this, CSVLoader.class);
            startService(intent);
            IntentFilter intentFilter = new IntentFilter(CSVLoader.Constants.BROADCAST_ACTION);
            ParseStateReceiver parseStateReceiver = new ParseStateReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(parseStateReceiver, intentFilter);
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
        if (TextUtils.isEmpty(query) && listView != null && View.VISIBLE == listView.getVisibility()) {
            currentItems.clear();
            currentItems.addAll(overallItems);
            adapter.notifyDataSetChanged();
            overallItems.clear();
            listView.setOnScrollListener(new ListViewScrollListener());
        } else if (listView != null && View.VISIBLE == listView.getVisibility()) {
            if (overallItems.isEmpty()) {
                overallItems.addAll(currentItems);
            }
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
            if (CSVLoader.Constants.LOAD_COMPLETE.equals(status)) {
                SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.csv_loaded), true);
                editor.commit();
                loadData();
            } else {
                SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.csv_loaded), false);
                editor.commit();
                displayAlertDialog();
            }
        }
    }

    public void loadData() {
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        Uri uri = DatabaseContentProvider.CONTENT_URI;
        String[] projection = {MainDatabase.COLUMN_ITEM};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int totalDisplayed = 0;
                do {
                    String current = cursor.getString(cursor.getColumnIndex(MainDatabase.COLUMN_ITEM));
                    allItems.add(current);
                    if (totalDisplayed < 50) {
                        currentItems.add(current);
                        totalDisplayed++;
                        adapter.notifyDataSetChanged();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public void displayAlertDialog() {
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        if (!this.isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.loading_error))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void setupListView() {
        listView = (ListView) findViewById(R.id.list);
        listView.setVisibility(View.GONE);
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, currentItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setSingleLine(false);
                return textView;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new ListViewScrollListener());
    }

    private void setupSearchView() {
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_hint));
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
    }

    private class ListViewScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 50;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

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
                //Load the next 50 items
                //firstScroll = false;
                int start = 50 * currentPage;
                for (int i = start; i <= start + 50 && i < allItems.size(); i++) {
                    currentItems.add(allItems.get(i));
                    adapter.notifyDataSetChanged();
                }
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

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
        String[] projection = new String[]{MainDatabase.COLUMN_ITEM};
        String selection = MainDatabase.COLUMN_ITEM + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        Cursor cursor = getContentResolver().query(DatabaseContentProvider.CONTENT_URI, projection,
                selection, selectionArgs, null);
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private class QueryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String query = params[0];
            currentItems.clear();
            Cursor cursor = getWordMatches(query);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        if (isCancelled()) {
                            return null;
                        }
                        String current = cursor.getString(cursor.getColumnIndex(MainDatabase.COLUMN_ITEM));
                        currentItems.add(current);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            listView.setOnScrollListener(null);
            adapter.notifyDataSetChanged();
        }
    }

}
