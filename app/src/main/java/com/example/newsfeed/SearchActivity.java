package com.example.newsfeed;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchBar;
import com.lapism.searchview.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class SearchActivity extends AppCompatActivity {
    private List<Source> sourceList;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private SourceAdapter adapter;
    private Config config;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        config = new Config();

        Intent intent = getIntent();
        query = intent.getStringExtra("query");
        Log.d(TAG, "onCreate: query:" + query);


        setSearchView();


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        sourceList = new ArrayList<>();
        getSearchResult(query);



    }

    void setSearchView() {
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new Search.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(CharSequence query) {
                getSearchResult((String) query);
                return true;
            }

            @Override
            public void onQueryTextChange(CharSequence newText) {

            }
        });
        searchView.setQuery(query, false);
    }

    //获取搜索结果列表
    private void getSearchResult(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onCreate: SearchActivity getSearchResult");
                    HttpUrl request_url;
                    if(text.startsWith("http") || text.startsWith("https")) {
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
                    Log.d(TAG, "run: search responseData: " + responseData);
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
                Toast.makeText(SearchActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
