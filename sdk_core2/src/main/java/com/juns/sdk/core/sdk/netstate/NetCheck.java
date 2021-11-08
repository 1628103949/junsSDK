package com.juns.sdk.core.sdk.netstate;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.utils.NetworkUtils;
import com.juns.sdk.framework.view.common.TipsDialog;
import com.juns.sdk.framework.view.common.ViewUtils;

/**
 * Data：16/10/2018-4:52 PM
 * Author: ranger
 */
public class NetCheck {

    public static void checkNet(Activity act, @NonNull final NetFlowCallback callback) {
        if (NetworkUtils.isConnected()) {
            callback.onSuccess();
            return;
        }

        NetCheckDialog.show(act, new NetCheckDialog.NetStateCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFail() {
                if (NetworkUtils.isConnected()) {
                    callback.onSuccess();
                } else {
                    callback.onFail();
                }
            }
        });
    }

    public static void checkNetNotExit(Activity act, @NonNull final NetFlowCallback callback) {
        if (NetworkUtils.isConnected()) {
            //有网络，直接返回
            callback.onSuccess();
            return;
        }

        ViewUtils.showTipsConfirm(act, act.getString(ResUtil.getStringID("juns_network_error", act)), new TipsDialog.TipsConfirmCallback() {
            @Override
            public void onConfirm() {
                if (NetworkUtils.isConnected()) {
                    callback.onSuccess();
                } else {
                    callback.onFail();
                }
            }
        });
    }

    public interface NetFlowCallback {
        void onSuccess();

        void onFail();
    }

}
