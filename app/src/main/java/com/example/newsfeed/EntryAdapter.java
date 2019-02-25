package com.example.newsfeed;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.ninegrid.ImageInfo;
import com.lzy.ninegrid.NineGridView;
import com.lzy.ninegrid.preview.NineGridViewClickAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class EntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int ENTRY_WHITOUT_COVER = 1;
    private final int ENTRY_WITH_COVER = 2;
    private final int ENTRY_WEIBO = 3;
    private final int FOOT_VIEW = 4;

    private final boolean hasMore = true;
    private final boolean fadeTips = false;

    private List<Entry> mEntryList;
    private Context context;
    private Config config;

    public EntryAdapter (List<Entry> entryList, Context context) {
        this.mEntryList = entryList;
        this.context = context;
        this.config = new Config();
    }


    //带封面图的文章类Entry
    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        private TextView entryTitle;
        private TextView entrySource;
        private TextView entryTime;
        private TextView entryDigest;
        private ImageView entryPhoto;
        private ImageView sourcePhoto;
        private CardView entryCard;
        private EntryViewHolder(View view) {
            super(view);
            entryTitle = (TextView) view.findViewById(R.id.entry_title);
            entrySource = (TextView) view.findViewById(R.id.entry_source);
            entryTime = (TextView) view.findViewById(R.id.entry_time);
            entryDigest = (TextView) view.findViewById(R.id.entry_digest);
            entryPhoto = (ImageView) view.findViewById(R.id.entry_cover);
            sourcePhoto = (ImageView) view.findViewById(R.id.source_photo);
            entryCard = (CardView) view.findViewById(R.id.entry_card);
        }
    }

    //不带封面图的文章类Entry
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

    //微博类Entry，可以显示微博正文和九图
    public static class EntryViewHolderWeibo extends RecyclerView.ViewHolder {
        private TextView entryContent;
        private TextView entrySource;
        private TextView entryTime;
        private CardView entryCard;
        private NineGridView nineGridView;
        private EntryViewHolderWeibo(View view) {
            super(view);
            entryContent = (TextView) view.findViewById(R.id.entry_content);
            entrySource = (TextView) view.findViewById(R.id.entry_source);
            entryTime = (TextView) view.findViewById(R.id.entry_time);
            entryCard = (CardView) view.findViewById(R.id.entry_card_weibo);
            nineGridView = (NineGridView) view.findViewById(R.id.nineGrid);
        }
    }

    class FootHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        public FootHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar1);
        }
    }




    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return FOOT_VIEW;
        } else if (mEntryList.get(position).getLink().indexOf("weibo.com") != -1 || mEntryList.get(position).getLink().indexOf("weibo.cn") != -1) {
            return ENTRY_WEIBO;
        } else if (mEntryList.get(position).getPhoto() == null || mEntryList.get(position).getPhoto().equals("")
                || mEntryList.get(position).getSourceId().equals("19") || mEntryList.get(position).getSourceId().equals("15")) { //无封面图的文章
            /*TODO: 消除硬编码，不要直接用source_id做判断条件，否则删除这个source后或者重新添加时肯会出问题*/
            return ENTRY_WITH_COVER;
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
        if (viewType == FOOT_VIEW) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footview, parent, false);
            return new FootHolder(view);
        } else if (viewType == ENTRY_WEIBO) {
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


    //根据Entry的类型进行不同的数据-视图绑定
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof  FootHolder) {
          FootHolder viewHolder = (FootHolder) holder;
          viewHolder.progressBar.setIndeterminate(true);
        } else if (holder instanceof EntryViewHolder) {
            final Entry entry = mEntryList.get(position);
            EntryViewHolder viewHolder = (EntryViewHolder) holder;
            viewHolder.entryTitle.setText(entry.getTitle());
            viewHolder.entrySource.setText(entry.getSourceName());
            viewHolder.entryTime.setText(entry.geLocalPubTime());
            if (entry.getDigest() == null || entry.getDigest().length() > 0) {
                viewHolder.entryDigest.setText(entry.getDigest());
            } else {
                viewHolder.entryDigest.setText(Html2Text(entry.getContent()));
            }
            Transformation transformation = new Transformation() {

                @Override
                public Bitmap transform(Bitmap source) {

                    int targetWidth = 1200;
//                    LogCat.i("source.getHeight()="+source.getHeight()+",source.getWidth()="+source.getWidth()+",targetWidth="+targetWidth);

                    if(source.getWidth()==0){
                        return source;
                    }

                    //如果图片小于设置的宽度，则返回原图
                    if(source.getWidth()<targetWidth){
                        return source;
                    }else{
                        //如果图片大小大于等于设置的宽度，则按照设置的宽度比例来缩放
                        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                        int targetHeight = (int) (targetWidth * aspectRatio);
                        if (targetHeight != 0 && targetWidth != 0) {
                            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                            if (result != source) {
                                // Same bitmap is returned if sizes are the same
                                source.recycle();
                            }
                            return result;
                        } else {
                            return source;
                        }
                    }

                }

                @Override
                public String key() {
                    return "transformation" + " desiredWidth";
                }
            };

            if (getStoredPhotoUrl(String.valueOf(entry.getSourceId())) == "NULL") {
                GetSourcePhotoTask getSourcePhotoTask = new GetSourcePhotoTask(viewHolder.entryPhoto);
                getSourcePhotoTask.executeOnExecutor(Executors.newCachedThreadPool(), entry.getSourceId());
            } else {
                Picasso.get()
                        .load(getStoredPhotoUrl(String.valueOf(entry.getSourceId())))
                        .into(viewHolder.sourcePhoto);
            }

            if (entry.getPhoto().length() > 0)
                Picasso.get()
                        .load(entry.getPhoto())
                        .placeholder(R.color.gainsboro)
                        .transform(transformation)
                        .into(viewHolder.entryPhoto);
            else {
                viewHolder.entryPhoto.setVisibility(View.GONE);
            }

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
            final Entry entry = mEntryList.get(position);
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
            final Entry entry = mEntryList.get(position);
            EntryViewHolderWeibo viewHolder = (EntryViewHolderWeibo) holder;
//            Pattern pattern = Pattern.compile("<img.+?><br><br>");
//            Matcher matcher = pattern.matcher(entry.getContent());
//            String result = matcher.replaceAll("");
            String str1 = entry.getContent();
            String str2 = str1.replaceAll("<img.+?><br><br>", "");
            String result = str2.replaceAll("<img.+?>", "");
            viewHolder.entryContent.setText(Html2Text(entry.getContent()));
//            viewHolder.entryContent.setText(Html.fromHtml(entry.getContent().replaceAll("<img.+?>", "")));
//            viewHolder.entryContent.setText(Html.fromHtml(entry.getContent()));
            viewHolder.entrySource.setText(entry.getSourceName());
            viewHolder.entryTime.setText(entry.geLocalPubTime());


            ArrayList<ImageInfo> imageInfo = new ArrayList<>();
            List<String> imageDetails = getAttachments(entry.getContent());
            if (imageDetails != null) {
                for (String imageDetail : imageDetails) {
                    ImageInfo info = new ImageInfo();
                    info.setThumbnailUrl(imageDetail);
                    info.setBigImageUrl(imageDetail);
                    imageInfo.add(info);
                    Log.d("EntryAdapter", "onBindViewHolder: imageDetail:" + imageDetail);
                }
            }

            viewHolder.nineGridView.setAdapter(new NineGridViewClickAdapter(context, imageInfo));

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

    //从微博网页源码提取微博正文的图片列表
    public static List<String> getAttachments(String content){
        List<String> list = new ArrayList<String>();
        //目前img标签标示有3种表达式
        //<img alt="" src="1.jpg"/> <img alt="" src="1.jpg"></img> <img alt="" src="1.jpg">
        //开始匹配content中的<img />标签
        Pattern p_img = Pattern.compile("<(img|IMG)(.*?)(/>|></img>|>)");
        Matcher m_img = p_img.matcher(content);
        boolean result_img = m_img.find();
        if (result_img) {
            while (result_img) {
                //获取到匹配的<img />标签中的内容
                String str_img = m_img.group(2);
                //开始匹配<img />标签中的src
                Pattern p_src = Pattern.compile("(src|SRC)=(\"|\')(.*?)(\"|\')");
                Matcher m_src = p_src.matcher(str_img);
                if (m_src.find()) {
                    String str_src = m_src.group(3);
                    if (str_src.endsWith(".jpg"))
                        list.add(str_src);
                }
                //匹配content中是否存在下一个<img />标签，有则继续以上步骤匹配<img />标签中的src
                result_img = m_img.find();
            }
        }
        return list;
    }


    public static String Html2Text(String inputString){
        String htmlStr = inputString; //含html标签的字符串
        String textStr ="";
        java.util.regex.Pattern p_script;
        java.util.regex.Matcher m_script;
        java.util.regex.Pattern p_style;
        java.util.regex.Matcher m_style;
        java.util.regex.Pattern p_html;
        java.util.regex.Matcher m_html;
        try{
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
            String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
            p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); //过滤script标签
            p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); //过滤style标签
            p_html = Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); //过滤html标签
            textStr = htmlStr;
        }catch(Exception e){
            e.printStackTrace();
        }
        return textStr;//返回文本字符串
    }

    //判断用户是否安装了某个程序，用于判断用户是否安装了chrome浏览器
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
        return mEntryList.size() + 1;
    }

    public void resetList() {
        mEntryList = new ArrayList<>();
    }

    public void updateList(List<Entry> newEntries) {
        if (newEntries != null) {
            mEntryList.addAll(newEntries);
        }
        notifyDataSetChanged();
    }

    public int getLastId() {
        return mEntryList.get(mEntryList.size()-1).getId();
    }

    public String getLastTime() {
        return mEntryList.get(mEntryList.size()-1).getTime();
    }

    public void storeSourcePhoto(String source_id, String photo_url) {
        SharedPreferences sp = context.getSharedPreferences("SourcePhotoUrl", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(source_id,"");    // 先清空原始数据
        editor.putString(source_id, photo_url);
        editor.commit();
    }

    public String getStoredPhotoUrl(String source_id) {
        SharedPreferences sp = context.getSharedPreferences("SourcePhotoUrl", MODE_PRIVATE);
        String result = sp.getString(source_id, "NULL");
        return result;
    }

    class GetSourcePhotoTask extends AsyncTask<String, Void, String> {
        private ImageView sourcePhoto;
        String source_id;
        public GetSourcePhotoTask(ImageView imageView) {
            sourcePhoto = imageView;
        }

        @Override
        protected String doInBackground(String... params) {
            source_id = params[0];
            try {
                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/sources/value?field=photo")
                        .addHeader("source_id", source_id)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                String result = jsonObject.getString("photo");
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            storeSourcePhoto(source_id, result);
            if (result.length() > 0) {
                Picasso.get()
                        .load(result)
                        .into(sourcePhoto);
            }
        }
    }
}
