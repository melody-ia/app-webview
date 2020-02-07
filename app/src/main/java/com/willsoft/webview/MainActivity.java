package com.willsoft.webview;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 팝업
        webSettings.setLoadsImagesAutomatically(true); // 이미지 리소스 다운
        webSettings.setUseWideViewPort (false); // 가로모드
        webSettings.setSupportZoom (true); // 확대,축소
        webSettings.setDomStorageEnabled(false); // 로컬스토리지
        webSettings.setAppCacheEnabled (false); // 앱 캐시사용

        webSettings.setAllowFileAccessFromFileURLs(true); // 파일
        webSettings.setSaveFormData(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);


        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setUserAgentString(
        this.webView.getSettings().getUserAgentString()
                + " "
                + getString(R.string.user_agent_suffix)
        );
        webView.loadUrl( this.getResources().getString(R.string.test) );

    }

    public void onBackPressed(){

        if(webView.canGoBack()) webView.goBack();
        else finish();
    }
}
