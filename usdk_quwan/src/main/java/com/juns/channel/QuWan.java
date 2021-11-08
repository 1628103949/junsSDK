package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;


import com.enjoy.sdk.core.api.EnjoyPayInfo;
import com.enjoy.sdk.core.api.EnjoySdkApi;
import com.enjoy.sdk.core.api.EnjoySubmitInfo;
import com.enjoy.sdk.core.api.callback.EnjoyCallback;
import com.enjoy.sdk.core.api.callback.EnjoyLogoutCallback;
import com.enjoy.sdk.core.api.callback.EnjoyPayCallback;
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
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class QuWan extends OPlatformSDK {
    private static final String TAG = "QuWan";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public QuWan(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        logger.print("quwaninit");
        EnjoySdkApi.getInstance().sdkInit(activity, SDKApplication.getPlatformConfig().getExt1(), new EnjoyCallback() {
            @Override
            public void onSuccess(String s) {
                Bus.getDefault().post(OInitEv.getSucc());
            }

            @Override
            public void onFail(int i, String s) {
                Bus.getDefault().post(OInitEv.getFail(i,s));
            }
        });
        EnjoySdkApi.getInstance().setLogoutCallback(new EnjoyLogoutCallback() {
            @Override
            public void onLogout() {
                Bus.getDefault().post(new OLogoutEv());
            }
        });
    }

    @Override
    public void login(Activity activity) {
        EnjoySdkApi.getInstance().sdkLogin(activity, new EnjoyCallback() {
            @Override
            public void onSuccess(String userInfo) {
                try {
                    JSONObject userJson = new JSONObject(userInfo);
                    String token = userJson.getString("token");
                    String userId = userJson.getString("userId");
                    login2RSService(token,userId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int i, String s) {
                Bus.getDefault().post(OLoginEv.getFail(i,s));
            }
        });
    }

    private void login2RSService(String token, String uid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        sendRoleInfo(mainAct,submitInfo);
    }
    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(Activity activity,HashMap<String, String> params) {

        EnjoySdkApi.getInstance().sdkSubmitInfo(activity, new EnjoySubmitInfo.SubmitBuilder()
                        //⻆⾊ID，建议数字【必传】
                .submitRoleId(params.get(JunSConstants.SUBMIT_ROLE_ID))
                //⻆⾊名称，【必传】
                .submitRoleName(params.get(JunSConstants.SUBMIT_ROLE_NAME))
                //⻆⾊等级，⽆则不传，【选传】
                .submitRoleLevel(Integer.parseInt(params.get(JunSConstants.SUBMIT_ROLE_LEVEL)))
                //服务器ID，建议数字，【必传】
                .submitServerId(params.get(JunSConstants.SUBMIT_SERVER_ID))
                //服务器名称，【必传】
                .submitServerName(params.get(JunSConstants.SUBMIT_SERVER_NAME))
                //玩家VIP等级，数字，⽆则不传，【选传】
                .submitVip(Integer.parseInt(params.get(JunSConstants.SUBMIT_VIP)))
                .submitTimeCreate(Integer.parseInt(params.get(JunSConstants.SUBMIT_CREATE_TIME)))
                .submitType(params.get(JunSConstants.SUBMIT_TYPE))
                .build(), new EnjoyCallback() {
            @Override
            public void onSuccess(String s) {
                logger.print("quwan sendRoleInfoonSuccess");
            }

            @Override
            public void onFail(int i, String s) {
                logger.print("quwan sendRoleInfoonFail");
            }
        });

    }

    @Override
    public void logout(Activity mainAct) {


    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print("quwanPayinfo"+payParams.toString());
        float money = Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        EnjoySdkApi.getInstance().sdkPay(activity, new EnjoyPayInfo.PayBuilder()
                        //CP订单号，全局唯⼀，不可重复，【必传】
                        .payOrderId(payParams.get(JunSConstants.PAY_M_ORDER_ID))
                        //充值⾦额，单位：元，【必传】
                        .payMoney(money)
                        //商品名称，【必传】
                        .payOrderName(payParams.get(JunSConstants.PAY_ORDER_NAME))
                        //CP扩展字段，⻓度255字符，⽆则不传，【选传】
                        .payExt(payParams.get(JunSConstants.PAY_EXT))
                        //⻆⾊ID，建议数字，【必传】
                        .payRoleId(payParams.get(JunSConstants.PAY_ROLE_ID))
                        //⻆⾊名称，【必传】
                        .payRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME))
                        //⻆⾊等级，⽆则不传，【选传】
                        .payRoleLevel(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_LEVEL)))
                        //服务器ID，建议数字，【必传】
                        .payServerId(payParams.get(JunSConstants.PAY_SERVER_ID))
                        //服务器名称，【必传】
                        .payServerName(payParams.get(JunSConstants.PAY_SERVER_NAME))
                        //⻆⾊VIP等级，【选传】
                        .payRoleVip(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_VIP)))
                        .build(),
                new EnjoyPayCallback() {
                    @Override
                    public void onFinish(int i, String s) {
                        Bus.getDefault().post(OPayEv.getSucc(s));
                    }
                });
    }

    @Override
    public void exitGame(Activity activity) {
        EnjoySdkApi.getInstance().sdkGameExit(activity, new EnjoyCallback() {
            @Override
            public void onSuccess(String s) {
                Bus.getDefault().post(OExitEv.getSucc());
            }

            @Override
            public void onFail(int i, String s) {
                Bus.getDefault().post(OExitEv.getFail(i,s));
            }
        });
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        EnjoySdkApi.getInstance().sdkOnCreate(mainAct);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        EnjoySdkApi.getInstance().sdkOnStart(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        EnjoySdkApi.getInstance().sdkOnRestart(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        EnjoySdkApi.getInstance().sdkOnResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        EnjoySdkApi.getInstance().sdkOnPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        EnjoySdkApi.getInstance().sdkOnStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        EnjoySdkApi.getInstance().sdkOnDestroy(mainAct);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        EnjoySdkApi.getInstance().sdkOnActivityResult(mainAct,requestCode,resultCode,data);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        EnjoySdkApi.getInstance().sdkOnNewIntent(mainAct,data);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        EnjoySdkApi.getInstance().sdkOnConfigurationChanged(mainAct,newConfig);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        EnjoySdkApi.getInstance().sdkOnRequestPermissionsResult(mainAct,requestCode,permissions,grantResults);
    }
}
