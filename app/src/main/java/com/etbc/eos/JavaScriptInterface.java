package com.etbc.eos;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    MainToJavaScriptInterfaceData mainToJavaScriptInterfaceData;

    public JavaScriptInterface(MainToJavaScriptInterfaceData mainToJavaScriptInterfaceData) {
        this.mainToJavaScriptInterfaceData = mainToJavaScriptInterfaceData;
    }

    @JavascriptInterface
    public void callQrScanner() {
        Log.d("TAG", "callQrScanner: ");
        mainToJavaScriptInterfaceData.moveToQrScannerActivity();
    }

    @JavascriptInterface
    public void callFingerPrint(String userPwd, boolean check) {
        Log.d("TAG", "callFingerPrint: " + userPwd);
        Log.d("TAG", "callFingerPrint: " + check);
        mainToJavaScriptInterfaceData.moveToFingerPrintDialogActivity(userPwd, check);
    }

    @JavascriptInterface
    public void setFcmToken(String userId){
        mainToJavaScriptInterfaceData.setFcmToken(userId);
    }
}
