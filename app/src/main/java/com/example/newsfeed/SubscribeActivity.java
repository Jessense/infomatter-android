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

public class SubscribeActivity extends AppCompatActivity {
    private List<Source> sourceList;
    private RecyclerView recyclerView;
    private SourceAdapter adapter;
    private EditText searchEdit;
    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        config = new Config();

        searchEdit = (EditText) findViewById(R.id.source_search);
        searchEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    getSearchResult(searchEdit.getText().toString());
                }
                return false;
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.source_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        sourceList = new ArrayList<>();
        getSourceList();
    }



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

    private void getSearchResult(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpUrl request_url;
                    if(text.startsWith(config.getScheme())) {
                        request_url = new HttpUrl.Builder()
                                .scheme(config.getScheme())
                                .host(config.getHost())
                                .port(config.getPort())
                                .addPathSegment("sources")
                                .addPathSegment("search")
                                .addQueryParameter("feedUrl", text)
                                .build();
                    } else {
                        request_url = new HttpUrl.Builder()
                                .scheme(config.getScheme())
                                .host(config.getHost())
                                .port(config.getPort())
                                .addPathSegment("sources")
                                .addPathSegment("search")
                                .addQueryParameter("name", text)
                                .build();
                    }
                    Log.d("SubscribeActivity", "run: request_rul="+request_url);
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(request_url)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    if (responseData.equals("NOTFOUND")) {
                        Log.d("SubscribeActivity", "run: no matches");
                        showToast("No Matches!");
                    } else {
                        Log.d("SubscribeActivity", "run: searchResult"+responseData);
                        Gson gson = new Gson();
                        sourceList = gson.fromJson(responseData, new TypeToken<List<Source>>(){}.getType());
                        showResponse(true);
                    }
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
                Toast.makeText(SubscribeActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
