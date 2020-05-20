package com.willsoft.webview;

public interface MainToJavaScriptInterfaceData {
    void moveToFingerPrintDialogActivity(boolean check);

    void moveToQrScannerActivity();

    void setFcmToken(String userId);

    void setMoveToWebPage(String url);

    void setShareData(String toWallet);

    void setUserId(String userId);
}
