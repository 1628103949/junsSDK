package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.huosdk.huounion.sdk.HuoUnionHelper;
import com.huosdk.huounion.sdk.domain.pojo.UserToken;
import com.huosdk.huounion.sdk.pay.Order;
import com.huosdk.huounion.sdk.plugin.IHuoUnionSDKCallback;
import com.huosdk.huounion.sdk.user.HuoUnionUserInfo;
import com.juns.sdk.core.api.JunSConstants;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WoLing extends OPlatformSDK {
    private static final String TAG = "WoLing";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public WoLing(OPlatformBean pBean) {
        super(pBean);
    }
    HuoUnionHelper sdkHelper = null;

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        sdkHelper = HuoUnionHelper.getInstance();
    }

    @Override
    public void init(final Activity activity) {
        sdkHelper.init(activity, new IHuoUnionSDKCallback() {
            @Override
            public void onInitResult(int i, String s) {
                if(i == 1){
                    Bus.getDefault().post(OInitEv.getSucc());
                }else{
                    Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,s));
                }

            }

            @Override
            public void onLoginSuccess(UserToken userToken) {
                //Log.e("guoinfo",userToken.toString());
                HuoUnionHelper.getInstance().showFloatButton();
                login2RSService(userToken.cp_mem_id,userToken.cp_user_token);
            }

            @Override
            public void onLoginFail(int i, String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onLogoutFinished(int i) {
                Bus.getDefault().post(new OLogoutEv());
            }

            @Override
            public void onPaySuccess() {
                Bus.getDefault().post(OPayEv.getSucc("success"));
            }

            @Override
            public void onPayFail(int i, String s) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onExitGameResult(int code) {
                if(code==IHuoUnionSDKCallback.STATUS_CANCEL){
                    //通知游戏，取消退出了
                }else if(code==IHuoUnionSDKCallback.STATUS_SUCCESS){
                    //退出成功了,游戏需执行内部的销毁操作，不需要后台任务的游戏可直接调用sdk 提供killGame方法结束游戏
                    HuoUnionHelper.getInstance().killGame(activity);
                    Bus.getDefault().post(OExitEv.getSucc());
                }else if(code==IHuoUnionSDKCallback.STATUS_NO_PROVIDE){
                    //渠道没有提供退出功能，调用游戏的退出界面，若游戏没有退出界面，调用sdk提供的默认退出界面
                    HuoUnionHelper.getInstance().showDefaultExitUI();
                }
            }

            @Override
            public void submitRoleEventResult(int i, String s) {

            }
        },true);
    }


    @Override
    public void login(Activity activity) {
        HuoUnionHelper.getInstance().login();
    }

    private void login2RSService(String uid, String token) {
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

    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        HuoUnionUserInfo huoUnionUserInfo = new HuoUnionUserInfo();
        //huoUnionUserInfo.
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            huoUnionUserInfo.setEvent("2");
        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            huoUnionUserInfo.setEvent("1");
        }else{
            huoUnionUserInfo.setEvent("3");
        }
        huoUnionUserInfo.setServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
        huoUnionUserInfo.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
        huoUnionUserInfo.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
        huoUnionUserInfo.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        huoUnionUserInfo.setRoleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));
        huoUnionUserInfo.setRoleVip(0);
        HuoUnionHelper.getInstance().submitRoleEvent(huoUnionUserInfo);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
       // Log.e("guoinfo",payParams.toString());
        Order order = new Order();
        order.setIsStandard(2);
        order.setCurrency("CNY");
        order.setCpOrderId(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        order.setProductPrice(Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY)));
        JSONObject payJson = null;
        try {
            payJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            order.setProductId(payJson.getString("productId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        order.setProductCnt(1);
        order.setProductName(payParams.get(JunSConstants.PAY_ORDER_NAME));
        order.setProductDesc(payParams.get(JunSConstants.PAY_ORDER_NAME));
        order.setExt(payParams.get(JunSConstants.PAY_EXT));
        HuoUnionHelper.getInstance().pay(order);
    }

    @Override
    public void exitGame(Activity activity) {
        HuoUnionHelper.getInstance().exitGame();
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        HuoUnionHelper.getInstance().showFloatButton();
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        HuoUnionHelper.getInstance().hideFloatButton();
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        HuoUnionHelper.getInstance().onRestart();
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        HuoUnionHelper.getInstance().onNewIntent(data);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        HuoUnionHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        HuoUnionHelper.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
