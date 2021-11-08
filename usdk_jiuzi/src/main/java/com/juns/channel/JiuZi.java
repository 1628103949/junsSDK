package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

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
import com.jzyx.sdk.open.JZYXSDK;
import com.jzyx.sdk.open.PayParams;
import com.jzyx.sdk.open.RoleExtraData;
import com.jzyx.sdk.open.SDKListener;
import com.jzyx.sdk.open.SDKUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JiuZi extends OPlatformSDK {
    private static final String TAG = "JiuZi";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public JiuZi(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        JZYXSDK.setSDKCallback(new SDKListener() {
            @Override
            public void onInitSuccess(String s) {
                Bus.getDefault().post(OInitEv.getSucc());
            }

            @Override
            public void onInitFail(String s) {
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onLoginSuccess(SDKUser sdkUser) {
               // Log.e("guoinfo",sdkUser.getUserID()+" |||| "+sdkUser.getToken());
                login2RSService(sdkUser.getUserID(),sdkUser.getToken());
            }

            @Override
            public void onLoginFail(String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onLoginCancel() {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
            }

            @Override
            public void onLogoutSuccess() {
                Bus.getDefault().post(new OLogoutEv());
            }

            @Override
            public void onLogoutFail(String s) {

            }

            @Override
            public void onExitSuccess() {
                JZYXSDK.submitExtraData(mSubmitData);
                Bus.getDefault().post(OExitEv.getSucc());
            }

            @Override
            public void onExitFail() {

            }

            @Override
            public void onExitCancel() {

            }

            @Override
            public void onPaySuccess() {
                Bus.getDefault().post(OPayEv.getSucc("success"));
            }

            @Override
            public void onPayFail(String s) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onPayCancel() {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
            }
        });
    }

    @Override
    public void init(Activity activity) {
        JZYXSDK.init(activity, SDKApplication.getPlatformConfig().getExt1(),SDKApplication.getPlatformConfig().getExt2());
    }

    @Override
    public void login(Activity activity) {
        JZYXSDK.login();
    }
    private void login2RSService(String uid,String token ) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid",uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {
        JZYXSDK.logout();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        PayParams params = new PayParams();
        int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        params.setProductId(money+"");//物品ID不能为空
        params.setProductName(payParams.get(JunSConstants.PAY_ORDER_NAME));// 物品名称不能为空;
        params.setProductDesc(payParams.get(JunSConstants.PAY_ORDER_NAME));// 物品描述不能为空
        params.setPrice(money);// 商品价格（单位元）
        params.setExtension(payParams.get(JunSConstants.PAY_M_ORDER_ID));//透传参数
        params.setServerId(payParams.get(JunSConstants.PAY_SERVER_ID));// 区服ID
        params.setServerName(payParams.get(JunSConstants.PAY_SERVER_NAME));// 区服名
        params.setRoleId(payParams.get(JunSConstants.PAY_ROLE_ID));// 角色ID
        params.setRoleLevel(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_LEVEL)));// 角色名等级
        params.setRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));// 角色名
        params.setVip(payParams.get(JunSConstants.PAY_ROLE_VIP));//角色VIP等级
        params.setTime(System.currentTimeMillis()+"");//下单时间
        JZYXSDK.pay(params);
    }
    RoleExtraData mSubmitData;
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            mSubmitData = new RoleExtraData();
            mSubmitData.setDataType(RoleExtraData.TYPE_ENTER_GAME);//选择服务器
            mSubmitData.setRoleID(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            mSubmitData.setRoleLevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
            mSubmitData.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            mSubmitData.setServerID(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//服务器id
            mSubmitData.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));//服务器名称
            mSubmitData.setVipLevel(submitInfo.get(JunSConstants.SUBMIT_VIP));//玩家VIP等级
            mSubmitData.setRoleCreatTime(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME));//角色创建时间
            JZYXSDK.submitExtraData(mSubmitData);
        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_UPGRADE)){
            mSubmitData = new RoleExtraData();
            mSubmitData.setDataType(RoleExtraData.TYPE_LEVEL_UP);//选择服务器
            mSubmitData.setRoleID(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            mSubmitData.setRoleLevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
            mSubmitData.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            mSubmitData.setServerID(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//服务器id
            mSubmitData.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));//服务器名称
            mSubmitData.setVipLevel(submitInfo.get(JunSConstants.SUBMIT_VIP));//玩家VIP等级
            mSubmitData.setRoleCreatTime(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME));//角色创建时间
            JZYXSDK.submitExtraData(mSubmitData);
        }
    }

    @Override
    public void exitGame(Activity activity) {
        JZYXSDK.gameExit();
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        JZYXSDK.onPause(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        JZYXSDK.onResume(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        JZYXSDK.onDestroy(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        JZYXSDK.onRestart(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        JZYXSDK.onStop(mainAct);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        JZYXSDK.onStart(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        JZYXSDK.onNewIntent(data);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        JZYXSDK.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        JZYXSDK.onConfigurationChanged(newConfig);
    }

}
