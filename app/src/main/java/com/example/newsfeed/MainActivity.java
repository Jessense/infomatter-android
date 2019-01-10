package com.example.newsfeed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.ninegrid.NineGridView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private List<Entry> entryList;
    private RecyclerView recyclerView;
    private EntryAdapter adapter;
    private User user;
    private Config config;
    private SwipeRefreshLayout swipeRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        config = new Config();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NineGridView.setImageLoader(new PicassoImageLoader());

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        user = new User(getApplicationContext());
        if (!user.getLogined()){ //如果用户未登录，则进行登录
            Log.d("MainActivity", "onCreate: name: "+user.getName());
            Log.d("MainActivity", "onCreate: logined?"+user.getLogined().toString());
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            Log.d("MainActivity", "onCreate: Prepare to Start Login");
            startActivityForResult(intent, 1);
            Log.d("MainActivity", "onCreate: After Login");
            Log.d("MainActivity", "onCreate: "+user.getName());
        } else { //如果用户已登录，则进入主页显示时间线
            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            entryList = new ArrayList<>();
            getEntryList();
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getEntryList();
            }
        });

    }

    //登录结果返回后完善当前用户的信息，如用户id
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
                    Log.d("MainActivity", "name: "+user.getName()+",id:"+user.getId() + user.getLogined().toString());
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

    //获取Entry列表
    private void getEntryList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/timeline")
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
                swipeRefresh.setRefreshing(false);
            }
        }).start();
    }


    //显示Entry列表
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。
        return true; // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
    }

    //菜单的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_search:
                Intent intent = new Intent(MainActivity.this, SubscribeActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                user.setLogined(false);
                finish();
                break;
            default:
                break;
        }

        return true;
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favourites) {
            // Handle the camera action
        } else if (id == R.id.nav_following) {
            Intent inent = new Intent(MainActivity.this, FollowingActivity.class);
            startActivity(inent);

        } else if (id == R.id.nav_discovery) {
//            Intent intent = new Intent(MainActivity.this, TestActivity.class);
//            startActivity(intent);

        } else if (id == R.id.nav_feedback) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {
            user.setLogined(false);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Picasso 加载图片
    private class PicassoImageLoader implements NineGridView.ImageLoader {

        @Override
        public void onDisplayImage(Context context, ImageView imageView, String url) {
            Picasso.get().load(url)//
                    .placeholder(R.drawable.ic_default_color)//
                    .error(R.drawable.ic_default_color)//
                    .into(imageView);
        }

        @Override
        public Bitmap getCacheImage(String url) {
            return null;
        }
    }

}
