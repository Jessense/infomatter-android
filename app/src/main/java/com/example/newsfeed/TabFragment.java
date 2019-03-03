package com.example.newsfeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class TabFragment extends Fragment {
    private List<Source> sourceList;
    private SourceAdapter sourceAdapter;
    private RecyclerView recyclerView;
    private Config config;


    public static Fragment newInstance(String type) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putString("category", type);
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        config = new Config();
        String category = getArguments().getString("category");
        View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getSourceList(category);
        return rootView;
    }

    //获取内容源列表
    private void getSourceList(final String category) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Log.d(TAG, "run: category: " + category);
                    Request request = new Request.Builder()
                            .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/sources/discovery?category=" + category)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d(TAG, "run: getSouceList resposeData: " + responseData);
                    Gson gson = new Gson();
                    sourceList = gson.fromJson(responseData, new TypeToken<List<Source>>(){}.getType());
                    recyclerView.setAdapter(new SourceAdapter(sourceList, false, getActivity()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
