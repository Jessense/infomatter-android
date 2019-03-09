package com.example.newsfeed;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class WebviewActivity extends AppCompatActivity {
    private Menu menu;
    private MenuItem starBtn;
    private Config config;
    private User user;
    private String entry_id;
    private String title;
    private String url;
    private Boolean starred;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        user = new User(this);
        config = new Config();

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        String html = intent.getStringExtra("html");
        entry_id = intent.getStringExtra("entry_id");
        title = intent.getStringExtra("title");

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle(title);


        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setWebViewClient(new WebViewClient());
        if (html != null) {
            String source_name = intent.getStringExtra("source_name");
            String time = intent.getStringExtra("time");

            String header = "<h2>" + "<a href=\"" + url + "\" style=\"color:#000000\">" + title + "</a>" + "</h2>" + "<i>" + source_name + " / " + time + "</i><p>";
            String css = "<style>p{font-size :16px !important;line-height:30px !important}</style><style>a{color:#4285F4; text-decoration:none}</style><body style=\"margin: 0; padding: 20\"><style>img{max-width: 100%; width:auto; height: auto;}</style>";

            webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

            webView.loadData(header + html + css, "text/html", "UTF-8");
        } else {
            webView.loadUrl(url);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        this.menu = menu;
        starBtn = menu.findItem(R.id.star);

        GetStarRelationTask getStarRelationTask = new GetStarRelationTask(menu.findItem(R.id.star));
        getStarRelationTask.executeOnExecutor(Executors.newCachedThreadPool(), entry_id);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.star:
                if (starred) {
                    UnstarTask unstarTask = new UnstarTask(item);
                    unstarTask.executeOnExecutor(Executors.newCachedThreadPool(), entry_id);
                } else {
                    StarTask starTask = new StarTask(item);
                    starTask.executeOnExecutor(Executors.newCachedThreadPool(), entry_id);
                }

                break;
            case R.id.open_in_browser:
                Intent browser_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browser_intent);
                break;
            case R.id.share:
                Intent share_intent = new Intent(Intent.ACTION_SEND);
                share_intent.setType("text/plain");
                share_intent.putExtra(Intent.EXTRA_TEXT, title + "\n" + url);
                share_intent.putExtra(Intent.EXTRA_SUBJECT, title);
                startActivity(share_intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class GetStarRelationTask extends AsyncTask<String, Void, Boolean> {
        private MenuItem menuItem;
        String entry_id;
        public GetStarRelationTask(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            entry_id = params[0];
            try {
                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/isstarring")
                        .addHeader("user_id", user.getId())
                        .addHeader("entry_id", entry_id)
                        .build();
                Log.d(TAG, "doInBackground: GetStarsActivity: " + entry_id);
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Log.d(TAG, "doInBackground: IsStarring: " + responseData);
                if (responseData.equals("true")) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: MenuItemTitle: " + menuItem.getTitle());
            if (result) {
                menuItem.setIcon(R.drawable.ic_bookmark_border_blue_24dp);
                starred = true;
            } else {
                menuItem.setIcon(R.drawable.ic_bookmark_border_gray_24dp);
                starred = false;
            }

        }
    }

    class StarTask extends AsyncTask<String, Void, Boolean> {
        private MenuItem menuItem;
        String entry_id;
        public StarTask(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            entry_id = params[0];
            try {
                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/star")
                        .addHeader("user_id", user.getId())
                        .addHeader("entry_id", entry_id)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Log.d(TAG, "doInBackground: star: " + responseData);
                if (responseData.equals("SUCCESS")) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                menuItem.setIcon(R.drawable.ic_bookmark_border_blue_24dp);
                starred = true;
                Toast.makeText(getApplicationContext(), "Starred", Toast.LENGTH_LONG).show();
            } else {
                menuItem.setIcon(R.drawable.ic_bookmark_border_gray_24dp);
                starred = false;
                Toast.makeText(getApplicationContext(), "Failed to star it", Toast.LENGTH_LONG).show();
            }

        }
    }

    class UnstarTask extends AsyncTask<String, Void, Boolean> {
        private MenuItem menuItem;
        String entry_id;
        public UnstarTask(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            entry_id = params[0];
            try {
                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/unstar")
                        .addHeader("user_id", user.getId())
                        .addHeader("entry_id", entry_id)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Log.d(TAG, "doInBackground: unstar: " + responseData);
                if (responseData.equals("SUCCESS")) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                menuItem.setIcon(R.drawable.ic_bookmark_border_gray_24dp);
                starred = false;
                Toast.makeText(getApplicationContext(), "Unstarred", Toast.LENGTH_LONG).show();
            } else {
                menuItem.setIcon(R.drawable.ic_bookmark_border_blue_24dp);
                starred = true;
                Toast.makeText(getApplicationContext(), "Failed to unstar it", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
}
