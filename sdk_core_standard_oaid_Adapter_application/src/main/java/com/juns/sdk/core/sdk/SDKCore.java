package com.juns.sdk.core.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.provider.Settings;
import android.text.TextUtils;

import com.juns.sdk.core.api.IJunSSdk;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSPayInfo;
import com.juns.sdk.core.api.JunSSubmitInfo;
import com.juns.sdk.core.api.callback.JunSCallback;
import com.juns.sdk.core.api.callback.JunSLogoutCallback;
import com.juns.sdk.core.api.callback.JunSPayCallback;
import com.juns.sdk.core.sdk.ads.JunsAds;
import com.juns.sdk.core.sdk.common.HeartBeat;
import com.juns.sdk.core.sdk.common.InitInfoCallBack;
import com.juns.sdk.core.sdk.common.InitInfoDialog;
import com.juns.sdk.core.sdk.config.SDKConfig;
import com.juns.sdk.core.sdk.event.EvInit;
import com.juns.sdk.core.sdk.event.EvLogin;
import com.juns.sdk.core.sdk.event.EvPay;
import com.juns.sdk.core.sdk.event.EvSubmit;
import com.juns.sdk.core.sdk.event.GOnResume;
import com.juns.sdk.core.sdk.event.OnActivityResult;
import com.juns.sdk.core.sdk.event.OnPause;
import com.juns.sdk.core.sdk.flow.MActiveFlow;
import com.juns.sdk.core.sdk.netstate.NetCheck;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.common.ToastUtil;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.permission.PermissionConstants;
import com.juns.sdk.framework.permission.PermissionUtils;
import com.juns.sdk.framework.utils.AppUtils;
import com.juns.sdk.framework.utils.EmptyUtils;
import com.juns.sdk.framework.utils.FileUtils;
import com.juns.sdk.framework.view.common.ConfirmDialog;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.xbus.Bus;

import java.util.List;

/**
 * Data：17/12/2018-11:48 AM
 * Author: ranger
 */
public class SDKCore implements IJunSSdk {
    private static final int JUNS_PHONE_PERMISSION = 1222;
    private static final int JUNS_SDCARD_PERMISSION = 1333;
    private static final String TAG = "sdk";
    //API调用间隔，300毫秒
//    private static final int API_CALL_INTERVAL = 300;
    public static TNLog logger = LogFactory.getLog(TAG, true);
    private static boolean sdkInitialized = false;
    private static boolean sdkLogined = false;
    private static boolean sdkShouldDoLogin = false;
    //application context
    @SuppressLint("StaticFieldLeak")
    private static Activity mMainAct;
    private GCallback gCallback;
    private PlatformHelper platformHelper;
    private MActiveFlow mInitFlow;
    private Activity currentLoginActivity;
    private JunSCallback currentLoginCallback;

    private boolean haveShowRationale = false;

    public SDKCore() {
        gCallback = new GCallback(SDKCore.this);
        Bus.getDefault().register(gCallback);
        platformHelper = new PlatformHelper(SDKCore.this);
    }

    public static synchronized boolean isSdkInitialized() {
        return sdkInitialized;
    }

    public static synchronized void setSdkInitialized(boolean initial) {
        sdkInitialized = initial;
    }

    public static synchronized boolean isSdkLogined() {
        return sdkLogined;
    }

    public static synchronized void setSdkLogined(boolean logined) {
        sdkLogined = logined;
    }

    public static synchronized boolean getSdkShouldLogin() {
        return sdkShouldDoLogin;
    }

    public static synchronized void setSdkShouldLogin(boolean shouldLogin) {
        sdkShouldDoLogin = shouldLogin;
    }

    public static Activity getMainAct() {
        return mMainAct;
    }

    @Override
    public void sdkInit(final Activity mainAct, String appKey, JunSCallback callback) {
        logger.print("sdkInit called.");
        if (mainAct == null || callback == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkInit api.");
        }

        mMainAct = mainAct;
        gCallback.setInitCallback(callback);
        SDKData.setSdkAppKey(appKey);
        NetCheck.checkNet(mainAct, new NetCheck.NetFlowCallback() {
            @Override
            public void onSuccess() {

                if(SDKApplication.getSdkConfig().isAgreement()){
                    if(!SDKData.getSdkAgree()){
                        InitInfoDialog.show(mainAct, new InitInfoCallBack() {
                            @Override
                            public void toContinue() {
                                SDKData.setSdkAgree(true);
                                requestPhonePermission();
                            }

                            @Override
                            public void toExit() {

                            }
                        });
                    }else {
                        requestSDCardPermission();
                    }
                }else {
                    requestSDCardPermission();
                }
            }

            @Override
            public void onFail() {
                Bus.getDefault().post(EvInit.getFail(JunSConstants.Status.HTTP_ERR, "network error. please check."));
            }
        });
    }

