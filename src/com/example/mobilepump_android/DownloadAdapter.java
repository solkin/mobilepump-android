package com.example.mobilepump_android;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class DownloadAdapter extends CursorAdapter implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int DOWNLOAD_ADAPTER_ID = 1;

    private Context mContext;
    private LayoutInflater mInflater;
    private DownloadManager mDownloadManager;

    public DownloadAdapter(Activity context, LoaderManager loaderManager) {
        super(context, null, 0x00);
        mContext = context;
        mInflater = context.getLayoutInflater();
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        loaderManager.initLoader(DOWNLOAD_ADAPTER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new DownloadCursorLoader(mContext, new DownloadManager.Query());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swapCursor(null);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.download_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.download_item_id)).setText(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
    }

    /* *
     *  @return uniq download id for Item
     */
    @Override
    public long getItemId(int position) {
        Cursor cursor = getCursor();
        if (cursor != null) {
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
