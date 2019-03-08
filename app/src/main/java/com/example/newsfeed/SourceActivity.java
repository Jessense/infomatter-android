package com.example.newsfeed;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

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

public class SourceActivity extends AppCompatActivity {
    private List<Entry> entryList;
    private RecyclerView recyclerView;
    private EntryAdapter adapter;
    private String source_id;
    private String source_name;
    private TextView sourceTitle;
    private Config config;
    public SwipeRefreshLayout swipeRefresh;
    private int lastVisibleItem = 0;
    private int last_id = 1000000;
    private String last_time = "9999-12-31 23:59:59";
    private int batch_size = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        config = new Config();

        Intent intent = getIntent();
        source_id = intent.getStringExtra("source_id");
        source_name = intent.getStringExtra("source_name");
//        sourceTitle = (TextView) findViewById(R.id.source_page_title);
//        sourceTitle.setText(source_name);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(source_name);


        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter.isHasMore() == true && ((lastVisibleItem == adapter.getItemCount() - 5) || (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == adapter.getItemCount() - 1))) {
                    last_time = adapter.getLastTime();
                    last_id = adapter.getLastId();
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                    TemporalAccessor accessor = timeFormatter.parse(last_time);
                    Date pubDate = Date.from(Instant.from(accessor));
                    LocalDateTime localPubDate = pubDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    String temp = localPubDate.toString();
                    last_time = temp.substring(0, 10) + " " + temp.substring(11, 16);
                    if (temp.length() < 19) {
                        last_time += ":00";
                    } else {
                        last_time += temp.substring(16, 19);
                    }
                    Log.d("MainActivity", "onScrollStateChanged: localPubDate" + localPubDate);
                    Log.d("MainActivity", "onScrollStateChanged: last_time=" + last_time);
                    Log.d("MainActivity", "onScrollStateChanged: last_id=" + last_id);
                    getEntryList();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
            }
        });
        entryList = new ArrayList<>();
        getEntryList1();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (entryList.size() > 0) {
                    adapter.resetList();
                    entryList = new ArrayList<>();
                    last_id = 0;
                    last_time = "9999-12-31 23:59:59";
                    getEntryList1();
                } else {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }


    private void getEntryList1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Log.d("SourceActivity", "getEntryList1: last_time=" + last_time);
                    Log.d("Sourcectivity", "getEntryList1: last_id=" + last_id);
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/sources/timeline_batch")
                            .addHeader("source_id", source_id)
                            .addHeader("last_time", last_time)
                            .addHeader("last_id", String.valueOf(last_id))
                            .addHeader("batch_size", String.valueOf(batch_size))
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("SourceActivity", "run: getEntryList1:" + responseData);
                    Gson gson = new Gson();
                    entryList = gson.fromJson(responseData, new TypeToken<List<Entry>>(){}.getType());
                    showResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                swipeRefresh.setRefreshing(false);
            }
        }).start();
    }

    //获取Entry列表
    private void getEntryList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/sources/timeline_batch")
                            .addHeader("source_id", source_id)
                            .addHeader("last_time", last_time)
                            .addHeader("last_id", String.valueOf(last_id))
                            .addHeader("batch_size", String.valueOf(batch_size))
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("SourceActivity", "run: getEntryList:" + responseData);
                    Gson gson = new Gson();
                    List<Entry> newData = gson.fromJson(responseData, new TypeToken<List<Entry>>(){}.getType());
                    adapter.updateList(newData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                swipeRefresh.setRefreshing(false);
            }
        }).start();
    }

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