    @Override
    public void sdkEventPost(String name, String data, JunSCallback callback) {
        logger.print("sdkEventPost called.");
        gCallback.setEventSubmitCallback(callback);
        platformHelper.eventSubmit(name,data);

    }

    private void requestPhonePermission() {
        PermissionUtils.permission(PermissionConstants.PHONE)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(final ShouldRequest shouldRequest) {
//                        ViewUtils.showConfirmDialog(mMainAct,
//                                mMainAct.getString(ResUtil.getStringID("juns_request_permission", mMainAct)),
//                                mMainAct.getString(ResUtil.getStringID("juns_device_permission_rationale_message", mMainAct)),
//                                false,
//                                new ConfirmDialog.ConfirmCallback() {
//                                    @Override
//                                    public void onCancel() {
//                                        shouldRequest.again(true);
//                                    }
//
//                                    @Override
//                                    public void onConfirm() {
//                                        shouldRequest.again(true);
//                                    }
//                                });
                        requestSDCardPermission();
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        requestSDCardPermission();
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever,
                                         List<String> permissionsDenied) {
                        if (permissionsDeniedForever != null && !permissionsDeniedForever.isEmpty()) {
//                            ViewUtils.showConfirmDialog(mMainAct,
//                                    mMainAct.getString(ResUtil.getStringID("juns_request_permission", mMainAct)),
//                                    mMainAct.getString(ResUtil.getStringID("juns_device_permission_rationale_message", mMainAct)),
//                                    false,
//                                    new ConfirmDialog.ConfirmCallback() {
//                                        @Override
//                                        public void onCancel() {
//                                            mMainAct.finish();
//                                        }
//
//                                        @Override
//                                        public void onConfirm() {
//                                            PermissionUtils.launchAppDetailsSettings(mMainAct, JUNS_PHONE_PERMISSION);
//                                        }
//                                    });
                            requestSDCardPermission();
                        } else {
                            requestPhonePermission();
                        }
                    }
                })
                .request();
    }

    private void requestSDCardPermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(final ShouldRequest shouldRequest) {
//                        ViewUtils.showConfirmDialog(mMainAct,
//                                mMainAct.getString(ResUtil.getStringID("juns_request_permission", mMainAct)),
//                                mMainAct.getString(ResUtil.getStringID("juns_sdcard_permission_rationale_message", mMainAct)),
//                                false,
//                                new ConfirmDialog.ConfirmCallback() {
//                                    @Override
//                                    public void onCancel() {
//                                        shouldRequest.again(true);
//                                    }
//
//                                    @Override
//                                    public void onConfirm() {
//                                        shouldRequest.again(true);
//                                    }
//                                });
                        //ToastUtil.showLong(mMainAct,"没有获取到读写手机存储权限，如无法正常游戏，请手动进入设置开启。");
                        doInitFlow();
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        doInitFlow();
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever,
                                         List<String> permissionsDenied) {
                        if (permissionsDeniedForever != null && !permissionsDeniedForever.isEmpty()) {
//                            ViewUtils.showConfirmDialog(mMainAct,
//                                    mMainAct.getString(ResUtil.getStringID("juns_request_permission", mMainAct)),
//                                    mMainAct.getString(ResUtil.getStringID("juns_sdcard_permission_rationale_message", mMainAct)),
//                                    false,
//                                    new ConfirmDialog.ConfirmCallback() {
//                                        @Override
//                                        public void onCancel() {
//                                            mMainAct.finish();
//                                        }
//
//                                        @Override
//                                        public void onConfirm() {
//                                            PermissionUtils.launchAppDetailsSettings(mMainAct, JUNS_SDCARD_PERMISSION);
//                                        }
//                                    });
                            //ToastUtil.showLong(mMainAct,"没有获取到读写手机存储权限，如无法正常游戏，请手动进入设置开启。");
                            doInitFlow();
                        } else {
                            requestSDCardPermission();
                        }
                    }
                })
                .request();
    }

    private void doInitFlow() {
        if (SDKData.getSdkFirstActive()) {
            //发起请求获取oaid
            try {
                if (TextUtils.isEmpty(SDKData.getSdkOaid())) {
                    OaidHelper.init(mMainAct);
                    OaidHelper.start(mMainAct);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //获取 Android ID
            if (TextUtils.isEmpty(SDKData.getSdkAndroidId())) {
                String androidId = Settings.System.getString(getMainAct().getContentResolver(), Settings.System.ANDROID_ID);
                SDKData.setSdkAndroidId(androidId);
            }
        }
        try {
            if (!TextUtils.isEmpty(SDKData.getUpdateApkVersion())) {
                String currentApkVersion = AppUtils.getAppVersionName(mMainAct.getPackageName());
                if (!SDKData.getUpdateApkVersion().equals(currentApkVersion)) {
                    //删除旧安装文件
                    FileUtils.deleteFile(SDKData.getUpdateApkPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //获取到权限后，在调用初始化广告
        JunsAds.getInstance().initAds(getMainAct(), SDKApplication.getPlatformConfig().getAdsAppId(), SDKApplication.getPlatformConfig().getAdsAppName(), SDKData.getSdkOaid(), SDKData.getSdkRefer());
        platformHelper.preInit(mMainAct);
        if (mInitFlow == null) {
            mInitFlow = new MActiveFlow();
        }
        mInitFlow.doMInit(mMainAct, new MActiveFlow.MInitCallback() {
            @Override
            public void onFinish() {
                platformHelper.init(mMainAct);
            }
        });
    }

    @Override
    public void setLogoutCallback(JunSLogoutCallback callback) {
        logger.print("setLogoutCallback called.");
        if (callback == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check setLogoutCallback api.");
        }
        gCallback.setLogoutCallback(callback);
    }

    void doInsideLogin() {
        if (currentLoginActivity != null && currentLoginCallback != null) {
            sdkLogin(currentLoginActivity, currentLoginCallback);
        }
    }

    @Override
    public void sdkLogin(final Activity mainAct, JunSCallback callback) {
        logger.print("sdkLogin called.");
        if (mainAct == null || callback == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkLogin api.");
        }
        if (!isSdkInitialized()) {
//            Bus.getDefault().post(EvLogin.getFail(JunSConstants.Status.SDK_ERR, "sdk not initial."));
            sdkShouldDoLogin = true;
            currentLoginActivity = mainAct;
            currentLoginCallback = callback;
            return;
        }
        sdkShouldDoLogin = false;
        gCallback.setLoginCallback(callback);

        //调用登录，注销登录状态
        setSdkLogined(false);

        NetCheck.checkNet(mainAct, new NetCheck.NetFlowCallback() {
            @Override
            public void onSuccess() {
                platformHelper.login(mainAct);
            }

            @Override
            public void onFail() {
                Bus.getDefault().post(EvLogin.getFail(JunSConstants.Status.HTTP_ERR, "network error. please check."));
            }
        });
    }

    @Override
    public void sdkLogout(Activity mainAct) {
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkLogout api.");
        }
        //设置当前登录状态为false
        HeartBeat.getInstance().endHeartBeat();
        SDKCore.setSdkLogined(false);
        //清除帐号信息
        SDKData.cleanUserData();
        platformHelper.logout(mainAct);
    }

    @Override
    public void sdkDIYEvent(Activity mainAct, String info) {
        if (mainAct == null || info == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkLogout api.");
        }
        platformHelper.divEvent(mainAct,info);
    }

    @Override
    public boolean isLogined() {
        return isSdkLogined();
    }

    @Override
    public String isShop() {
        if(EmptyUtils.isEmpty(SDKData.getIsShop())){
            //ToastUtil.showLong(SDKCore.getMainAct(),"查询失败，请先进入游戏");
        }
        return SDKData.getIsShop();
    }

    @Override
    public void sdkPay(final Activity mainAct, final JunSPayInfo payInfo, JunSPayCallback callback) {
        logger.print("sdkPay called.");
        if (mainAct == null || payInfo == null || callback == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkPay api.");
        }

        gCallback.setPayCallback(callback);

        if (!isSdkInitialized()) {
            Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.SDK_ERR, "sdk not initial."));
            return;
        }

        if (!isSdkLogined()) {
            Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.SDK_ERR, "sdk not login."));
            return;
        }

        NetCheck.checkNetNotExit(mainAct, new NetCheck.NetFlowCallback() {
            @Override
            public void onSuccess() {
                platformHelper.pay(mainAct, payInfo.toHash());
            }

            @Override
            public void onFail() {
                Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.HTTP_ERR, "network error. please check."));
            }
        });
    }

    @Override
    public void sdkGameExit(Activity mainAct, JunSCallback callback) {
        logger.print("sdkGameExit called.");
        if (mainAct == null || callback == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkGameExit api.");
        }
        gCallback.setExitCallback(callback);

        platformHelper.exitGame(mainAct);
    }

    @Override
    public void sdkSubmitInfo(Activity mainAct, JunSSubmitInfo submitInfo, JunSCallback callback) {
        logger.print("sdkSubmitInfo called.");
        if (mainAct == null || submitInfo == null) {
            throw new RuntimeException("sdk exception. param contain null, please check sdkSubmitInfo api.");
        }
        gCallback.setSubmitCallback(callback);

        if (!isSdkInitialized()) {
            Bus.getDefault().post(EvSubmit.getFail(JunSConstants.Status.SDK_ERR, "sdk not initial."));
            return;
        }

        if (!isSdkLogined()) {
            Bus.getDefault().post(EvSubmit.getFail(JunSConstants.Status.SDK_ERR, "sdk not login."));
            return;
        }
        platformHelper.submitInfo(mainAct, submitInfo.toHash());
    }

    @Override
    public void showFloat(Activity mainAct) {
        platformHelper.showFloat(mainAct);
    }

    @Override
    public void hideFloat(Activity mainAct) {
        platformHelper.hideFloat(mainAct);
    }

    @Override
    public String sdkGetConfig(Activity mainAct) {
        logger.print("sdkGetConfig called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkGetConfig api.");
        }
        return SDKApplication.getSdkConfig().toString();
    }

    @Override
    public void sdkOnCreate(Activity mainAct) {
        logger.print("sdkOnCreate called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnCreate api.");
        }
        platformHelper.onCreate(mainAct);
    }

    @Override
    public void sdkOnStart(Activity mainAct) {
        logger.print("sdkOnStart called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnStart api.");
        }
        platformHelper.onStart(mainAct);
    }

    @Override
    public void sdkOnRestart(Activity mainAct) {
        logger.print("sdkOnRestart called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnRestart api.");
        }
        platformHelper.onRestart(mainAct);
    }

    @Override
    public void sdkOnResume(Activity mainAct) {
        logger.print("sdkOnResume called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnResume api.");
        }
        platformHelper.onResume(mainAct);
        Bus.getDefault().post(new GOnResume(mainAct));
        JunsAds.getInstance().onEvResume(mainAct);
    }

    @Override
    public void sdkOnPause(Activity mainAct) {
        logger.print("sdkOnPause called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnPause api.");
        }
        platformHelper.onPause(mainAct);
        Bus.getDefault().post(new OnPause(mainAct));
        JunsAds.getInstance().onEvPause(mainAct);
    }

    @Override
    public void sdkOnStop(Activity mainAct) {
        logger.print("sdkOnStop called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnStop api.");
        }
        platformHelper.onStop(mainAct);
        if (mainAct != null && mainAct.isFinishing() && gCallback != null) {
            gCallback.destroyCallback();
            Bus.getDefault().unregister(gCallback);
        }
    }

    @Override
    public void sdkOnDestroy(Activity mainAct) {
        logger.print("sdkOnDestroy called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnDestroy api.");
        }
       // HeartBeat.offline();
        platformHelper.onDestroy(mainAct);
        if (mainAct != null && mainAct.isFinishing() && gCallback != null) {
            gCallback.destroyCallback();
            Bus.getDefault().unregister(gCallback);
        }
    }

    @Override
    public void sdkOnActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        logger.print("sdkOnActivityResult called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnActivityResult api.");
        }

        if (requestCode == JUNS_PHONE_PERMISSION) {
            requestPhonePermission();
            return;
        }

        if (requestCode == JUNS_SDCARD_PERMISSION) {
            requestSDCardPermission();
            return;
        }

        Bus.getDefault().post(new OnActivityResult(requestCode, resultCode, data, mainAct));
        platformHelper.onActivityResult(mainAct, requestCode, resultCode, data);
    }

    @Override
    public void sdkOnNewIntent(Activity mainAct, Intent data) {
        logger.print("sdkOnNewIntent called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnNewIntent api.");
        }
        platformHelper.onNewIntent(mainAct, data);
    }

    @Override
    public void sdkOnConfigurationChanged(Activity mainAct, Configuration newConfig) {
        logger.print("sdkOnConfigurationChanged called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnConfigurationChanged api.");
        }
        platformHelper.onConfigurationChanged(mainAct, newConfig);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        logger.print("sdkOnRequestPermissionsResult called.");
        if (mainAct == null) {
            throw new RuntimeException("juns sdk exception. param contain null, please check sdkOnRequestPermissionsResult api.");
        }
        platformHelper.onRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
    }

}
