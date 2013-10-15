package com.example.mobilepump_android;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: lapshin
 * Date: 10/6/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddDownloadDialog extends DialogFragment {

    public static final String URI = "uri";
    public static final String FILE_PATH = "file_path";
    public static final String FILE_NAME = "file_name";

    public interface DownloadAdder{
        public void addDownload(String uri, String fileName, String filePath) throws EmptyUriException;
    }

    DownloadAdder mListener;
    View dialogView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (DownloadAdder) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public static void show(FragmentManager fragmentManager){
        showWithData(fragmentManager, null);
    }

    public static void showWithParameters(FragmentManager fragmentManager, String uri){
        Bundle data = new Bundle();
        data.putString(URI, uri);
        showWithData(fragmentManager, data);
    }

    public static void showWithData(FragmentManager fragmentManager, Bundle data){
        AddDownloadDialog dialog = new AddDownloadDialog();
        if (data != null) {
            dialog.setArguments(data);
        }
        dialog.show(fragmentManager, "add_download_dialog");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.add_download_dialog, null);
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String uri = ((EditText) dialogView.findViewById(R.id.uri)).getText().toString();
                        String fileName = ((EditText) dialogView.findViewById(R.id.file_name)).getText().toString();
                        String filePath = ((EditText) dialogView.findViewById(R.id.file_path)).getText().toString();
                        try {
                            mListener.addDownload(uri, fileName, filePath);
                        } catch (EmptyUriException ignored) {
                            Toast.makeText(getActivity(), "Download URI is empty", Toast.LENGTH_LONG);
                            return;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddDownloadDialog.this.getDialog().cancel();
                    }
                });

        // Set text fields if dialog show with arguments
        Bundle data = getArguments();
        if (data != null){
            String uri = data.getString(URI);
            if (uri != null) {
                ((EditText) dialogView.findViewById(R.id.uri)).setText(uri);

                String fileName = data.getString(FILE_NAME);
                if (fileName != null) {
                    ((EditText) dialogView.findViewById(R.id.file_name)).setText(fileName);
                }

                String filePath = data.getString(FILE_PATH);
                if (filePath != null) {
                    ((EditText) dialogView.findViewById(R.id.file_path)).setText(fileName);
                }
            }
        }
        return builder.create();
    }

}
