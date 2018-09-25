package com.example.newsfeed;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {
    private List<Entry> mEntryList;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView entryTitle;
        public ViewHolder(View view) {
            super(view);
            entryTitle = (TextView) view.findViewById(R.id.entry_name);
        }
    }

    public EntryAdapter (List<Entry> entryList, Context context) {
        this.mEntryList = entryList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Entry entry = mEntryList.get(position);

        holder.entryTitle.setText(entry.getTitle());

        holder.entryTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = entry.getLink();
                PackageManager pm = context.getPackageManager();
                boolean isChromeInstalled = isPackageInstalled("com.android.chrome", pm);
                if (isChromeInstalled) {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_share);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "http://www.codepath.com");
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
