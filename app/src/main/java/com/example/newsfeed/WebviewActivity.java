package com.example.newsfeed;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String html = intent.getStringExtra("html");

        WebView webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        if (url != null) {
            webView.loadUrl(url);
        } else if (html != null) {
            String title = intent.getStringExtra("title");
            String source_name = intent.getStringExtra("source_name");
            String time = intent.getStringExtra("time");

            String header = "<h2>" + title + "</h2>" + "<i>" + source_name + " / " + time + "</i><p>";
            String css = "<style>p{font-size :17px !important;line-height:30px !important}</style><style>a{color:#4285F4; text-decoration:none}</style>";

            webView.setVerticalScrollBarEnabled(false);

            WebSettings webSettings = webView.getSettings();//获取webview设置属性
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//把html中的内容放大webview等宽的一列中
            webSettings.setJavaScriptEnabled(true);//支持js


            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) webView.getLayoutParams();
            p.leftMargin = 20;
            p.rightMargin = 20;
            webView.setLayoutParams(p);

            webView.loadData(header + getNewContent(html) + css, "text/html", "UTF-8");
        }

    }

    public static String getNewContent(String htmltext){
        try {
            Document doc= Jsoup.parse(htmltext);
            Elements elements=doc.getElementsByTag("img");
            for (Element element : elements) {
                element.attr("width","100%").attr("height","auto");
            }

            return doc.toString();
        } catch (Exception e) {
            return htmltext;
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
