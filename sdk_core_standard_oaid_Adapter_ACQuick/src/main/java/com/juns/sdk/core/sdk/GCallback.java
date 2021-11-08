package com.juns.sdk.core.sdk;

import android.os.Handler;
import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.params.MPayQueryParam;
import com.juns.sdk.core.sdk.ads.JunsAds;
import com.juns.sdk.core.sdk.common.HeartBeat;
import com.juns.sdk.core.sdk.common.NoticeDialog;
import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.core.sdk.event.EvInit;
import com.juns.sdk.core.sdk.event.EvLogin;
import com.juns.sdk.core.sdk.event.EvLogout;
import com.juns.sdk.core.sdk.event.EvPay;
import com.juns.sdk.core.sdk.event.EvSubmit;
import com.juns.sdk.core.sdk.event.EvSubmitLog;
import com.juns.sdk.core.sdk.flow.MActiveFlow;
import com.juns.sdk.framework.utils.ReflectUtils;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-12:16 PM
 * Author: ranger
 */
public class GCallback {
    private SDKCore sdkCore;
    private Object initCallback;
    private Object loginCallback;
    private Object payCallback;
    private Object exitCallback;
    private Object logoutCallback;
    private Object submitCallback;
    private Object eventSubmitCallback;

    private NoticeDialog payNoticeDialog;

    GCallback(SDKCore sdkCore) {
        this.sdkCore = sdkCore;
    }

    void setInitCallback(Object initCallback) {
        this.initCallback = null;
        this.initCallback = initCallback;
    }

    void setLogoutCallback(Object logoutCallback) {
        this.logoutCallback = null;
        this.logoutCallback = logoutCallback;
    }

    void setLoginCallback(Object loginCallback) {
        this.loginCallback = null;
        this.loginCallback = loginCallback;
    }

    void setPayCallback(Object payCallback) {
        this.payCallback = null;
        this.payCallback = payCallback;
    }

    void setExitCallback(Object exitCallback) {
        this.exitCallback = null;
        this.exitCallback = exitCallback;
    }
    void setEventSubmitCallback(Object eventSubmitCallback) {
        this.eventSubmitCallback = null;
        this.eventSubmitCallback = eventSubmitCallback;
    }
    void setSubmitCallback(Object submitCallback) {
        this.submitCallback = null;
        this.submitCallback = submitCallback;
    }

    public void destroyCallback() {
        initCallback = null;
        logoutCallback = null;
        loginCallback = null;
        payCallback = null;
        exitCallback = null;
        submitCallback = null;
        if (payNoticeDialog != null && payNoticeDialog.isShowing()) {
            payNoticeDialog.dismiss();
        }
        sdkCore = null;
    }

    @BusReceiver
    public void onInitEvent(EvInit evInit) {
        SDKCore.logger.print("onInitEvent --> " + evInit.toString());
        if (initCallback == null) {
            return;
        }
        MActiveFlow.setInitLock(false);
        if (evInit.getRet() == EvInit.SUCCESS) {
            //success
            SDKCore.logger.print("onInitEvent --> " + evInit.toString());
            JunsAds.getInstance().onEvActive(SDKCore.getMainAct());
            SDKCore.setSdkInitialized(true);
            ReflectUtils.reflect(initCallback).method("onSuccess", evInit.getInitInfo());
            if (SDKCore.getSdkShouldLogin()) {
                if (sdkCore != null) {
                    sdkCore.doInsideLogin();
                    SDKCore.setSdkShouldLogin(false);
                }
            }
        } else {
            //fail
            ReflectUtils.reflect(initCallback).method("onFail", evInit.getInitCode(), evInit.getInitMsg());
        }
    }

    @BusReceiver
    public void onLoginEvent(EvLogin evLogin) {
        SDKCore.logger.print("onLoginEvent --> " + evLogin.toString());
        if (loginCallback == null) {
            SDKCore.logger.print("onLoginEvent --> " + "loginCallback null");
            return;
        }

        if (evLogin.getRet() == EvLogin.SUCCESS) {
            //success
            SDKCore.logger.print("onLoginEvent --> " + "loginCallback success");
            SDKCore.setSdkLogined(true);
            ReflectUtils.reflect(loginCallback).method("onSuccess", evLogin.getUserInfo());
        } else {
            //fail
            SDKCore.logger.print("onLoginEvent --> " + "loginCallback fail");
            ReflectUtils.reflect(loginCallback).method("onFail", evLogin.getLoginCode(), evLogin.getLoginMsg());
        }
    }

