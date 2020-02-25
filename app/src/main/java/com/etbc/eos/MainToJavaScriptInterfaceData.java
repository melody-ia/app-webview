package com.etbc.eos;

public interface MainToJavaScriptInterfaceData {
    void moveToFingerPrintDialogActivity(String userPwd, boolean check);

    void moveToQrScannerActivity();

    void setFcmToken(String userId);
}
