package com.example.newsfeed;

import android.app.Service;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DiscoveryActivity extends AppCompatActivity {
    private List<Source> sourceList;
    private RecyclerView recyclerView;
    private SourceAdapter adapter;
    private EditText searchEdit;
    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        config = new Config();

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Discovery");

        recyclerView = (RecyclerView) findViewById(R.id.source_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        sourceList = new ArrayList<>();
        getSourceList();
    }


    //获取内容源列表
    private void getSourceList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/sources")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    sourceList = gson.fromJson(responseData, new TypeToken<List<Source>>(){}.getType());
                    showResponse(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    private void showResponse (final Boolean isSearching) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new SourceAdapter(sourceList, isSearching, getApplicationContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void showToast (final String toastText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DiscoveryActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
