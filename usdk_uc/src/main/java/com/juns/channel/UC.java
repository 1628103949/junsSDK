package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
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
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.flow.MPayFlow;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.gundam.sdk.shell.even.SDKEventKey;
import cn.gundam.sdk.shell.even.SDKEventReceiver;
import cn.gundam.sdk.shell.even.Subscribe;
import cn.gundam.sdk.shell.exception.AliLackActivityException;
import cn.gundam.sdk.shell.exception.AliNotInitException;
import cn.gundam.sdk.shell.open.OrderInfo;
import cn.gundam.sdk.shell.open.ParamInfo;
import cn.gundam.sdk.shell.open.UCOrientation;
import cn.gundam.sdk.shell.param.SDKParamKey;
import cn.gundam.sdk.shell.param.SDKParams;
import cn.uc.gamesdk.UCGameSdk;

public class UC extends OPlatformSDK {
    private static final String TAG = "uc";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;


    public UC(OPlatformBean pBean) {
        super(pBean);
    }

    SDKParams sdkParams = new SDKParams();
    @Override
    public void init(Activity activity) {
        mContext = activity;
        //这里对接渠道初始化
        UCGameSdk.defaultSdk().registerSDKEventReceiver(eventReceiver);
        ParamInfo gpi= new ParamInfo();
        gpi.setGameId(Integer.parseInt(SDKApplication.getPlatformConfig().getExt1()));
        if (SDKApplication.getPlatformConfig().getExt2().equals("0")){
            gpi.setOrientation(UCOrientation.LANDSCAPE);
        }else if(SDKApplication.getPlatformConfig().getExt2().equals("1")){
            gpi.setOrientation(UCOrientation.PORTRAIT);
        }else{
            gpi.setOrientation(UCOrientation.LANDSCAPE);
        }
        //gpi.setOrientation(UCOrientation.LANDSCAPE);
        sdkParams.put(SDKParamKey.GAME_PARAMS,gpi);
        try {
            UCGameSdk.defaultSdk().initSdk(activity,sdkParams);
        } catch (AliLackActivityException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        UCGameSdk.defaultSdk().unregisterSDKEventReceiver(eventReceiver);
    }

    @Override
    public void login(Activity activity) {
        doLogin();
    }

    @Override
    public void logout(Activity mainAct) {
        try {
            UCGameSdk.defaultSdk().logout(mainAct,null);
        } catch (AliLackActivityException e) {
            e.printStackTrace();
        } catch (AliNotInitException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String prePay(Activity mainAct) {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("accountid", SDKData.getpUserId());
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 支付接口
     * payParams：服务器回传数据
     */
    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        doPay(activity, payParams, payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
    }

    @Override
    public void exitGame(Activity activity) {
        doExitGame();
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        if (!submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)) {
            sendRoleInfo(submitInfo);
        }
    }

    private void doLogin() {
        //这里对接渠道登录
        try {
            UCGameSdk.defaultSdk().login((Activity) mContext, sdkParams);
        } catch (AliNotInitException e) {

        } catch (AliLackActivityException e) {
            e.printStackTrace();
        }

    }



    //渠道登录成功后到服务器验证
    private void login2RSService(String sid) {

        OPlatformUtils.loginToServer(sid, null);

    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {
        //角色信息上报
        SDKParams roleParams = new SDKParams();
        roleParams.put(SDKParamKey.STRING_ROLE_ID, params.get(JunSConstants.SUBMIT_ROLE_ID));
        roleParams.put(SDKParamKey.STRING_ROLE_NAME, params.get(JunSConstants.SUBMIT_ROLE_NAME));
        roleParams.put(SDKParamKey.LONG_ROLE_LEVEL, params.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        roleParams.put(SDKParamKey.LONG_ROLE_CTIME, params.get(JunSConstants.SUBMIT_CREATE_TIME));
        roleParams.put(SDKParamKey.STRING_ZONE_ID, params.get(JunSConstants.SUBMIT_SERVER_ID));
        roleParams.put(SDKParamKey.STRING_ZONE_NAME, params.get(JunSConstants.SUBMIT_SERVER_NAME));
        try {
            UCGameSdk.defaultSdk().submitRoleData((Activity) mContext,roleParams);
        } catch (AliNotInitException e) {
            e.printStackTrace();
        } catch (AliLackActivityException e) {
            e.printStackTrace();
        }

    }

    /**
     * 退出游戏
     */
    private void doExitGame() {
        //游戏退出
        try {
            UCGameSdk.defaultSdk().exit((Activity) mContext,null);
        } catch (AliLackActivityException e) {
            e.printStackTrace();
        } catch (AliNotInitException e) {
            e.printStackTrace();
        }

    }

    private void doPay(Context context, HashMap<String, String> payParams,
                       String moid, String mData) {
        //支付对接
       // Log.e("payinfo",payParams.toString());
        SDKParams params = new SDKParams();
        JSONObject data=null;
        try {
            data = new JSONObject(mData);

            params.put(SDKParamKey.CALLBACK_INFO, data.getString("callback_info"));
            params.put(SDKParamKey.NOTIFY_URL, data.getString("notify_url"));
            params.put(SDKParamKey.AMOUNT, data.getString("amount"));
            params.put(SDKParamKey.CP_ORDER_ID, moid);
            params.put(SDKParamKey.ACCOUNT_ID, data.optString("account_id"));
            params.put(SDKParamKey.SIGN_TYPE, data.getString("sign_type"));
            params.put(SDKParamKey.SIGN, data.getString("ucsign"));
           // Log.e("payinfo",params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }



        try {
            UCGameSdk.defaultSdk().pay((Activity) mContext, params);
        } catch (AliLackActivityException e) {
            e.printStackTrace();
        } catch (AliNotInitException e) {
            e.printStackTrace();
        }


    }
    private SDKEventReceiver eventReceiver = new SDKEventReceiver() {
        @Subscribe(event = SDKEventKey.ON_INIT_SUCC)
        private void onInitSucc() {
            Bus.getDefault().post(OInitEv.getSucc());
        }
        @Subscribe(event = SDKEventKey.ON_INIT_FAILED)
        private void onInitFailed(String desc){
            Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.CHANNEL_ERR,desc));
        }
        @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
        private void onLoginSucc(String sid) {
            login2RSService(sid);
        }
        @Subscribe(event = SDKEventKey.ON_LOGIN_FAILED)
        private void onLoginFailed(String desc) {
            Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, desc));
//
        }
        @Subscribe(event = SDKEventKey.ON_CREATE_ORDER_SUCC)
        private void onCreateOrderSucc(OrderInfo orderInfo){
            Bus.getDefault().post(OPayEv.getSucc("pay success."));
        }
        @Subscribe(event = SDKEventKey.ON_PAY_USER_EXIT)
        private void onPayUserExit(OrderInfo orderInfo) {
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "fail"));

        }
        @Subscribe(event = SDKEventKey.ON_LOGOUT_SUCC)
        private void onLogoutSucc() {
            Bus.getDefault().post(new OLogoutEv());
        }
        @Subscribe(event = SDKEventKey.ON_LOGOUT_FAILED)
        private void onLogoutFailed() {

        }
        @Subscribe(event = SDKEventKey.ON_EXIT_SUCC)
        private void onExitSucc() {
            Bus.getDefault().post(OExitEv.getSucc());

        }
        @Subscribe(event = SDKEventKey.ON_EXIT_CANCELED)
        private void onExitCanceled() {

        }
    };
}
