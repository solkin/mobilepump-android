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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity implements AddDownloadDialog.DownloadAdder{

    private DownloadManager mDownloadManager;
    private BroadcastReceiver mDownloadCompleteReceiver;
    private BroadcastReceiver mDownloadNotificationClickedReceiver;
    private BroadcastReceiver mViewDownloadsReceiver;
    private ListView mListView;
    private DownloadAdapter mDownloadAdapter;

    private boolean isOnlyWiFi = true;
    private boolean isNotAllowedOverRoaming = true;

    private String DEFAULT_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + Environment.DIRECTORY_DOWNLOADS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        mDownloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MainActivity.this, "Download Complete", Toast.LENGTH_LONG).show();
            }
        };
        registerReceiver(mDownloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        mDownloadNotificationClickedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MainActivity.this, "Notification Clicked", Toast.LENGTH_LONG).show();
            }
        };
        registerReceiver(mDownloadNotificationClickedReceiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        mViewDownloadsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MainActivity.this, "Action View Downloads", Toast.LENGTH_LONG).show();
            }
        };
        registerReceiver(mViewDownloadsReceiver, new IntentFilter(DownloadManager.ACTION_VIEW_DOWNLOADS));

        mListView = (ListView) findViewById(R.id.list);
        mDownloadAdapter = new DownloadAdapter(this, getLoaderManager());
        mListView.setAdapter(mDownloadAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(mDownloadManager.getUriForDownloadedFile(id), mDownloadManager.getMimeTypeForDownloadedFile(id));
                startActivity(intent);
                Log.d(Constants.TAG, "Open file " + id );
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                AddDownloadDialog.showWithParameters(getFragmentManager(), intent.getStringExtra(Intent.EXTRA_TEXT));
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                AddDownloadDialog.showWithParameters(getFragmentManager(), intent.getStringExtra(Intent.EXTRA_TEXT));
            }
        } else if (Intent.ACTION_VIEW.equals(action)) {
            AddDownloadDialog.showWithParameters(getFragmentManager(), intent.getDataString());
        }

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

    // TODO: Возможно нужно вынести в отдельный класс вместе с различными сетевыми настройками
    public void addDownload(String stringUri, String fileName, String filePath) throws IllegalArgumentException, EmptyUriException{
        DownloadManager.Request request;

        if (TextUtils.isEmpty(stringUri.trim())){
            throw new EmptyUriException();
        }

        try {
            Uri uri = Uri.parse(stringUri);
            request = new DownloadManager.Request(uri);
        } catch (NullPointerException e)  {
            Log.e(Constants.TAG, "exception", e);
            return;
        }

        if (TextUtils.isEmpty(filePath.trim())) {
            filePath = DEFAULT_DIR;
        }

        File fileDir = new File(filePath.trim());
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        } else if (fileDir.isFile()) {
            fileDir = fileDir.getParentFile();
        }

        if (TextUtils.isEmpty(fileName.trim())){
            fileName = stringUri.substring(stringUri.lastIndexOf('/') + 1);
        }

        request.setDestinationUri(Uri.parse(new File(fileDir, fileName).toURI().toString()));

        request.setTitle(stringUri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        if (isOnlyWiFi){
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }
        if (isNotAllowedOverRoaming) {
            request.setAllowedOverRoaming(false);
        }

        try {
            mDownloadManager.enqueue(request);
        } catch (SecurityException e) {
            // Нет доступа к директории
            Log.d(Constants.TAG, e.getMessage());
            request.setDestinationUri(Uri.parse(new File(new File(DEFAULT_DIR), fileName).toURI().toString()));
            mDownloadManager.enqueue(request);
        }

    }
}
