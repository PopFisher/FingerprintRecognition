package fingerprint.com.fingerprintrecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import fingerprint.com.fingerprintrecognition.core.FingerprintCore;

public class FingerprintMainActivity extends Activity {

    private FingerprintCore mFingerprintCore;

    private Toast mToast;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_main);
        mFingerprintCore = new FingerprintCore(this);
        mFingerprintCore.setFingerprintManager(mResultListener);
        Button fingerBtn = (Button) findViewById(R.id.btn_activity_main_finger);
        fingerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFingerprintRecognition();
            }
        });
    }

    /**
     * 开始指纹识别
     */
    private void startFingerprintRecognition() {
        if (mFingerprintCore.isSupport()) {
            toastTipMsg(R.string.fingerprint_recognition_tip);
            mFingerprintCore.startAuthenticate();
        } else {
            toastTipMsg(R.string.fingerprint_recognition_not_support);
        }
    }

    private FingerprintCore.IFingerprintResultListener mResultListener = new FingerprintCore.IFingerprintResultListener() {
        @Override
        public void onAuthenticateSuccess() {
            toastTipMsg(R.string.fingerprint_recognition_success);
        }

        @Override
        public void onAuthenticateFailed(int helpId) {
            toastTipMsg(R.string.fingerprint_recognition_failed);
        }

        @Override
        public void onAuthenticateError(int errMsgId) {
            toastTipMsg(R.string.fingerprint_recognition_error);
        }

        @Override
        public void onStartAuthenticateResult(boolean isSuccess) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == KeyguardLockScreenManager.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                toastTipMsg("识别成功");
            } else {
                toastTipMsg("识别失败");
            }
        }
    }

    private void toastTipMsg(int messageId) {
        if (mToast == null) {
            mToast = Toast.makeText(this, messageId, Toast.LENGTH_LONG);
        }
        mToast.setText(messageId);
        mToast.cancel();
        mHandler.removeCallbacks(mShowToastRunnable);
        mHandler.postDelayed(mShowToastRunnable, 0);
    }

    private void toastTipMsg(String message) {
        if (mToast == null) {
            mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        }
        mToast.setText(message);
        mToast.cancel();
        mHandler.removeCallbacks(mShowToastRunnable);
        mHandler.postDelayed(mShowToastRunnable, 200);
    }

    private Runnable mShowToastRunnable = new Runnable() {
        @Override
        public void run() {
            mToast.show();
        }
    };
}
