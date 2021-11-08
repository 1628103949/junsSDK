package com.juns.sdk.core.sdk.flow;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.http.params.ActiveParam;
import com.juns.sdk.core.sdk.JunSUtils;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.MiIdHelper;
import com.juns.sdk.core.sdk.common.NoticeDialog;
import com.juns.sdk.core.sdk.common.SplashDialog;
import com.juns.sdk.core.sdk.event.EvInit;
import com.juns.sdk.core.sdk.update.TNUpdate;
import com.juns.sdk.core.sdk.update.UpdateView;
import com.juns.sdk.framework.common.Dev;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimerTask;

/**
 * Description: control init flow.
 * Data：28/09/2018-10:44 AM
 * Author: ranger
 */
public class MActiveFlow {
    //SDK重试次数
    private static final int INIT_RETRY_MAX = 2;
    //闪屏时间1.5S
    private static final int SPLASH_TIME = 1500;

    //是否为首次初始化，首次初始化才出现闪屏
    private static boolean isFirstInit = true;
    private static long startSplashTime = 0L;
    private static boolean initLock = false;

    private Activity initActivity;
    private MInitCallback initCallback;
    private int initRetryTime = 0;
    private SplashDialog mSplashDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private JunSResponse mInitResponse;
    private NoticeDialog initNoticeDialog;

    public static synchronized void setInitLock(boolean lock) {
        initLock = lock;
    }

    //进行初始化流程
    public void doMInit(Activity act, MInitCallback callback) {
        this.initActivity = act;
        this.initCallback = callback;
        if (initLock) {
            //已在初始化流程中，忽略处理
            return;
        }
        initLock = true;
        //进行闪屏流程
        if (isFirstInit) {
            dealSplashFlow();
        }
        isFirstInit = false;
        //进行初始化网络请求
        requestInit();
    }

    private boolean shouldShowSplash() {
        if (!SDKApplication.getPlatformConfig().getShowSplash().equals("1")) {
            return false;
        }
        //其他情况，则需要显示
        return true;
    }

    //dismiss闪屏
    public void dismissInitSplash() {
        if (mSplashDialog != null && mSplashDialog.isShowing()) {
            mSplashDialog.dismiss();
        }
        mSplashDialog = null;
    }

