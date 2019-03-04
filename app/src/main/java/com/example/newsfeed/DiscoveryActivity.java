package com.example.newsfeed;

import android.app.Service;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
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

        ViewPager viewPager = (ViewPager) findViewById(R.id.tab_viewpager);
        viewPager.setOffscreenPageLimit(15);
        MyViewPagerAdapter viewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
//        viewPagerAdapter.addFragment(TabFragment.newInstance("0"), "推荐");
        viewPagerAdapter.addFragment(TabFragment.newInstance("1"), "科技");
        viewPagerAdapter.addFragment(TabFragment.newInstance("2"), "技术");
        viewPagerAdapter.addFragment(TabFragment.newInstance("9"), "大学");
        viewPagerAdapter.addFragment(TabFragment.newInstance("3"), "财经");
        viewPagerAdapter.addFragment(TabFragment.newInstance("5"), "好奇心");
        viewPagerAdapter.addFragment(TabFragment.newInstance("4"), "公众号");
        viewPagerAdapter.addFragment(TabFragment.newInstance("E"), "社交媒体");
        viewPagerAdapter.addFragment(TabFragment.newInstance("6"), "美");
        viewPagerAdapter.addFragment(TabFragment.newInstance("C"), "生活");
        viewPagerAdapter.addFragment(TabFragment.newInstance("7"), "娱乐");
        viewPagerAdapter.addFragment(TabFragment.newInstance("A"), "体育");
        viewPagerAdapter.addFragment(TabFragment.newInstance("B"), "搞笑");
        viewPagerAdapter.addFragment(TabFragment.newInstance("Z"), "其他");

        viewPager.setAdapter(viewPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
//        tabLayout.addTab(tabLayout.newTab().setText("推荐"));
        tabLayout.addTab(tabLayout.newTab().setText("科技"));
        tabLayout.addTab(tabLayout.newTab().setText("技术"));
        tabLayout.addTab(tabLayout.newTab().setText("大学"));
        tabLayout.addTab(tabLayout.newTab().setText("财经"));
        tabLayout.addTab(tabLayout.newTab().setText("好奇心"));
        tabLayout.addTab(tabLayout.newTab().setText("公众号"));
        tabLayout.addTab(tabLayout.newTab().setText("社交媒体"));
        tabLayout.addTab(tabLayout.newTab().setText("美"));
        tabLayout.addTab(tabLayout.newTab().setText("生活"));
        tabLayout.addTab(tabLayout.newTab().setText("娱乐"));
        tabLayout.addTab(tabLayout.newTab().setText("体育"));
        tabLayout.addTab(tabLayout.newTab().setText("搞笑"));
        tabLayout.addTab(tabLayout.newTab().setText("其他"));

        tabLayout.setupWithViewPager(viewPager);


//        recyclerView = (RecyclerView) findViewById(R.id.source_list);
//        recyclerView.setHasFixedSize(true);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        sourceList = new ArrayList<>();
//        getSourceList();
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
