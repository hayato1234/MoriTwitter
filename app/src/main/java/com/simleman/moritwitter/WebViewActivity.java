package com.simleman.moritwitter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by hayatomoritani on 10/6/16.
 */
public class WebViewActivity extends AppCompatActivity {

    WebView webView;

    public static String EXTRA_URL = "extra_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.auth_dialog);
        setTitle("Login");

        final String url = this.getIntent().getStringExtra(EXTRA_URL);
        if (null == url) {
            Log.e("Twitter", "URL cannot be null");
            finish();
        }

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                //if( url.contains(MainActivity.TWITTER_CALLBACK_URL)){
                    Uri uri = Uri.parse(url);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("KEY_URI", uri.toString());
                    setResult(RESULT_OK, resultIntent);

                /* closing webview */
                    finish();
                    return true;
                //}
                //return false;
            }

        });
        webView.loadUrl(url);
    }
}