    @BusReceiver
    public void onLogoutEvent(EvLogout evLogout) {
        SDKCore.logger.print("onLogoutEvent");

        if (logoutCallback == null) {
            return;
        }
        //重置登录状态
        //停止心跳
        HeartBeat.getInstance().endHeartBeat();
        SDKCore.setSdkLogined(false);
        ReflectUtils.reflect(logoutCallback).method("onLogout");
    }

    @BusReceiver
    public void onPayEvent(final EvPay evPay) {
        SDKCore.logger.print("onPayEvent --> " + evPay.toString());

        if (payCallback == null) {
            return;
        }

        //所有支付状态都查询一遍
        MPayQueryParam mPayQueryParam = new MPayQueryParam();
        x.http().post(mPayQueryParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse response) {
                try {
                    JSONObject dataJson = new JSONObject(response.data);

                    try {
                        //广告上报数据
                        String money = dataJson.getString("money");
                        String payWay = dataJson.getString("pway");
                        String goodsName = dataJson.getString("goodsname");
                        JunsAds.getInstance().onEvPay(SDKCore.getMainAct(), goodsName, payWay, money);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (dataJson.has("nurl")) {
                        String noticeUrl = JunSUtils.buildCommonWebUrl(dataJson.getString("nurl"), true);
                        if (!TextUtils.isEmpty(noticeUrl)) {
                            if (payNoticeDialog != null && payNoticeDialog.isShowing()) {
                                payNoticeDialog.dismiss();
                            }
                            payNoticeDialog = null;
                            payNoticeDialog = new NoticeDialog(SDKCore.getMainAct(), noticeUrl, new NoticeDialog.NoticeCallback() {
                                @Override
                                public void onFinish() {
                                    dealPayCallback(evPay);
                                }
                            });
                            payNoticeDialog.show();
                        }
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dealPayCallback(evPay);
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                //不用管
                dealPayCallback(evPay);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void dealPayCallback(EvPay evPay) {
        if (evPay.getRet() == EvPay.SUCCESS) {
            //success
            ReflectUtils.reflect(payCallback).method("onFinish", JunSConstants.Status.PAY_SUCC, evPay.getPayInfo());
        } else {
            //fail
            ReflectUtils.reflect(payCallback).method("onFinish", evPay.getPayCode(), evPay.getPayMsg());
        }
    }

    @BusReceiver
    public void onExitEvent(EvExit evExit) {
        SDKCore.logger.print("onExitEvent --> " + evExit.toString());

        if (exitCallback == null) {
            if (SDKCore.getMainAct() != null) {
                SDKCore.getMainAct().finish();
                System.exit(1);
            }
            return;
        }

        if (evExit.getRet() == EvExit.SUCCESS) {
            //success
            ReflectUtils.reflect(exitCallback).method("onSuccess", evExit.getExitInfo());
        } else {
            //fail
            ReflectUtils.reflect(exitCallback).method("onFail", evExit.getExitCode(), evExit.getExitMsg());
        }
    }

    @BusReceiver
    public void onEventSubmit(EvSubmitLog evSubmit) {
        SDKCore.logger.print("onEventSubmit --> " + evSubmit.toString());
        if (eventSubmitCallback == null) {
            return;
        }

        if (evSubmit.getRet() == EvSubmit.SUCCESS) {
            //success
            ReflectUtils.reflect(eventSubmitCallback).method("onSuccess", evSubmit.getSubmitInfo());
        } else {
            //fail
            ReflectUtils.reflect(eventSubmitCallback).method("onFail", evSubmit.getSubmitCode(), evSubmit.getSubmitMsg());
        }
    }
    @BusReceiver
    public void onSubmitEvent(EvSubmit evSubmit) {
        SDKCore.logger.print("onSubmitEvent --> " + evSubmit.toString());
        if (submitCallback == null) {
            return;
        }

        if (evSubmit.getRet() == EvSubmit.SUCCESS) {
            //success
            ReflectUtils.reflect(submitCallback).method("onSuccess", evSubmit.getSubmitInfo());
        } else {
            //fail
            ReflectUtils.reflect(submitCallback).method("onFail", evSubmit.getSubmitCode(), evSubmit.getSubmitMsg());
        }
    }
}
