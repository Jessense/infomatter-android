package com.example.newsfeed;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
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
    private int lastVisibleItem = 0;
    private int last_id = 1000000;
    private int batch_size = 15;

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
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter.isHasMore() == true && ((lastVisibleItem == adapter.getItemCount() - 5) || (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == adapter.getItemCount() - 1))) {
                    last_id = adapter.getLastStarId();
                    Log.d("MainActivity", "onScrollStateChanged: last_id=" + last_id);
                    getStarsList();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
            }
        });
        entryList = new ArrayList<>();
        getStarsList1();
    }

    //初始请求用户收藏列表
    private void getStarsList1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/starring")
                            .addHeader("user_id", user.getId())
                            .addHeader("last_id", String.valueOf(last_id))
                            .addHeader("batch_size", String.valueOf(batch_size))
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
                            .addHeader("last_id", String.valueOf(last_id))
                            .addHeader("batch_size", String.valueOf(batch_size))
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("StarsActivity", "run: responseData:" + responseData.substring(0, 100));
                    Gson gson = new Gson();
                    List<Entry> newData = gson.fromJson(responseData, new TypeToken<List<Entry>>(){}.getType());
                    adapter.updateList(newData);
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
