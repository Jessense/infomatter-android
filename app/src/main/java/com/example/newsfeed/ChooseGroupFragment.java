package com.example.newsfeed;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class ChooseGroupFragment extends DialogFragment {
    private List<Integer> mSelectedItems;
    private User user;
    private String source_id;
    private String groups;
    private Config config;
    private String[] groupArray;
    private List<String> groupList;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        user = new User(getContext());
        config = new Config();
        Bundle bundle = getArguments();
        source_id = bundle.getString("source_id");
        groups = "";
        mSelectedItems = new ArrayList();  // Where we track the selected items
//        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.choose_group, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle("Choose groups")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(user.getGroups2(), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }

                        })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        for (int index:mSelectedItems) {
                            groups += "$" + user.getGroups2()[index] + "$";
                        }
                        Log.d("ChooseGroupFragment", "ChooseGroupOk: " +groups);
                        updateSourceGroup();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNeutralButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
//        final EditText add_edt = (EditText) dialogView.findViewById(R.id.add_group_edt);
//        final ImageView add_submit = (ImageView) dialogView.findViewById(R.id.add_group_submit);
//        add_submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "add group: " + add_edt.getText().toString());
//                groupList.add(add_edt.getText().toString());
//                groupArray = groupList.toArray(new String[0]);
//            }
//        });


        return builder.create();
    }

    private void updateSourceGroup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/edit_source_group?group=" + groups)
                            .addHeader("user_id", user.getId())
                            .addHeader("source_id", source_id)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("ChooseGroupFragment", "updateSourceGroup: " + responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
