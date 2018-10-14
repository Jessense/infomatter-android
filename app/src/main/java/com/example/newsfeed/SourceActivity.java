package com.example.newsfeed;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SourceActivity extends AppCompatActivity {
    List<Entry> entryList;
    RecyclerView recyclerView;
    EntryAdapter adapter;
    String source_id;
    String source_name;
    TextView sourceTitle;
    Config config;
//    public SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        config = new Config();

        Intent intent = getIntent();
        source_id = intent.getStringExtra("source_id");
        source_name = intent.getStringExtra("source_name");
        sourceTitle = (TextView) findViewById(R.id.source_page_title);
        sourceTitle.setText(source_name);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view2);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        entryList = new ArrayList<>();
        getEntryList(source_id);
//        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh2);
//        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//        user = new User(getApplicationContext());
//        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getEntryList(source_id);
//            }
//        });
    }

    private void getEntryList(final String source_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/sources/timeline")
                            .addHeader("source_id", source_id)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    entryList = gson.fromJson(responseData, new TypeToken<List<Entry>>(){}.getType());
                    showResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                swipeRefresh.setRefreshing(false);
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
}
