package com.example.newsfeed;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SubscribeActivity extends AppCompatActivity {
    List<Source> sourceList;
    RecyclerView recyclerView;
    SourceAdapter adapter;
    EditText searchEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

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
                            .url("http://188.131.178.76:3000/sources")
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
                    if(text.startsWith("http")) {
                        request_url = new HttpUrl.Builder()
                                .scheme("http")
                                .host("188.131.178.76")
                                .port(3000)
                                .addPathSegment("sources")
                                .addPathSegment("search")
                                .addQueryParameter("feedUrl", text)
                                .build();
                    } else {
                        request_url = new HttpUrl.Builder()
                                .scheme("http")
                                .host("188.131.178.76")
                                .port(3000)
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
                    Log.d("SubscribeActivity", "run: searchResult"+responseData);
                    Gson gson = new Gson();
                    sourceList = gson.fromJson(responseData, new TypeToken<List<Source>>(){}.getType());
                    showResponse(true);
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
}
