package com.etbc.eos;

public interface MainToJavaScriptInterfaceData {
    void moveToFingerPrintDialogActivity(String userPwd, boolean check);

    void moveToQrScannerActivity();

    void setFcmToken(String userId);

    void setMoveToWebPage(String url);

    void setShareData(String toWallet);
}
