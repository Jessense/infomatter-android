package com.example.newsfeed;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<Group> groupList;
    private Context context;
    private User user;
    private Config config;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private EditText groupName;
        private ImageView delete_btn;
        private ImageView rename_btn;
        public ViewHolder(View view) {
            super(view);
            this.groupName = (EditText) view.findViewById(R.id.group_name);
            this.delete_btn = (ImageView) view.findViewById(R.id.delte_group);
            this.rename_btn = (ImageView) view.findViewById(R.id.rename_group);
        }
    }

    public GroupAdapter(List<Group> groupList, Context context) {
        this.groupList = groupList;
        this.context = context;
        this.user = new User(context);
        this.config = new Config();
    }

    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group, parent, false);
        GroupAdapter.ViewHolder holder = new GroupAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final Group group = groupList.get(i);
        viewHolder.groupName.setText(group.getGroupName());
        viewHolder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroup(viewHolder.groupName.getText().toString(), i);
            }
        });
        viewHolder.rename_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginActivity.isLeagal(viewHolder.groupName.getText().toString())) {
                    if (viewHolder.groupName.getText().toString() != group.getGroupName() && viewHolder.groupName.getText().toString().length() > 0) {
                        viewHolder.groupName.clearFocus();
                        editGroup(group.getGroupName(), viewHolder.groupName.getText().toString(), i);
                    }
                } else {
                    Toast.makeText(context, "Illegal input!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void deleteGroup(final String s, final int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/delete_group?pre_group=" + s)
                            .addHeader("user_id", user.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("GroupActivity", "run: delete group:" + responseData);
                    if (responseData.equals("SUCCESS")) {
                        user.deleteGroups2(s);
                        groupList.remove(i);
                        notifyItemRemoved(i);
                        showToast("Deleted group " + s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void editGroup(final String pre_group, final String new_group, final int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/edit_group?pre_group=" + pre_group + "&new_group=" + new_group)
                            .addHeader("user_id", user.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("GroupActivity", "run: edit group:" + responseData);
                    if (responseData.equals("SUCCESS")) {
                        user.editGroups2(pre_group, new_group);
                        groupList.set(i, new Group(new_group));
                        notifyItemChanged(i);
                        showToast("Renamed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void addData(String s) {
        groupList.add(new Group(s));
        notifyDataSetChanged();
    }

    private void showToast (final String toastText) {
        ((GroupActivity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }
}
