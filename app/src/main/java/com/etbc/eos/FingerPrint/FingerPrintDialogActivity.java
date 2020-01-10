package com.etbc.eos.FingerPrint;

import android.content.Intent;
import android.graphics.Color;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.etbc.eos.R;

public class FingerPrintDialogActivity extends Activity implements FingerPrintAuthDialogFragment.SecretAuthorize {

    private FingerPrintAuthDialogFragment mFragment;
    String userPwd;
    boolean check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_dialog);

        init();
        //세팅부분
        mFragment = new FingerPrintAuthDialogFragment(userPwd, check);

        mFragment.setCallback(this);
        mFragment.show(this.getFragmentManager(), "my_fragment");
        mFragment.setCancelable(false);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#000000"));
    }

    private void init() {
        userPwd = getIntent().getStringExtra("userPwd");
        check = getIntent().getBooleanExtra("check", false);
    }

    @Override
    public void success() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void fail() {
        Toast.makeText(this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
    }
}
