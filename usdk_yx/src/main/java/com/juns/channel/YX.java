package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.sspyx.center.widget.NormalDialog;
import com.sspyx.psdk.GameListener;
import com.sspyx.psdk.SSPPay;
import com.sspyx.psdk.SSPSDK;
import com.sspyx.psdk.SSPUser;
import com.sspyx.psdk.bean.Payment;
import com.sspyx.psdk.bean.RoleInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class YX extends OPlatformSDK {
    private static final String TAG = "YX";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public YX(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        SSPSDK.getInstance().onCreate(activity, new GameListener() {
            @Override
            public void onInit(int code) {
                if (code == GameListener.SUCCESS) {
                    // TODO 初始化成功！
                    Bus.getDefault().post(OInitEv.getSucc());
                } else {
                    // TODO 初始化失败！
                    Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
                }

            }

            @Override
            public void onLogin(int code, String s) {
                if (code == GameListener.SUCCESS){
                    login2RSService(s);
                }else {
                    Bus.getDefault().post(OLoginEv.getFail(code,s));
                }
            }

            @Override
            public void onPay(int code, String s) {
                if (code == GameListener.SUCCESS){
                    Bus.getDefault().post(OPayEv.getSucc(s));
                }else {
                    Bus.getDefault().post(OPayEv.getFail(code,s));
                }
            }

            @Override
            public void onLogout(int code) {
                if (code == GameListener.SUCCESS){
                    Bus.getDefault().post(new OLogoutEv());
                }
            }

            @Override
            public void onAntiAddiction(int i) {

            }

            @Override
            public void onRealName(int i) {

            }

            @Override
            public void onExit(int code) {
                if (code == GameListener.SUCCESS){
                    Bus.getDefault().post(OExitEv.getSucc());
                }
            }
        });
    }

    @Override
    public void login(Activity activity) {
        SSPUser.getInstance().login();
    }

    private void login2RSService(String token) {
        OPlatformUtils.loginToServer(token);
    }

    @Override
    public void logout(Activity mainAct) {
        SSPUser.getInstance().logout();
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        RoleInfo info = new RoleInfo();
        //huoUnionUserInfo.
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            info.setDataType(RoleInfo.CREATE_ROLE);
        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            info.setDataType(RoleInfo.ENTER_GAME);
        }else{
            info.setDataType(RoleInfo.LEVEL_UP);
        }

        info.setServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
        info.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
        info.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
        info.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        info.setRoleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));
        info.setPartyName("");
        info.setBalance("");
        info.setVipLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_VIP)));
        info.setRoleCTime(Long.parseLong(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME)));
        SSPUser.getInstance().submitExtraData(info);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //Log.e("guoinfo",payParams.toString());
        RoleInfo info = new RoleInfo();
        info.setServerId(payParams.get(JunSConstants.PAY_SERVER_ID));
        info.setServerName(payParams.get(JunSConstants.PAY_SERVER_NAME));
        info.setRoleId(payParams.get(JunSConstants.PAY_ROLE_ID));
        info.setRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));
        info.setRoleLevel(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_LEVEL)));
        info.setPartyName("");
        info.setBalance("");
        info.setVipLevel(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_VIP)));
        Payment payment = new Payment();
        payment.setOrderId(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        JSONObject payJson = null;
        try {
            payJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            payment.setProductId(payJson.getString("productId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        payment.setExt(payParams.get(JunSConstants.PAY_EXT));
        payment.setProductName(payParams.get(JunSConstants.PAY_ORDER_NAME));
        payment.setProductDesc(payParams.get(JunSConstants.PAY_ORDER_NAME));
        payment.setRatio(Integer.parseInt(payParams.get(JunSConstants.PAY_RATE)));
        payment.setProductPrice((int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100);
        SSPPay.getInstance().pay(info, payment);
    }

    @Override
    public void exitGame(Activity activity) {
        if (SSPUser.getInstance().hasExitUI()) {//判断当前渠道是否有退出弹窗，有，则直接调用exit方法
            SSPUser.getInstance().exit();
        } else {// 没有，则需游戏工程实现退出弹窗，并在点击退出时调用exit方法，此处NormalDialog仅为示例
            new NormalDialog(activity, "退出游戏？", "不", null, "是的", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SSPUser.getInstance().exit();
                }
            }).show();
        }
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        SSPSDK.getInstance().onStart();
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        SSPSDK.getInstance().onPause();
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        SSPSDK.getInstance().onRestart();
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        SSPSDK.getInstance().onResume();
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        SSPSDK.getInstance().onStop();
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        SSPSDK.getInstance().onDestroy();
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        SSPSDK.getInstance().onNewIntent(data);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        SSPSDK.getInstance().onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        SSPSDK.getInstance().onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
