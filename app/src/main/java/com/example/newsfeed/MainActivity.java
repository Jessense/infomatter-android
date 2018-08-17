package com.example.newsfeed;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity{
    List<Entry> entryList;
    RecyclerView recyclerView;
    EntryAdapter adapter;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (user == null)
            user = new User();
        if (!user.getLogined()){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            Log.d("MainActivity", "onCreate: Prepare to Start Login");
            startActivityForResult(intent, 1);
//            Log.d("MainActivity", "onCreate: After Login");
//            Log.d("MainActivity", "onCreate: "+user.getName());
        } else {
            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            entryList = new ArrayList<>();
            getEntryList();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Log.d("MainActivity", "onActivityResult: RESULT_OK");
                    user.setName(data.getStringExtra("name"));
                    user.setPassword(data.getStringExtra("password"));
                    user.setId(data.getStringExtra("id"));
                    user.setLogined(true);
                    Log.d("MainActivity", "name: "+user.getName()+",id:"+user.getId());
                    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                    recyclerView.setHasFixedSize(true);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(layoutManager);
                    entryList = new ArrayList<>();
                    getEntryList();
                }
                break;
             default:
        }
    }

    private void getEntryList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://188.131.178.76:3000/users/timeline")
                            .addHeader("user_id", user.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    entryList = gson.fromJson(responseData, new TypeToken<List<Entry>>(){}.getType());
                    showResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
