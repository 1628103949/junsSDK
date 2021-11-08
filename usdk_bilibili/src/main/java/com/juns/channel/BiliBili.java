package com.juns.channel;

import android.app.Activity;
import android.os.Bundle;

import com.bsgamesdk.android.BSGameSdk;
import com.bsgamesdk.android.callbacklistener.AccountCallBackListener;
import com.bsgamesdk.android.callbacklistener.BSGameSdkError;
import com.bsgamesdk.android.callbacklistener.CallbackListener;
import com.bsgamesdk.android.callbacklistener.ExitCallbackListener;
import com.bsgamesdk.android.callbacklistener.InitCallbackListener;
import com.bsgamesdk.android.callbacklistener.OrderCallbackListener;
import com.bsgamesdk.android.utils.LogUtils;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BiliBili extends OPlatformSDK {
    private static final String TAG = "BiliBili";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    // 配置参数
    String uid;
    String userName;
    String nickname;
    public BiliBili(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {

        BSGameSdk.initialize(false, activity, SDKApplication.getPlatformConfig().getExt1(), SDKApplication.getPlatformConfig().getExt2(),
                SDKApplication.getPlatformConfig().getExt3(), SDKApplication.getPlatformConfig().getExt5(), new InitCallbackListener() {
                    @Override
                    public void onSuccess() {
                        Bus.getDefault().post(OInitEv.getSucc());
                    }

                    @Override
                    public void onFailed() {
                        Bus.getDefault().post(OInitEv.getFail(2,"fail"));
                    }
                }, new ExitCallbackListener() {
                    @Override
                    public void onExit() {
                        Bus.getDefault().post(OExitEv.getSucc());
                    }
                });
        BSGameSdk.getInstance().setAccountListener(new AccountCallBackListener() {
            @Override
            public void onAccountInvalid() {
                isNotifyZone = false;
                Bus.getDefault().post(new OLogoutEv());
            }
        });
    }

    @Override
    public void login(final Activity activity) {
        BSGameSdk.getInstance().login(new CallbackListener() {
            @Override
            public void onSuccess(Bundle bundle) {
                uid = bundle.getString("uid");
                userName = bundle.getString("username");
                String access_token = bundle.getString("access_token");
                nickname = bundle.getString("nickname");
                login2RSService(uid,access_token);
            }

            @Override
            public void onFailed(BSGameSdkError bsGameSdkError) {
                Bus.getDefault().post(OLoginEv.getFail(bsGameSdkError.getErrorCode(),bsGameSdkError.getErrorMessage()));
            }

            @Override
            public void onError(BSGameSdkError bsGameSdkError) {
                Bus.getDefault().post(OLoginEv.getFail(bsGameSdkError.getErrorCode(),bsGameSdkError.getErrorMessage()));
            }
        });
    }
    private void login2RSService(String uid,String token ) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid",uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }
    private boolean isNotifyZone = false;
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            BSGameSdk.getInstance().start(mainAct);
            logger.print("进入游戏");
            if(!isNotifyZone){
                BSGameSdk.getInstance().notifyZone(SDKApplication.getPlatformConfig().getExt3(),SDKApplication.getPlatformConfig().getExt4(),submitInfo.get(JunSConstants.SUBMIT_ROLE_ID),submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
                isNotifyZone = true;
            }
        }
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            logger.print("创建角色");
                BSGameSdk.getInstance().createRole(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME),submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));

        }
    }

    @Override
    public void logout(final Activity mainAct) {
        BSGameSdk.getInstance().logout(new CallbackListener() {
            @Override
            public void onSuccess(Bundle bundle) {
                isNotifyZone = false;
                BSGameSdk.floatLogout(mainAct);
                BSGameSdk.getInstance().stop(mainAct);
                Bus.getDefault().post(new OLogoutEv());
            }

            @Override
            public void onFailed(BSGameSdkError bsGameSdkError) {

            }

            @Override
            public void onError(BSGameSdkError bsGameSdkError) {

            }
        });
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print(payParams.toString());
        String sign = "";
        int gameMoney = 1;
        int money = 1;
        try {
            JSONObject dataJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            sign = dataJson.getString("sign");
            gameMoney = dataJson.getInt("game_money");
            money = dataJson.getInt("money");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String notify_url = "";//不为空的话支付后异步通知到此地址，否则异步通知到正式地址，此字段可用于沙盒支付，正式上线前请置空
        String out_trade_number = payParams.get(JunSConstants.PAY_M_ORDER_ID);

        long uidLong = Long.valueOf(uid);
        //int fee = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));

        // 支付操作
        BSGameSdk.getInstance().pay(uidLong, userName, nickname, SDKApplication.getPlatformConfig().getExt3(), money,
                /* 注意这里fee是以分为单位的，以元为单位换算时要先除以100.0 */gameMoney,
                out_trade_number, payParams.get(JunSConstants.PAY_ORDER_NAME), payParams.get(JunSConstants.PAY_ORDER_NAME),
                "", notify_url, sign, new OrderCallbackListener() {
                    @Override
                    public void onSuccess(String out_trade_no, String bs_trade_no) {
                        // 此处为操作成功时执行，返回值通过Bundle传回
                        LogUtils.d("onSuccess");
                        Bus.getDefault().post(OPayEv.getSucc(bs_trade_no));
                    }

                    @Override
                    public void onFailed(String out_trade_no,
                                         BSGameSdkError arg0) {
                        // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                        LogUtils.d("onFailed\n" + "payOutTradeNo: "
                                + out_trade_no + "\nErrorCode : "
                                + arg0.getErrorCode() + "\nErrorMessage : "
                                + arg0.getErrorMessage());
                        Bus.getDefault().post(OPayEv.getFail(2,arg0.getErrorMessage()));
                    }

                    @Override
                    public void onError(String out_trade_no, BSGameSdkError arg0) {
                        // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                        LogUtils.d("onError\n" + "payOutTradeNo: "
                                + out_trade_no + "\nErrorCode : "
                                + arg0.getErrorCode() + "\nErrorMessage : "
                                + arg0.getErrorMessage());
                        Bus.getDefault().post(OPayEv.getFail(2,arg0.getErrorMessage()));
                    }
                });

    }
    @Override
    public void exitGame(final Activity activity) {
        BSGameSdk.getInstance().exit(new ExitCallbackListener() {
            @Override
            public void onExit() {
                BSGameSdk.getInstance().stop(activity);
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        BSGameSdk.appOnline(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        BSGameSdk.appOffline(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        BSGameSdk.appDestroy(mainAct);
    }
}
