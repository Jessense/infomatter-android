package com.example.newsfeed;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchItem;
import com.lzy.ninegrid.NineGridView;
import com.lapism.searchview.widget.SearchView;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private SearchView searchView;
    private Menu menu;
    private Spinner spinner;
    private String[] groups_default;
    private int lastVisibleItem = 0;
    private int last_id = 1000000;
    private String last_time = "9999-12-31 23:59:59";
    private int batch_size = 15;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        config = new Config();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NineGridView.setImageLoader(new PicassoImageLoader());

        setSearchView();

        groups_default = new String[] {"All", "Recommendation", "Popular"};

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
            View navHeader = navigationView.getHeaderView(0);
            TextView userName = navHeader.findViewById(R.id.user_name);
            userName.setText(user.getName());

            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (adapter.isHasMore() == true && ((lastVisibleItem == adapter.getItemCount() - 5) || (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == adapter.getItemCount() - 1))) {
                        last_time = adapter.getLastTime();
                        last_id = adapter.getLastId();
                        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                        TemporalAccessor accessor = timeFormatter.parse(last_time);
                        Date pubDate = Date.from(Instant.from(accessor));
                        LocalDateTime localPubDate = pubDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        String temp = localPubDate.toString();
                        last_time = temp.substring(0, 10) + " " + temp.substring(11, 16);
                        if (temp.length() < 19) {
                            last_time += ":00";
                        } else {
                            last_time += temp.substring(16, 19);
                        }
                        Log.d("MainActivity", "onScrollStateChanged: localPubDate" + localPubDate);
                        Log.d("MainActivity", "onScrollStateChanged: last_time=" + last_time);
                        Log.d("MainActivity", "onScrollStateChanged: last_id=" + last_id);
                        getEntryList();
                    }

                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                }
            });
            entryList = new ArrayList<>();
            getEntryList1();
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.resetList();
                entryList = new ArrayList<>();
                last_id = 0;
                last_time = "9999-12-31 23:59:59";
                getEntryList();
            }
        });

    }


    void setSearchView() {
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new Search.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(CharSequence query) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                searchView.close();
                return true;
            }

            @Override
            public void onQueryTextChange(CharSequence newText) {

            }
        });

        searchView.setOnOpenCloseListener(new Search.OnOpenCloseListener() {
            @Override
            public void onOpen() {

            }

            @Override
            public void onClose() {

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
                    user.setGroups2(data.getStringExtra("groups2"));


                    //update spinner
                    List<String> list = new ArrayList(Arrays.asList(groups_default));
                    list.addAll(Arrays.asList(user.getGroups2()));
                    String[] groups = list.toArray(new String[0]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (this, android.R.layout.simple_spinner_item, groups);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    View navHeader = navigationView.getHeaderView(0);
                    TextView userName = navHeader.findViewById(R.id.user_name);
                    userName.setText(user.getName());

                    Log.d("MainActivity", "name: "+user.getName()+",id:"+user.getId() + user.getLogined().toString());
                    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                    recyclerView.setHasFixedSize(true);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(layoutManager);
                    entryList = new ArrayList<>();
                    getEntryList1();
                }
                break;
             default:
        }
    }

    private void getEntryList1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Log.d("MainActivity", "getEntryList1: last_time=" + last_time);
                    Log.d("MainActivity", "getEntryList1: last_id=" + last_id);
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/timeline_batch")
                            .addHeader("user_id", user.getId())
                            .addHeader("last_time", last_time)
                            .addHeader("last_id", String.valueOf(last_id))
                            .addHeader("batch_size", String.valueOf(batch_size))
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("MainActivity", "run: getEntryList1:" + responseData);
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

    //获取Entry列表
    private void getEntryList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Log.d("MainActivity", "getEntryList1: last_time=" + last_time);
                    Log.d("MainActivity", "getEntryList1: last_id=" + last_id);
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/timeline_batch")
                            .addHeader("user_id", user.getId())
                            .addHeader("last_time", last_time)
                            .addHeader("last_id", String.valueOf(last_id))
                            .addHeader("batch_size", String.valueOf(batch_size))
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    List<Entry> newData = gson.fromJson(responseData, new TypeToken<List<Entry>>(){}.getType());
                    adapter.updateList(newData);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                searchView.open(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。
        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item);

        //update spinner
        List<String> list = new ArrayList(Arrays.asList(groups_default));
        list.addAll(Arrays.asList(user.getGroups2()));
        String[] groups = list.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        this.menu = menu;
        return true;
    }

    public void updateSpinner() {
        getMenuInflater().inflate(R.menu.main,menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);


        String[] groups_default = new String[] {"All", "Recommendation", "Popular"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, groups_default);

//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favourites) {
            Intent inent = new Intent(MainActivity.this, StarsActivity.class);
            startActivity(inent);
        } else if (id == R.id.nav_following) {
            Intent inent = new Intent(MainActivity.this, FollowingActivity.class);
            startActivity(inent);

        } else if (id == R.id.nav_discovery) {
            Intent intent = new Intent(MainActivity.this, DiscoveryActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_feedback) {
            Intent inent = new Intent(MainActivity.this, FeedbackActivity.class);
            startActivity(inent);
        } else if (id == R.id.nav_settings) {
            Intent inent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(inent);
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
