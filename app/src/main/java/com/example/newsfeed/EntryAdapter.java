package com.example.newsfeed;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int ENTRY_WHITOUT_COVER = 1;
    private final int ENTRY_WITH_COVER = 2;
    private final int ENTRY_WEIBO = 3;

    private List<Entry> mEntryList;
    private Context context;

    public EntryAdapter (List<Entry> entryList, Context context) {
        this.mEntryList = entryList;
        this.context = context;
    }


    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        private TextView entryTitle;
        private TextView entrySourceTime;
        private ImageView entryPhoto;
        private CardView entryCard;
        private EntryViewHolder(View view) {
            super(view);
            entryTitle = (TextView) view.findViewById(R.id.entry_name);
            entrySourceTime = (TextView) view.findViewById(R.id.entry_source_time);
            entryPhoto = (ImageView) view.findViewById(R.id.entry_cover);
            entryCard = (CardView) view.findViewById(R.id.entry_card);
        }
    }

    public static class EntryViewHolderWithoutCover extends RecyclerView.ViewHolder {
        private TextView entryTitle;
        private TextView entrySourceTime;
        private CardView entryCard;
        private EntryViewHolderWithoutCover(View view) {
            super(view);
            entryTitle = (TextView) view.findViewById(R.id.entry_name_without_cover);
            entrySourceTime = (TextView) view.findViewById(R.id.entry_source_time_without_cover);
            entryCard = (CardView) view.findViewById(R.id.entry_card_without_cover);
        }
    }

    public static class EntryViewHolderWeibo extends RecyclerView.ViewHolder {
        private TextView entryTitle;
        private TextView entrySourceTime;
        private CardView entryCard;
        private EntryViewHolderWeibo(View view) {
            super(view);
            entryTitle = (TextView) view.findViewById(R.id.entry_name_weibo);
            entrySourceTime = (TextView) view.findViewById(R.id.entry_source_time_weibo);
            entryCard = (CardView) view.findViewById(R.id.entry_card_weibo);
        }
    }



    @Override
    public int getItemViewType(int position) {
        if (mEntryList.get(position).getLink().startsWith("https://www.weibo.com")) {
            return ENTRY_WEIBO;
        } else if (mEntryList.get(position).getPhoto() == null || mEntryList.get(position).getPhoto().equals("")
                || mEntryList.get(position).getSourceId().equals("19") || mEntryList.get(position).getSourceId().equals("15")) { //无封面图的文章
            /*TODO: 消除硬编码，不要直接用source_id做判断条件，否则删除这个source后或者重新添加时肯会出问题*/
            return ENTRY_WHITOUT_COVER;
        } else {
            return ENTRY_WITH_COVER; //有封面图的文章
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.entry, parent, false);
//        SourceAdapter.ViewHolder holder = new SourceAdapter.ViewHolder(view);
        View view;
        if (viewType == ENTRY_WEIBO) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.entry_weibo, parent, false);
            return new EntryViewHolderWeibo(view); //TODO
        } else if (viewType == ENTRY_WHITOUT_COVER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.entry_without_cover, parent, false);
            return new EntryViewHolderWithoutCover(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.entry, parent, false);
            return new EntryViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Entry entry = mEntryList.get(position);

        if (holder instanceof EntryViewHolder) {
            EntryViewHolder viewHolder = (EntryViewHolder) holder;
            viewHolder.entryTitle.setText(entry.getTitle());
            viewHolder.entrySourceTime.setText(entry.getSourceName() + " / " + entry.geLocalPubTime());
            Picasso.get()
                    .load(entry.getPhoto())
                    .into(viewHolder.entryPhoto);

            ((EntryViewHolder) holder).entryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = entry.getLink();
                    PackageManager pm = context.getPackageManager();
                    boolean isChromeInstalled = isPackageInstalled("com.android.chrome", pm);
                    if (isChromeInstalled) {
                        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_share);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, entry.getLink());
                        intent.putExtra(Intent.EXTRA_SUBJECT, entry.getTitle());
                        int requestCode = 100;

                        PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(),
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setActionButton(bitmap, "Share Link", pendingIntent, true);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                    } else {
                        Intent intent = new Intent(view.getContext(), WebviewActivity.class);
                        intent.putExtra("extra_data",url);
                        view.getContext().startActivity(intent);
                    }
                }
            });

        } else if (holder instanceof EntryViewHolderWithoutCover) {
            EntryViewHolderWithoutCover viewHolder = (EntryViewHolderWithoutCover) holder;
            viewHolder.entryTitle.setText(entry.getTitle());
            viewHolder.entrySourceTime.setText(entry.getSourceName() + " / " + entry.geLocalPubTime());

            ((EntryViewHolderWithoutCover) holder).entryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = entry.getLink();
                    PackageManager pm = context.getPackageManager();
                    boolean isChromeInstalled = isPackageInstalled("com.android.chrome", pm);
                    if (isChromeInstalled) {
                        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_share);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, entry.getLink());
                        intent.putExtra(Intent.EXTRA_SUBJECT, entry.getTitle());
                        int requestCode = 100;

                        PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(),
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setActionButton(bitmap, "Share Link", pendingIntent, true);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                    } else {
                        Intent intent = new Intent(view.getContext(), WebviewActivity.class);
                        intent.putExtra("extra_data",url);
                        view.getContext().startActivity(intent);
                    }
                }
            });
        } else if (holder instanceof EntryViewHolderWeibo){
            EntryViewHolderWeibo viewHolder = (EntryViewHolderWeibo) holder;
            viewHolder.entryTitle.setText(entry.getTitle());
            viewHolder.entrySourceTime.setText(entry.getSourceName() + " / " + entry.geLocalPubTime());

            ((EntryViewHolderWeibo) holder).entryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = entry.getLink();
                    PackageManager pm = context.getPackageManager();
                    boolean isChromeInstalled = isPackageInstalled("com.android.chrome", pm);
                    if (isChromeInstalled) {
                        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_share);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, entry.getLink());
                        intent.putExtra(Intent.EXTRA_SUBJECT, entry.getTitle());
                        int requestCode = 100;

                        PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(),
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setActionButton(bitmap, "Share Link", pendingIntent, true);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                    } else {
                        Intent intent = new Intent(view.getContext(), WebviewActivity.class);
                        intent.putExtra("extra_data",url);
                        view.getContext().startActivity(intent);
                    }
                }
            });
        }


    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return mEntryList.size();
    }
}
