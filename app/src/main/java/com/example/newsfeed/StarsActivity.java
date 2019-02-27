package com.example.newsfeed;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StarsActivity extends AppCompatActivity {

    private User user;
    private Config config;
    private RecyclerView recyclerView;
    private List<Entry> entryList;
    private EntryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stars);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Stars");

        config = new Config();
        user = new User(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.recycler_stars);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        entryList = new ArrayList<>();
        getStarsList();
    }

    //请求用户关注列表
    private void getStarsList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/starring")
                            .addHeader("user_id", user.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("StarsActivity", "run: responseData:" + responseData.substring(0, 100));
                    Gson gson = new Gson();
                    entryList = gson.fromJson(responseData, new TypeToken<List<Entry>>(){}.getType());
                    Log.d("EntryActivity", "run: entryList" + entryList.get(0));
                    showResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //显示请求的回复
    private void showResponse () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new EntryAdapter(entryList, getApplicationContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
