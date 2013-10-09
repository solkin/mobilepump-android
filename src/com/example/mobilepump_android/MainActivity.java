package com.example.mobilepump_android;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class MainActivity extends Activity implements AddDownloadDialog.DownloadAdder{

    private DownloadManager mDownloadManager;
    private BroadcastReceiver mReceiver;
    private ListView mListView;
    private DownloadAdapter mDownloadAdapter;

    private boolean isOnlyWiFi = true;
    private boolean isNotAllowedOverRoaming = true;

    private String DEFAULT_PATH = Environment.getExternalStorageDirectory().getPath() + Environment.DIRECTORY_DOWNLOADS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE){
                    Toast.makeText(MainActivity.this, "Download Complete", Toast.LENGTH_LONG).show();
                } else if (action == DownloadManager.ACTION_NOTIFICATION_CLICKED) {
                    Toast.makeText(MainActivity.this, "Notification Clicked", Toast.LENGTH_LONG).show();
                } else if (action == DownloadManager.ACTION_VIEW_DOWNLOADS) {
                    Toast.makeText(MainActivity.this, "Action View Downloads", Toast.LENGTH_LONG).show();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        filter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        registerReceiver(mReceiver, filter);

        mListView = (ListView) findViewById(R.id.list);
        mDownloadAdapter = new DownloadAdapter(this, getLoaderManager());
        mListView.setAdapter(mDownloadAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_download: {
                AddDownloadDialog.show(getFragmentManager());
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addDownload(String stringUri, String fileName, String filePath) {
        DownloadManager.Request request;

        try {
            Uri uri = Uri.parse(stringUri);
            request = new DownloadManager.Request(uri);
        } catch (NullPointerException e)  {
            Log.e(Constants.TAG, "exception", e);
            return;
        } catch (IllegalArgumentException e) {
            // Протокол должен быть либо HTTP, либо HTTPS
            Log.e(Constants.TAG, "exception", e);
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG);
            return;
        }


        String destinationUri;
        if (fileName.isEmpty()){
            fileName = stringUri.substring(stringUri.lastIndexOf('/') + 1, stringUri.length());
        }

        if (!filePath.isEmpty()) {
            //filePath = DEFAULT_PATH;
            if (filePath.endsWith("/")){
                destinationUri = "file://" + filePath + fileName;
            } else {
                destinationUri = "file://" + filePath + "/" + fileName;
            }
            request.setDestinationUri(Uri.parse(destinationUri));
        }

        request.setTitle(stringUri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        if (isOnlyWiFi){
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }
        if (isNotAllowedOverRoaming) {
            request.setAllowedOverRoaming(false);
        }

        mDownloadManager.enqueue(request);
    }
}
