package com.example.newsfeed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
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
        public TextView entryView;
        public ViewHolder(View view) {
            super(view);
            entryView = (TextView) view.findViewById(R.id.entry_name);
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

        holder.entryView.setText(entry.getTitle());

        holder.entryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = entry.getLink();

                Intent intent = new Intent(view.getContext(), WebviewActivity.class);
                intent.putExtra("extra_data",url);
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mEntryList.size();
    }
}
