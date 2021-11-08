package com.juns.sdk.core.sdk.track;

import android.os.Looper;

import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import java.util.HashMap;

/**
 * Data：06/11/2018-11:02 AM
 * Author: ranger
 */
public class UploadRoleTask {

    private int retryTime = 0;

    private android.os.Handler mHandler;

    private UploadRoleTask() {
        mHandler = new android.os.Handler(Looper.getMainLooper());
    }

    public static void uploadRole(HashMap<String, String> roleInfo, int type) {
//        new UploadRoleTask().doUploadRole(roleInfo, type);
    }

//    private void doUploadRole(final HashMap<String, String> roleInfo, final int type) {
//        if (roleInfo == null || roleInfo.isEmpty()) {
//            mHandler.removeCallbacksAndMessages(null);
//            mHandler = null;
//            return;
//        }
//        RoleInfoParams roleInfoParams = new RoleInfoParams(roleInfo, type);
//        x.http().post(roleInfoParams, new Callback.CommonCallback<JunSResponse>() {
//            @Override
//            public void onSuccess(JunSResponse tnResponse) {
//                mHandler.removeCallbacksAndMessages(null);
//                mHandler = null;
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                if (TrackRetryHandler.canRetry(ex, retryTime)) {
//                    retryTime++;
//                    if (mHandler != null) {
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                doUploadRole(roleInfo, type);
//                            }
//                        }, retryTime * 1000);
//                    }
//                } else {
//                    mHandler.removeCallbacksAndMessages(null);
//                    mHandler = null;
//                }
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
//    }
}
