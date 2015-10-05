package com.smashingboxes.code_challenge_android;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by NicholasCook on 7/9/15.
 */
public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder> {

    public Cursor cursor;

    public ViewAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public ViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            holder.itemName.setText(cursor.getString(cursor.getColumnIndexOrThrow
                    (MainDatabase.COLUMN_ITEM)));
        } else {
            throw new IllegalStateException("Could not move cursor to correct position: " +
                    position);
        }

    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView itemName;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            itemName = (TextView) itemLayoutView.findViewById(R.id.listItem);
        }
    }

}
