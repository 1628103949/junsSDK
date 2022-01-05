package com.juns.sdk.core.api;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import com.juns.sdk.core.api.callback.JunSCallback;
import com.juns.sdk.core.api.callback.JunSLogoutCallback;
import com.juns.sdk.core.api.callback.JunSPayCallback;
import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.view.common.ConfirmDialog;

/**
 * Data：17/12/2018-11:25 AM
 * Author: ranger
 */
public class JunSSdkApi implements IJunSSdk {
    private static final String TAG = "api";
    static TNLog logger = LogFactory.getLog(TAG, true);

    private SDKCore mSDKCore;

    public JunSSdkApi() {
        mSDKCore = new SDKCore();
    }

    public static JunSSdkApi getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public void sdkInit(Activity mainAct, String appKey, JunSCallback callback) {
        logger.print("sdkInit called.");
        mSDKCore.sdkInit(mainAct, appKey, callback);
    }

    @Override
    public void sdkEventPost(String name, String data, JunSCallback callback) {
        logger.print("sdkEventPost called.");
        mSDKCore.sdkEventPost(name,data,callback);
    }

    @Override
    public void setLogoutCallback(JunSLogoutCallback callback) {
        mSDKCore.setLogoutCallback(callback);
    }

    @Override
    public void sdkLogin(Activity mainAct, JunSCallback callback) {
        logger.print("sdkLogin called.");
//        JunsNotiDialog.showNoti(SDKCore.getMainAct(),"未成年登陆提示：" +
//                "\n    您已被识别为未成年人，根据国家新闻出版署《关于防止未成年人沉迷网络游戏的通知》，非周五、周六、周日和法定节假日20时至21时，该游戏将不以任何形式为未成年人提供游戏服务！",false,190);
        mSDKCore.sdkLogin(mainAct, callback);
    }

    @Override
    public void sdkLogout(Activity mainAct) {
        logger.print("sdkLogout called.");
        mSDKCore.sdkLogout(mainAct);
    }

    @Override
    public void sdkDIYEvent(Activity mainAct, String info) {
        logger.print("sdkDIYEvent called."+info);
        mSDKCore.sdkDIYEvent(mainAct,info);
    }

    @Override
    public boolean isLogined() {
        logger.print("isLogined called.");
        return mSDKCore.isLogined();
    }

    @Override
    public String isShop() {
        logger.print("isShop called.");
        return mSDKCore.isShop();
    }

    @Override
    public void sdkPay(final Activity mainAct, final JunSPayInfo payInfo, final JunSPayCallback callback) {
        logger.print("sdkPay called.");
        payInfo.debugParam(mainAct, new ConfirmDialog.ConfirmCallback() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onConfirm() {
                if (payInfo.checkParam()) {
                    mSDKCore.sdkPay(mainAct, payInfo, callback);
                } else {
                    callback.onFinish(JunSConstants.Status.SDK_ERR, "param is illegal, please check!");
                }

            }
        });
    }

    @Override
    public void sdkGameExit(Activity mainAct, JunSCallback callback) {
        logger.print("sdkGameExit called.");
        mSDKCore.sdkGameExit(mainAct, callback);
    }

    @Override
    public void sdkSubmitInfo(final Activity mainAct, final JunSSubmitInfo submitInfo, final JunSCallback callback) {
        logger.print("sdkSubmitInfo called.");
        submitInfo.debugParam(mainAct, new ConfirmDialog.ConfirmCallback() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onConfirm() {
                if (submitInfo.checkParam()) {
                    mSDKCore.sdkSubmitInfo(mainAct, submitInfo, callback);
                } else {
                    callback.onFail(JunSConstants.Status.SDK_ERR, "param is illegal, please check!");
                }
            }
        });
    }

    @Override
    public void showFloat(Activity mainAct) {
        mSDKCore.showFloat(mainAct);
    }

    @Override
    public void hideFloat(Activity mainAct) {
        mSDKCore.hideFloat(mainAct);
    }

    @Override
    public String sdkGetConfig(Activity mainAct) {
        logger.print("sdkGetConfig called.");
        return mSDKCore.sdkGetConfig(mainAct);
    }

    @Override
    public void sdkOnCreate(Activity mainAct) {
        logger.print("sdkOnCreate called.");
        mSDKCore.sdkOnCreate(mainAct);
    }

    @Override
    public void sdkOnStart(Activity mainAct) {
        logger.print("sdkOnStart called.");
        mSDKCore.sdkOnStart(mainAct);
    }

    @Override
    public void sdkOnRestart(Activity mainAct) {
        logger.print("sdkOnRestart called.");
        mSDKCore.sdkOnRestart(mainAct);
    }

    @Override
    public void sdkOnResume(Activity mainAct) {
        logger.print("sdkOnResume called.");
        mSDKCore.sdkOnResume(mainAct);
    }

    @Override
    public void sdkOnPause(Activity mainAct) {
        logger.print("sdkOnPause called.");
        mSDKCore.sdkOnPause(mainAct);
    }

    @Override
    public void sdkOnStop(Activity mainAct) {
        logger.print("sdkOnStop called.");
        mSDKCore.sdkOnStop(mainAct);
    }

    @Override
    public void sdkOnDestroy(Activity mainAct) {
        logger.print("sdkOnDestroy called.");
        mSDKCore.sdkOnDestroy(mainAct);
    }

    @Override
    public void sdkOnActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        logger.print("sdkOnActivityResult called.");
        mSDKCore.sdkOnActivityResult(mainAct, requestCode, resultCode, data);
    }

    @Override
    public void sdkOnNewIntent(Activity mainAct, Intent data) {
        logger.print("sdkOnNewIntent called.");
        mSDKCore.sdkOnNewIntent(mainAct, data);
    }

    @Override
    public void sdkOnConfigurationChanged(Activity mainAct, Configuration newConfig) {
        logger.print("sdkOnConfigurationChanged called.");
        mSDKCore.sdkOnConfigurationChanged(mainAct, newConfig);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        logger.print("sdkOnRequestPermissionsResult called.");
        mSDKCore.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
    }

    private static class SingletonHolder {
        private static final JunSSdkApi INSTANCE = new JunSSdkApi();
    }
}
