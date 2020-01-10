package com.etbc.eos.FingerPrint;

import android.annotation.SuppressLint;
import android.app.DialogFragment;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.etbc.eos.R;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

@SuppressLint("ValidFragment")
public class FingerPrintAuthDialogFragment extends DialogFragment implements TextView.OnEditorActionListener, FingerPrintUiHelper.Callback {

    private Button mCancelButton;
    private Button mSecondDialogButton;
    private View mFingerprintContent;
    private View mBackupContent;
    private EditText mPassword;
    private ImageView mVisibilityOn , mVisibilityOff;


    private FingerprintManagerCompat.CryptoObject mCryptoObject;
    private FingerPrintUiHelper mFingerprintUiHelper;

    private SecretAuthorize secretAuthorize;
    private int mStage;

    public final static int FINGER_PRINT = 1;
    public final static int PASS_WORD = 2;

    private Animation vibrateAnim;

    String userPwd;
    private boolean check;

    public FingerPrintAuthDialogFragment(String userPwd, boolean check) {
        this.userPwd = userPwd;
        this.check = check;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        checkFingerOrPassWord();

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);

        if (Build.VERSION.SDK_INT > LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        }

        vibrateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.vibrate_anim);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setTitle(getString(R.string.payment_pro));
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FingerPrintDialogActivity) getActivity()).finish();
            }
        });

        mSecondDialogButton = (Button) v.findViewById(R.id.second_dialog_button);
        mSecondDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStage == FINGER_PRINT) {
                    goToBackup();
                } else {
                    verifyPassword();
                }
            }
        });
        mFingerprintContent = v.findViewById(R.id.fingerprint_container);
        mBackupContent = v.findViewById(R.id.backup_container);
        mPassword = (EditText) v.findViewById(R.id.password);
        mPassword.setOnEditorActionListener(this);


        mVisibilityOn = (ImageView) v.findViewById(R.id.visibilityOn);
        mVisibilityOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 비밀번호 보이게
                mVisibilityOn.setVisibility(View.GONE);
                mVisibilityOff.setVisibility(View.VISIBLE);
                mPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                mPassword.setSelection(mPassword.getText().length());
            }
        });

        mVisibilityOff = (ImageView) v.findViewById(R.id.visibilityOff);
        mVisibilityOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 비밀번호 안보이게
                mVisibilityOff.setVisibility(View.GONE);
                mVisibilityOn.setVisibility(View.VISIBLE);
                mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mPassword.setSelection(mPassword.getText().length());
            }
        });

        mFingerprintUiHelper = new FingerPrintUiHelper(getActivity().getApplicationContext(),
                (ImageView) v.findViewById(R.id.fingerprint_icon),
                (TextView) v.findViewById(R.id.fingerprint_status), this);
        updateStage();

        // If fingerprint authentication is not available, switch immediately to the backup
        // (password) screen.
        if (!FingerPrintUtil.isFingerprintAuthAvailable(getActivity().getApplicationContext())) {
            Toast.makeText(getContext(), R.string.check_finger_in_device, Toast.LENGTH_LONG).show();
            goToBackup();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == FINGER_PRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManagerCompat.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private void goToBackup() {
        mStage = PASS_WORD;
        updateStage();
        mPassword.requestFocus();


        // Fingerprint is not used anymore. Stop listening for it.
        mFingerprintUiHelper.stopListening();
    }

    /**
     * Checks whether the current entered password is correct, and dismisses the the dialog and
     * let's the activity know about the result.
     */
    private void verifyPassword() {

        if (mPassword.getText().toString().equals(userPwd)) {
            dismiss();
            secretAuthorize.success();
            mStage = FINGER_PRINT;
        } else {
            mBackupContent.startAnimation(vibrateAnim);
            mPassword.setText("");
        }
    }

    /**
     * @return true if {@code password} is correct, false otherwise
     */
    private boolean checkPassword(String password) {
        // Assume the password is always correct.
        // In the real world situation, the password needs to be verified in the server side.
        return password.length() > 0;
    }


    private void updateStage() {
        switch (mStage) {
            case FINGER_PRINT:
                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setText(R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                mBackupContent.setVisibility(View.GONE);
                break;
            case PASS_WORD:
                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setText(R.string.ok);
                mFingerprintContent.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            verifyPassword();
            return true;
        }
        return false;
    }

    @Override
    public void onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        dismiss();
        secretAuthorize.success();
    }

    @Override
    public void onError() {
        goToBackup();
    }


    public void setCallback(SecretAuthorize secretAuthorize) {
        this.secretAuthorize = secretAuthorize;
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public interface SecretAuthorize {
        void success();

        void fail();
    }

    public void checkFingerOrPassWord() {
        if (check) {
            mStage = FINGER_PRINT;
        } else {
            mStage = PASS_WORD;
        }
    }

}