package fingerprint.com.fingerprintrecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import fingerprint.com.fingerprintrecognition.core.FingerprintCore;

public class FingerprintMainActivity extends Activity {

    private final static String TAG = "finger_log";
    private FingerprintCore mFingerprintCore;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_main);
        mFingerprintCore = new FingerprintCore(this);
        mFingerprintCore.setFingerprintManager(mResultListener);
        Button btn_finger = (Button) findViewById(R.id.btn_activity_main_finger);
        btn_finger.setOnClickListener(new View.OnClickListener() {
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
            Toast.makeText(FingerprintMainActivity.this, "请进行指纹识别，长按指纹解锁键", Toast.LENGTH_LONG).show();
            mFingerprintCore.startAuthenticate();
        } else {

        }
    }

    private FingerprintCore.IFingerprintResultListener mResultListener = new FingerprintCore.IFingerprintResultListener() {
        @Override
        public void onAuthenticateSuccess() {

        }

        @Override
        public void onAuthenticateFailed(int helpId) {

        }

        @Override
        public void onFailedIdentify(int errMsgId) {

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
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
