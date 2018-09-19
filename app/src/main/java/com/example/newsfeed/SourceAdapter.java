package com.example.newsfeed;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.ViewHolder> {
    private List<Source> mSourceList;
    private Context context;
    private User user;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView sourceName;
        public Button followButton;
        public ViewHolder(View view) {
            super(view);
            sourceName = (TextView) view.findViewById(R.id.source_name);
            followButton = (Button) view.findViewById(R.id.button_follow);
        }
    }

    public SourceAdapter (List<Source> sourceList, Context context) {
        this.mSourceList = sourceList;
        this.context = context;
        this.user = new User(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.source, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Source source = mSourceList.get(position);

        holder.sourceName.setText(source.getName());
        GetRelationTask getRelationTask = new GetRelationTask(holder.followButton);
        getRelationTask.executeOnExecutor(Executors.newCachedThreadPool(), user.getId(), source.getId());


//        getRelationRequest(user.getId(), source.getId());
//        if (result) {
//            holder.followButton.setText("following");
//        } else {
//            holder.followButton.setText("follow");
//        }



        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowTask followTask = new FollowTask(holder.followButton);
                String curState = holder.followButton.getText().toString();
                String action;
                if(curState.equals("following")) {
                    action = "unfollow";
                } else {
                    action = "follow";
                }
                followTask.executeOnExecutor(Executors.newCachedThreadPool(), user.getId(), source.getId(), action);
            }
        });


    }

//    private void getRelationRequest(final String user_id, final String source_id) {
//        final Boolean[] result = new Boolean[1];
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    OkHttpClient client = new OkHttpClient();
//                    Request request = new Request.Builder()
//                            .url("http://188.131.178.76:3000/users/isfollowing")
//                            .addHeader("user_id", user_id)
//                            .addHeader("source_id", source_id)
//                            .build();
//                    Response response = client.newCall(request).execute();
//                    String responseData = response.body().string();
//                    result[0] = Boolean.parseBoolean(responseData);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//
//
//    private void getFollowRequest(final String user_id, final String source_id) {
//        final String[] result = new String[1];
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    OkHttpClient client = new OkHttpClient();
//                    Request request = new Request.Builder()
//                            .url("http://188.131.178.76:3000/users/follow")
//                            .addHeader("user_id", user_id)
//                            .addHeader("source_id", source_id)
//                            .build();
//                    Response response = client.newCall(request).execute();
//                    result[0] = response.body().string();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//    private void getUnfollowRequest(final String user_id, final String source_id) {
//        final String[] result = new String[1];
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    OkHttpClient client = new OkHttpClient();
//                    Request request = new Request.Builder()
//                            .url("http://188.131.178.76:3000/users/unfollow")
//                            .addHeader("user_id", user_id)
//                            .addHeader("source_id", source_id)
//                            .build();
//                    Response response = client.newCall(request).execute();
//                    String result = response.body().string();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

    class GetRelationTask extends AsyncTask<String, Void, Boolean> {
        private Button followbtn;
        String user_id;
        String source_id;
        public GetRelationTask(Button btn) {
            followbtn = btn;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            user_id = params[0];
            source_id = params[1];
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://188.131.178.76:3000/users/isfollowing")
                        .addHeader("user_id", user_id)
                        .addHeader("source_id", source_id)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Boolean result = Boolean.parseBoolean(responseData);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                followbtn.setText("following");
            } else {
                followbtn.setText("follow");
            }
        }
    }

    class FollowTask extends AsyncTask<String, Void, String> {
        private Button followbtn;
        String user_id;
        String source_id;
        String action;
        public FollowTask(Button btn) {
            followbtn = btn;
        }

        @Override
        protected String doInBackground(String... params) {
            user_id = params[0];
            source_id = params[1];
            action = params[2];
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://188.131.178.76:3000/users/" + action)
                        .addHeader("user_id", user_id)
                        .addHeader("source_id", source_id)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                return responseData;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("SUCCESS")) {
                if (action.equals("follow")) {
                    followbtn.setText("following");
                } else {
                    followbtn.setText("follow");
                }
            }
        }
    }





    @Override
    public int getItemCount() {
        return mSourceList.size();
    }
}
