package com.example.newsfeed;

import android.content.Context;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GroupActivity extends AppCompatActivity {

    private List<Group> groupList;
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private User user;
    private Config config;
    private EditText add_group_edt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);


        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit group");

        config = new Config();
        user = new User(getApplicationContext());

        add_group_edt = (EditText) findViewById(R.id.add_group_edt);
        ImageView add_group_submit = (ImageView) findViewById(R.id.add_group_submit);
        add_group_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (add_group_edt.getText().toString().length() > 0) {
                    addGroup(add_group_edt.getText().toString());
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_group);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        groupList = new ArrayList<>();
        getGroupList();
    }


    private void addGroup(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/add_group?group=" + s)
                            .addHeader("user_id", user.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("GroupActivity", "run: add group:" + responseData);
                    if (responseData.equals("SUCCESS")) {
                        resetEdt();
                        user.addGroups2(s);
                        groupList.add(new Group(s));
                        showResponse();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getGroupList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/get_group")
                            .addHeader("user_id", user.getId())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("GroupActivity", "run: responseData:" + responseData);
                    Gson gson = new Gson();
                    List<ResponseGroup> responseGroups = gson.fromJson(responseData, new TypeToken<List<ResponseGroup>>(){}.getType());
                    if (responseGroups.size() > 0) {
                        String groupsString = responseGroups.get(0).getGroups2();
                        user.setGroups2(groupsString);
                        String[] groupsArray = user.getGroups2();
                        groupList.clear();
                        for (String groupString:groupsArray) {
                            if (groupString.length() > 0)
                                groupList.add(new Group(groupString));
                            Log.d("GroupActivity", "run: groupList" + groupList.get(0).getGroupName());
                        }
                    }
                    showResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showResponse() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new GroupAdapter(groupList, getApplicationContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void resetEdt() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                add_group_edt.setText("");
                add_group_edt.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(add_group_edt.getWindowToken(), 0);
            }
        });
    }

    public class ResponseGroup {
        private String groups2;

        public String getGroups2() {
            return groups2;
        }
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
