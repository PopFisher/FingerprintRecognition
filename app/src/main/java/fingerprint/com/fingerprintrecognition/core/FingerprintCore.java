package fingerprint.com.fingerprintrecognition.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;

import java.lang.ref.WeakReference;

import fingerprint.com.fingerprintrecognition.log.FPLog;

/**
 * Created by popfisher on 2016/11/7.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintCore {

    private static final int NONE = 0;
    private static final int CANCEL = 1;
    private static final int AUTHENTICATING = 2;
    private int mState = NONE;

    private FingerprintManager mFingerprintManager;
    private WeakReference<IFingerprintResultListener> mFpResultListener;
    private CancellationSignal mCancellationSignal;
    private CryptoObjectCreator mCryptoObjectCreator;

    private int mFailedTimes = 0;
    private boolean isSupport = false;

    public interface IFingerprintResultListener {
        void onAuthenticateSuccess();

        void onAuthenticateFailed(int helpId);

        void onFailedIdentify(int errMsgId);

        void onStartAuthenticateResult(boolean isSuccess);
    }

    public FingerprintCore(Context context) {
        mFingerprintManager = getFingerprintManager(context);
        isSupport = (mFingerprintManager != null && isHardwareDetected());
        FPLog.log("fingerprint isSupport: " + isSupport);
        mCryptoObjectCreator = new CryptoObjectCreator(new CryptoObjectCreator.ICryptoObjectCreateListener() {
            @Override
            public void onDataPrepared(FingerprintManager.CryptoObject cryptoObject) {
                startAuthenticate(cryptoObject);
            }
        });
    }

    public void setFingerprintManager(IFingerprintResultListener fingerprintResultListener) {
        mFpResultListener = new WeakReference<IFingerprintResultListener>(fingerprintResultListener);
    }

    public void startAuthenticate() {
        startAuthenticate(mCryptoObjectCreator.getCryptoObject());
    }

    public void startAuthenticate(FingerprintManager.CryptoObject cryptoObject) {
        mCancellationSignal = new CancellationSignal();
        mState = AUTHENTICATING;
        try {
            mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0, mAuthCallback, null);
            FPLog.log("start authenticate...");
            if (mFpResultListener.get() != null) {
                mFpResultListener.get().onStartAuthenticateResult(true);
            }
        } catch (Exception e) {
            try {
                mFingerprintManager.authenticate(null, mCancellationSignal, 0, mAuthCallback, null);
                if (mFpResultListener.get() != null) {
                    mFpResultListener.get().onStartAuthenticateResult(true);
                }
            } catch (Exception e2) {
                FPLog.log("startListening, Exception:" + Log.getStackTraceString(e2));
                if (mFpResultListener.get() != null) {
                    mFpResultListener.get().onStartAuthenticateResult(false);
                }
            }
        }
    }

    public void cancelAuthenticate() {
        if (mCancellationSignal != null && mState == AUTHENTICATING) {
            FPLog.log("cancelAuthenticate...");
            mState = CANCEL;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    private FingerprintManager.AuthenticationCallback mAuthCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            // 多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证,一般间隔要超过30秒
            mState = NONE;
            FPLog.log("onAuthenticationError, errId:" + errMsgId + ", err:" + errString + ", retry after 30 seconds");
            if (FingerprintManager.FINGERPRINT_ERROR_LOCKOUT == errMsgId) {
                if (null != mFpResultListener && null != mFpResultListener.get()) {
                    mFpResultListener.get().onFailedIdentify(errMsgId);
                }
            }
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            mState = NONE;
            FPLog.log("onAuthenticationHelp, helpId:" + helpMsgId + ", help:" + helpString);
            onFailedRetry(helpMsgId);
        }

        @Override
        public void onAuthenticationFailed() {
            mState = NONE;
            FPLog.log("onAuthenticationFailed");
            onFailedRetry(0);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            mState = NONE;
            FPLog.log("onAuthenticationSucceeded");
            if (null != mFpResultListener && null != mFpResultListener.get()) {
                mFpResultListener.get().onAuthenticateSuccess();
            }
        }
    };

    private void onFailedRetry(int msgId) {
        mFailedTimes++;
        if (null != mFpResultListener && null != mFpResultListener.get()) {
            mFpResultListener.get().onAuthenticateFailed(msgId);
        }
        FPLog.log("on failed retry time " + mFailedTimes);
        cancelAuthenticate();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startAuthenticate(mCryptoObjectCreator.getCryptoObject());
            }
        }, 300); // 每次重试间隔一会儿再启动
    }

    public boolean isSupport() {
        return isSupport;
    }

    /**
     * 时候有指纹识别硬件支持
     * @return
     */
    public boolean isHardwareDetected() {
        try {
            mFingerprintManager.isHardwareDetected();
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    /**
     * 时候录入指纹
     * @return
     */
    public boolean hasEnrolledFingerprints() {
        try {
            // 有些厂商api23之前的版本可能没有做好兼容，这个方法内部会崩溃（redmi note2, redmi note3等）
            return mFingerprintManager.hasEnrolledFingerprints();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 指纹识别时候有效，有硬件支持并且有录入指纹返回true
     * @return
     */
    public boolean isFingerprintAvailable() {
        return isHardwareDetected() && hasEnrolledFingerprints();
    }

    public static FingerprintManager getFingerprintManager(Context context) {
        FingerprintManager fingerprintManager = null;
        try {
            fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        } catch (Throwable e) {
            FPLog.log("have not class FingerprintManager");
        }
        return fingerprintManager;
    }
}
