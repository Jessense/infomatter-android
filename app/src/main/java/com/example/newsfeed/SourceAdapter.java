package com.example.newsfeed;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.ViewHolder> {
    private List<Source> mSourceList;
    private Context context;
    private User user;
    private Boolean isSearchingRSS;
    private Config config;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView sourceName;
        public Button followButton;
        public ViewHolder(View view) {
            super(view);
            sourceName = (TextView) view.findViewById(R.id.source_name);
            followButton = (Button) view.findViewById(R.id.button_follow);
        }
    }

    public SourceAdapter (List<Source> sourceList, Boolean isSearchingRSS,Context context) {
        this.mSourceList = sourceList;
        this.context = context;
        this.user = new User(context);
        this.isSearchingRSS = isSearchingRSS;
        this.config = new Config();
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
        getRelationTask.executeOnExecutor(Executors.newCachedThreadPool(), source);


//        getRelationRequest(user.getId(), source.getId());
//        if (result) {
//            holder.followButton.setText("following");
//        } else {
//            holder.followButton.setText("follow");
//        }

        holder.sourceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SourceActivity.class);
                intent.putExtra("source_id", source.getId());
                intent.putExtra("source_name", source.getName());
                context.startActivity(intent);
            }
        });


        //用户点添加、关注、取关按钮，分别异步执行三种任务
        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curState = holder.followButton.getText().toString();
                String action;
                if(curState.equals("following")) {
                    action = "unfollow";
                    FollowTask followTask = new FollowTask(holder.followButton);
                    followTask.executeOnExecutor(Executors.newCachedThreadPool(), user.getId(), source.getId(), action);
                } else if(curState.equals("+ follow")){
                    action = "follow";
                    FollowTask followTask = new FollowTask(holder.followButton);
                    followTask.executeOnExecutor(Executors.newCachedThreadPool(), user.getId(), source.getId(), action);
                } else if(curState.equals("+ add")){
                    AddTask addTask = new AddTask(holder.followButton);
                    addTask.executeOnExecutor(Executors.newCachedThreadPool(), source);
                }
            }
        });

    }


    //请求用户的订阅列表
    class GetRelationTask extends AsyncTask<Source, Void, String> {
        private Button followbtn;
        Source source;
        public GetRelationTask(Button btn) {
            followbtn = btn;
        }

        @Override
        protected String doInBackground(Source... params) {
            source = params[0];
            try {
                String result;
                if (isSearchingRSS) {
                    HttpUrl request_url = new HttpUrl.Builder()
                            .scheme(config.getScheme())
                            .host(config.getHost())
                            .port(config.getPort())
                            .addPathSegment("sources")
                            .addPathSegment("search")
                            .addQueryParameter("link", source.getLink())
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(request_url)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("SubscribeActivity", "doInBackground: responseRSS:"+responseData);
                    if (responseData.equals("NOTFOUND")) {
                        result = "+ add";
                        Log.d("SubscribeActivity", "doInBackground: relationResult="+result);
                    } else {
                        Gson gson = new Gson();
                        List<Source> sourceList = gson.fromJson(responseData, new TypeToken<List<Source>>(){}.getType());
                        source.setId(sourceList.get(0).getId());
                        OkHttpClient client2 = new OkHttpClient();
                        Request request2 = new Request.Builder()
                                .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/isfollowing")
                                .addHeader("user_id", user.getId())
                                .addHeader("source_id", source.getId())
                                .build();
                        Response response2 = client2.newCall(request2).execute();
                        String responseData2 = response2.body().string();
                        Boolean isFollowing = Boolean.parseBoolean(responseData2);
                        if (isFollowing) {
                            result = "following";
                        } else {
                            result = "+ follow";
                        }
                    }
                } else {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/isfollowing")
                            .addHeader("user_id", user.getId())
                            .addHeader("source_id", source.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Boolean isFollowing = Boolean.parseBoolean(responseData);
                    if (isFollowing) {
                        result = "following";
                    } else {
                        result = "+ follow";
                    }
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String text) {
            super.onPostExecute(text);
            if (text.equals("following")) {
                followbtn.setText("following");
                followbtn.setTextColor(context.getResources().getColor(R.color.simplenoteBlue));
                followbtn.setBackgroundResource(R.drawable.border_line_blue);

            } else if(text.equals("+ follow")){
                followbtn.setText("+ follow");
                followbtn.setTextColor(context.getResources().getColor(R.color.white));
                followbtn.setBackgroundResource(R.drawable.btn_bg_blue);
            } else if(text.equals(("+ add"))){
                followbtn.setText("+ add");
                followbtn.setTextColor(context.getResources().getColor(R.color.white));
                followbtn.setBackgroundResource(R.drawable.btn_bg_blue);
            }
        }
    }

    //用户请求关注某一内容源
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
                        .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/" + action)
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
                    followbtn.setTextColor(context.getResources().getColor(R.color.simplenoteBlue));
                    followbtn.setBackgroundResource(R.drawable.border_line_blue);

                } else {
                    followbtn.setText("+ follow");
                    followbtn.setTextColor(context.getResources().getColor(R.color.white));
                    followbtn.setBackgroundResource(R.drawable.btn_bg_blue);
                }
            }
        }
    }


    //用户请求添加某一内容源
    class AddTask extends AsyncTask<Source, Void, Integer> {
        private Button followbtn;
        Source source;
        public AddTask(Button btn) {
            followbtn = btn;
        }

        @Override
        protected Integer doInBackground(Source... params) {
            source = params[0];
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("name", source.getName())
                        .add("link",source.getLink())
                        .add("feedUrl", source.getFeedUrl())
                        .add("photo", source.getPhoto())
                        .build();

                Request request = new Request.Builder()
                        .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/sources/add")
                        .post(formBody)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Gson gson = new Gson();
                JSONArray jsonArray = new JSONArray(responseData);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                Integer result = jsonObject.getInt("LAST_INSERT_ID()");
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            source.setId(result.toString());
            followbtn.setText("+ follow");
            followbtn.setTextColor(context.getResources().getColor(R.color.white));
            followbtn.setBackgroundResource(R.drawable.btn_bg_blue);
        }
    }





    @Override
    public int getItemCount() {
        return mSourceList.size();
    }
}
