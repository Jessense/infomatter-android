package com.example.newsfeed;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FollowingActivity extends AppCompatActivity {
    private List<Source> sourceList;
    private RecyclerView recyclerView;
    private SourceAdapter adapter;
    private User user;
    private Config config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        config = new Config();
        user = new User(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.recycler_following);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        sourceList = new ArrayList<>();
        getFollowingList();
    }

    //请求用户关注列表
    private void getFollowingList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/following")
                            .addHeader("user_id", user.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("FollowingActivity", "run: responseData:" + responseData.substring(0, 100));
                    Gson gson = new Gson();
                    sourceList = gson.fromJson(responseData, new TypeToken<List<Source>>(){}.getType());
                    Log.d("SourceActivity", "run: sourceList" + sourceList.get(0));
                    showResponse(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //显示请求的回复
    private void showResponse (final Boolean isSearching) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new SourceAdapter(sourceList, isSearching, getApplicationContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }
}
