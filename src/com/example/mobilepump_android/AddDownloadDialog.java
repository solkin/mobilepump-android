package com.example.mobilepump_android;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created with IntelliJ IDEA.
 * User: lapshin
 * Date: 10/6/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddDownloadDialog extends DialogFragment {

    public interface AddDownloadDialogListener {
        public void onDialogPositiveClick(String uri, String fileName, String filePath);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    AddDownloadDialogListener mListener;
    View dialogView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (AddDownloadDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public static void show(FragmentManager fragmentManager){
        AddDownloadDialog dialog = new AddDownloadDialog();
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
                        mListener.onDialogPositiveClick(uri, fileName, filePath);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AddDownloadDialog.this);
                    }
                });
        return builder.create();
    }

}
