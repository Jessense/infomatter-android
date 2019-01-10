package com.example.newsfeed;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class Search extends Activity {
    private List<Source> sourceList;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private SourceAdapter adapter;
    private Config config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        config = new Config();

        recyclerView = (RecyclerView) findViewById(R.id.source_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        sourceList = new ArrayList<>();
//        getSourceList();

        handleIntent(getIntent());
        Log.d(TAG, "onCreate: SearchActivity onCreate");

//        searchView = (SearchView) findViewById(R.id.searchview);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                getSearchResult(query);
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
////                mAdapter.getFilter().filter(newText);
//                return false;
//            }
//        });


    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getSearchResult(query);
        }
    }


    //获取内容源列表
    private void getSourceList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onCreate: SearchActivity getSourceList");
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

    //获取搜索结果列表
    private void getSearchResult(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onCreate: SearchActivity getSearchResult");
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
                    } else if (text.startsWith("///")) {
                        request_url = new HttpUrl.Builder()
                                .scheme(config.getScheme())
                                .host(config.getHost())
                                .port(config.getPort())
                                .addPathSegment("sources")
                                .addPathSegment("search")
                                .addQueryParameter("feedUrl", config.getScheme() + "://" + config.getHost() + ":" + config.getPub_rsshub_port() + "/" + text.substring(3))
                                .build();
                        Log.d("SubscribeActivity", "run: search text"+config.getScheme() + "://" + config.getHost() + ":" + config.getPub_rsshub_port() + "/" + text.substring(3));
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
                Log.d(TAG, "onCreate: SearchActivity showResponse");
                adapter = new SourceAdapter(sourceList, isSearching, getApplicationContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void showToast (final String toastText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Search.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