    //处理闪屏，确保Activity onResume后再出现闪屏
    //闪屏时间计算如下：1、时长为1.5S；2、初始化成功后，才关闭，不够1.5S则补上
    private void dealSplashFlow() {
        if (!shouldShowSplash()) {
            return;
        }

        if (mSplashDialog == null) {
            mSplashDialog = new SplashDialog(initActivity, new SplashDialog.SplashCallback() {
                @Override
                public void onFinish() {
                    //闪屏流程完成后，往下执行初始化流程
                    if (mInitResponse != null) {
                        dealDataFlow(mInitResponse);
                    }
                }
            });
        }
        try {
            if (!mSplashDialog.isShowing()) {
                mSplashDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        startSplashTime = System.currentTimeMillis();
    }

    private void requestInit() {
        //首次激活，先获取oaid
        if (SDKData.getSdkFirstActive()) {
            if (TextUtils.isEmpty(MiIdHelper.mOAID)) {
                SDKData.setSdkOaid("");
            } else {
                SDKData.setSdkOaid(MiIdHelper.mOAID);
            }
        }
        final int[] count = {0};
        if(SDKData.getSdkKaState() == 1){
            final java.util.Timer timer = new java.util.Timer(true);
            TimerTask task = new TimerTask() {
                public void run() {
                    //每次需要执行的代码放到这里面。
                    //当获取成功后释放timer对象
                    //int count = 0;
                    count[0]++;
                    if(SDKData.getSdkKaState() == 2 || count[0] > 8){
                        timer.cancel();
                        ActiveParam activeParam = new ActiveParam();
                        x.http().post(activeParam, new Callback.CommonCallback<JunSResponse>() {
                            @Override
                            public void onSuccess(JunSResponse tnResponse) {
                                dealReqSuccess(tnResponse);
                            }

                            @Override
                            public void onError(Throwable ex, boolean isOnCallback) {
                                dealReqFail(ex);
                            }

                            @Override
                            public void onCancelled(CancelledException cex) {

                            }

                            @Override
                            public void onFinished() {

                            }
                        });
                    }
                    //timer.cancel();
                }
            };
            timer.schedule(task, 500,500);
        }else{
            ActiveParam activeParam = new ActiveParam();
            x.http().post(activeParam, new Callback.CommonCallback<JunSResponse>() {
                @Override
                public void onSuccess(JunSResponse tnResponse) {
                    dealReqSuccess(tnResponse);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    dealReqFail(ex);
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
        }


            }
//        }, SDKData.getSdkInitDelay());
//
//
//
//    }

    private void dealReqSuccess(JunSResponse tnResponse) {
        SDKCore.logger.print(tnResponse.toString());
        mInitResponse = tnResponse;
        if (shouldShowSplash() && mSplashDialog != null && mSplashDialog.isShowing()) {
            //闪屏回调处处理
            long currentTime = System.currentTimeMillis();
            if (currentTime - startSplashTime > SPLASH_TIME) {
                //dismiss闪屏
                dismissInitSplash();
            } else {
                //补够1.5S
                long remainTime = SPLASH_TIME - (currentTime - startSplashTime);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //dismiss闪屏
                        dismissInitSplash();
                    }
                }, remainTime);
            }
        } else {
            dealDataFlow(mInitResponse);
        }
    }

    private void dealReqFail(Throwable errorException) {
        if (errorException instanceof JunSServerException) {
            //服务端错误
            Bus.getDefault().post(EvInit.getFail(JunSConstants.Status.SERVER_ERR, ((JunSServerException) errorException).getServerMsg()));
        } else {
            //非服务端错误
            //非服务器错误，进行重试
            initRetryTime = initRetryTime + 1;
            if (initRetryTime <= INIT_RETRY_MAX) {
                //重试
                //间隔时间逐步递增
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestInit();
                    }
                }, 1000 * initRetryTime);
            } else {
                //回调初始化失败
                Bus.getDefault().post(EvInit.getFail(JunSConstants.Status.HTTP_ERR, errorException.getMessage()));
            }
        }
    }

    private void dealDataFlow(JunSResponse tnResponse) {
        try {
            if (TextUtils.isEmpty(tnResponse.data) || tnResponse.data.equals("[]")) {
                Bus.getDefault().post(EvInit.getFail(JunSConstants.Status.PARSE_ERR, "MActive parser data error."));
            } else {
                final JSONObject dataJson = new JSONObject(tnResponse.data);
                dealConfig(dataJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bus.getDefault().post(EvInit.getFail(JunSConstants.Status.PARSE_ERR, "MActive parser data error."));
        }
    }

    private void dealConfig(JSONObject dataJson) {
        try {
            //后端返回duid
            if (dataJson.has("duid")) {
                String duid = dataJson.getString("duid");
                if (TextUtils.isEmpty(duid)) {
                    //使用本地生成的
                    SDKData.setSdkDuid(Dev.getDevId(SDKCore.getMainAct()));
                } else {
                    SDKData.setSdkDuid(duid);
                }
            }
            //跑马灯
            if (dataJson.has("prompt")) {
                SDKData.setUserHorsePrompt(dataJson.getString("prompt"));
            }
            //用户协议
            if (dataJson.has("userService")) {
                SDKData.setUserAgreement(dataJson.getString("userService"));
            }
            if (dataJson.has("findAccount")) {
                SDKData.setUserFindPwd(dataJson.getString("findAccount"));
            }
            if (dataJson.has("sansungLoginParams")) {
                SDKData.setLoginParams(dataJson.getString("sansungLoginParams"));
            }
            dealHeartBeat(dataJson);
            dealUpdate(dataJson);
        } catch (Exception e) {
            e.printStackTrace();
            Bus.getDefault().post(EvInit.getFail(JunSConstants.Status.PARSE_ERR, "MActive parser data error."));
        }
    }
    //初始化心跳
    private void dealHeartBeat(final JSONObject dataJson){
        if (dataJson.has("polltime")) {
            try {
                SDKData.setSdkPeriod(dataJson.getInt("polltime"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    private void dealUpdate(final JSONObject dataJson) {
        try {
            //更新类型：1 -> 普通更新，2 ->强制更新
            if (dataJson.has("utype") && dataJson.has("uurl") && dataJson.has("uct")) {
                //处理强更新
                int updateType = dataJson.getInt("utype");
                String updateUrl = dataJson.getString("uurl");
                String updateDescription = dataJson.getString("uct");

                switch (updateType) {
                    case 1:
                        //普通更新
                        TNUpdate.startUpdate(initActivity, false, updateUrl, updateDescription, new UpdateView.UpdateViewCallback() {
                            @Override
                            public void onFinish() {
                                dealNotice(dataJson);
                            }
                        });
                        return;

                    case 2:
                        //强制更新
                        TNUpdate.startUpdate(initActivity, true, updateUrl, updateDescription, new UpdateView.UpdateViewCallback() {
                            @Override
                            public void onFinish() {
                                dealNotice(dataJson);
                            }
                        });
                        return;

                    default:
                        break;
                }
            }
            dealNotice(dataJson);
        } catch (JSONException e) {
            e.printStackTrace();
            Bus.getDefault().post(EvInit.getFail(JunSConstants.Status.PARSE_ERR, "MActive parser data error."));
        }
    }

    private void dealNotice(JSONObject dataJson) {
        try {
            if (dataJson.has("nurl")) {
                String noticeUrl = JunSUtils.buildCommonWebUrl(dataJson.getString("nurl"));
                if (!TextUtils.isEmpty(noticeUrl)) {
                    if (initNoticeDialog != null && initNoticeDialog.isShowing()) {
                        initNoticeDialog.dismiss();
                    }
                    initNoticeDialog = null;
                    initNoticeDialog = new NoticeDialog(initActivity, noticeUrl, new NoticeDialog.NoticeCallback() {
                        @Override
                        public void onFinish() {
                            callInitFinish();
                        }
                    });
                    initNoticeDialog.show();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callInitFinish();
    }

    private void callInitFinish() {
        if (SDKData.getSdkFirstActive()) {
            SDKData.setSdkFirstActive(false);
        }
        dismissInitSplash();
        if (initCallback != null) {
            initCallback.onFinish();
        }
    }

    public interface MInitCallback {
        void onFinish();
    }
}
