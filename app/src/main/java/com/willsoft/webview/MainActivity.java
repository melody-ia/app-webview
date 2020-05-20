package com.willsoft.webview;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.willsoft.webview.FingerPrint.FingerPrintDialogActivity;
import com.willsoft.webview.QrScan.QrScannerActivity;


import com.willsoft.webview.Retrofit.RetrofitConnection;
import com.willsoft.webview.Retrofit.retrofitData;
import com.willsoft.webview.SharedPreferences.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
        webSettings.setUseWideViewPort(false); // 가로모드
        webSettings.setSupportZoom(true); // 확대,축소
        webSettings.setDomStorageEnabled(false); // 로컬스토리지
        webSettings.setAppCacheEnabled(false); // 앱 캐시사용
        webSettings.setAllowFileAccessFromFileURLs(true); // 파일
        webSettings.setSaveFormData(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webView.addJavascriptInterface(new JavaScriptInterface(this), "App");
        webView.getSettings().setUserAgentString(
                this.webView.getSettings().getUserAgentString()
                        + " "
                        + getString(R.string.user_agent_suffix)
        );
        webView.loadUrl(this.getResources().getString(R.string.test));
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

        handleDeepLink();
    }

    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
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
    public void moveToFingerPrintDialogActivity(boolean check) {
        Intent intent = new Intent(this, FingerPrintDialogActivity.class);
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

    @Override
    public void setFcmToken(String userId) {
        Log.d("TAG", "setFcmToken: " + userId);
        sendFcmTokenToServer(userId);
    }

    @Override
    public void setMoveToWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void setShareData(String toWallet) { // 공유하기

        Task<ShortDynamicLink> shortDynamicLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://test.mouwallet.com/wallet/send.php?to_wallet=" + toWallet))
                .setDomainUriPrefix("https://willsoft.page.link/")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.willsoft.webview").build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();

                            Log.d("TAG", "onComplete: " + shortLink);
                            Log.d("TAG", "onComplete: " + flowchartLink);

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");

                            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                            Intent chooser = Intent.createChooser(intent, "공유하기");
                            startActivity(chooser);

                        } else {
                            // Error
                            // ...
                        }
                    }
                });
    }

    @Override
    public void setUserId(String userId) {
        PreferenceManager.setString(this,"userId",userId);
    }

    void sendFcmTokenToServer(String userId) {
        String fcmToken = PreferenceManager.getString(this, "fcmToken");
        if (!fcmToken.isEmpty()) {  // 새로운 fcm token이 있다면 서버로 저장 / 그렇지 않으면 retrofit 호출안함 (shared에 값이 있느냐 없느냐로 결정) / 매번 DB의 값을 불러와 대조 하는 것은 비효율적
            Log.d("TAG", "onResponse: sendFcmTokenToServer");
            RetrofitConnection retrofitConnection = new RetrofitConnection();
            Call<retrofitData> call = retrofitConnection.server.setFcmToken(userId, fcmToken);
            call.enqueue(new Callback<retrofitData>() {
                @Override
                public void onResponse(Call<retrofitData> call, Response<retrofitData> response) {
                    Log.d("TAG", "onResponse:" + response);
                    if (response.body().getResult().equals("OK")) {
                        PreferenceManager.setString(MainActivity.this, "fcmToken", null);
                        Log.d("TAG", "onResponse: 저장 성공");
                    } else if (response.body().getResult().equals("FAILED")) {
                        Log.d("TAG", "onResponse: 저장 실패");
                    }
                }

                @Override
                public void onFailure(Call<retrofitData> call, Throwable t) {
                    Log.d("TAG", "onFailure: " + t);
                }
            });
        }
    }

    private void handleDeepLink() { // 공유하기 링크를 타고 들어왔을때
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                        if (pendingDynamicLinkData != null) {
                            Uri deepLink = pendingDynamicLinkData.getLink();
                            Uri data = getIntent().getData();
                            Log.d("TAG", "onSuccess: " + deepLink);
                            Log.d("TAG", "onSuccess: " + deepLink.getQueryParameter("toWallet"));
                            Log.d("TAG", "onSuccess: " + data.getQueryParameter("toWallet"));
                            webView.loadUrl(String.valueOf(deepLink));
                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "onFailure: " + e);
                    }
                });
    }

}
