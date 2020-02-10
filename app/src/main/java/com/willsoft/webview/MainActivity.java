package com.willsoft.webview;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;

import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.iid.FirebaseInstanceId;
import com.willsoft.webview.FingerPrint.FingerPrintDialogActivity;
import com.willsoft.webview.QrScan.QrScannerActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


public class MainActivity extends AppCompatActivity implements MainToJavaScriptInterfaceData {

    private WebView webView;
    private static final int REQUEST_FINGER_PRINT_CODE = 999;
    private static final int REQUEST_QR_SCANNER_CODE = 888;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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

        webView.addJavascriptInterface(new JavaScriptInterface(this), "App");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setUserAgentString(
        this.webView.getSettings().getUserAgentString()
                + " "
                + getString(R.string.user_agent_suffix)
        );
        webView.loadUrl( this.getResources().getString(R.string.test) );
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {  // alert
                new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme).setTitle(R.string.alert).setMessage("\n\t" + message).setIcon(R.drawable.ic_priority_high_black_24dp)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        }).setCancelable(false).create().show();
                return true;
            }
        });

        try {
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d("IDService","device token : "+token);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }



    public void onBackPressed(){

        if(webView.canGoBack()) webView.goBack();
        else finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FINGER_PRINT_CODE) {  // finger print
                String getFingerPrintResult = "OK";
                Log.d("TAG", "getFingerPrintResult: " + getFingerPrintResult);
                webView.loadUrl("javascript:getFingerPrintResult('" + getFingerPrintResult + "')");
            }

            if (requestCode == REQUEST_QR_SCANNER_CODE) {  // qr scanner
                String getQrScannerResult = data.getStringExtra("QrScannerResult");
                Log.d("TAG", "QrScannerResult: " + getQrScannerResult);
                webView.loadUrl("javascript:getQrScannerResult('" + getQrScannerResult + "')");
            }
        }
    }

    @Override
    public void moveToFingerPrintDialogActivity(String userPwd, boolean check) {
        Intent intent = new Intent(this, FingerPrintDialogActivity.class);
        intent.putExtra("userPwd", userPwd);
        intent.putExtra("check", check);
        startActivityForResult(intent, REQUEST_FINGER_PRINT_CODE);
    }

    @Override
    public void moveToQrScannerActivity() {  // 퍼미션 여부

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startActivityForResult(new Intent(MainActivity.this, QrScannerActivity.class), REQUEST_QR_SCANNER_CODE);  // 수락 후 QrScannerActivity 로 이동
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

}
