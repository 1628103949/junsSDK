package com.juns.channel;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.yaoyue.release.ICallback;
import com.yaoyue.release.YYReleaseSDK;
import com.yaoyue.release.model.GameInfo;
import com.yaoyue.release.model.GamePayInfo;
import com.yaoyue.release.model.UserInfoModel;
import com.yaoyue.release.util.SDKUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JH extends OPlatformSDK {
    private static final String TAG = "JH";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public JH(OPlatformBean pBean) {
        super(pBean);
    }
    private ICallback callback = new ICallback() {
        @Override
        public void initSuccess() {
            //logger.print("初始化成功");
            Bus.getDefault().post(OInitEv.getSucc());
        }

        @Override
        public void loginSuccess(UserInfoModel userInfoModel) {
            //logger.print("游戏fan登录成功");
            login2RSService(userInfoModel.sessionId,userInfoModel.pid,userInfoModel.appId);
        }

        @Override
        public void setGameInfoSuccess(String s) {

        }

        @Override
        public void createRoleSuccess() {

        }

        @Override
        public void logoutSuccess() {
            Bus.getDefault().post(new OLogoutEv());
        }

        @Override
        public void paySuccess(String s) {
            Bus.getDefault().post(OPayEv.getSucc(s));
        }

        @Override
        public void onError(int i, String s) {
            switch (i) {
                case ICallback.INIT:
                    logger.print("初始化失败 " + s);
                    Bus.getDefault().post(OInitEv.getFail(i,s));
                    break;
                case ICallback.LOGIN:
                    logger.print("登陆失败 " + s);
                    Bus.getDefault().post(OLoginEv.getFail(i,s));
                    break;
                case ICallback.CREATE_ROLE:
                    logger.print("创建角色失败 " + s);
                    break;
                case ICallback.UPLOAD_GAME_INFO:
                    logger.print("更新角色信息失败 " + s);
                    break;
                case ICallback.PAY:
                    logger.print("支付失败 " + s);
                    Bus.getDefault().post(OPayEv.getFail(i,s));
                    break;
                default:
                    logger.print("onError type = " + i + "  , message = " + s);
                    break;
            }
        }
        @Override
        public void exitSuccess() {
            Bus.getDefault().post(OExitEv.getSucc());
        }
    };
    @Override
    public void init(Activity activity) {
        YYReleaseSDK.getInstance().showSplash(activity);
        YYReleaseSDK.getInstance().sdkInit(activity,callback);
        //Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        YYReleaseSDK.getInstance().sdkLogin(activity, callback);
    }

    private void login2RSService(String sessionId,String platformId , String appId) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("platformId",platformId);
            dataJson.put("appId",appId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(sessionId, dataJson.toString());
    }
    private GameInfo gameInfo2 = null;
    @Override
    public void logout(Activity mainAct) {
        YYReleaseSDK.getInstance().onSdkLogOut(mainAct, gameInfo2, callback);
    }

    @Override
    public String prePay(Activity mainAct) {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("uid", SDKData.getpUserId());
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print(payParams.toString());
        //所有参数不能为空，否则报错
        try {
            JSONObject jsonData = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            GamePayInfo payInfo = new GamePayInfo();
            GameInfo gameInfo = getGameInfo(payParams);
            payInfo.setExtInfo(jsonData.getString("extInfo"));
            payInfo.setMoney(jsonData.getString("money"));
            payInfo.setNotifyUrl(jsonData.getString("notifyUrl"));
            payInfo.setCpOrderId(jsonData.getString("cpOrderId"));
            payInfo.setProductCount(jsonData.getInt("productCount"));
            payInfo.setProductId(jsonData.getString("productId"));
            payInfo.setProductName(jsonData.getString("productName"));
            payInfo.setRatio(jsonData.getString("ratio"));
            payInfo.setSign(jsonData.getString("sign"));
            YYReleaseSDK.getInstance().doPay(activity, gameInfo, payInfo, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            GameInfo gameInfo = getGameInfo2(submitInfo);
            YYReleaseSDK.getInstance().createRole(mainAct, gameInfo, callback);
        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            GameInfo gameInfo = getGameInfo2(submitInfo);
            YYReleaseSDK.getInstance().setGameInfo(mainAct, gameInfo,true, callback);
        }else{
            GameInfo gameInfo = getGameInfo2(submitInfo);
            YYReleaseSDK.getInstance().levelUpdate(mainAct, gameInfo);
        }

    }

    public GameInfo getGameInfo(HashMap<String, String> payParams) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setRoleId(payParams.get(JunSConstants.PAY_ROLE_ID));
        gameInfo.setRoleLevel(payParams.get(JunSConstants.PAY_ROLE_LEVEL));
        gameInfo.setRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));
        gameInfo.setZoneId(payParams.get(JunSConstants.PAY_SERVER_ID));
        gameInfo.setServerId(payParams.get(JunSConstants.PAY_SERVER_ID));
        gameInfo.setZoneName(payParams.get(JunSConstants.PAY_SERVER_NAME));
        gameInfo.setVipLevel(payParams.get(JunSConstants.PAY_ROLE_VIP));
        return gameInfo;
    }
    public GameInfo getGameInfo2(HashMap<String, String> payParams) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setRoleId(payParams.get(JunSConstants.SUBMIT_ROLE_ID));
        gameInfo.setRoleLevel(payParams.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        gameInfo.setRoleName(payParams.get(JunSConstants.SUBMIT_ROLE_NAME));
        gameInfo.setZoneId(payParams.get(JunSConstants.SUBMIT_SERVER_ID));
        gameInfo.setServerId(payParams.get(JunSConstants.SUBMIT_SERVER_ID));
        gameInfo.setZoneName(payParams.get(JunSConstants.SUBMIT_SERVER_NAME));
        gameInfo.setVipLevel(payParams.get(JunSConstants.SUBMIT_VIP));
        gameInfo2 = gameInfo;
        return gameInfo;
    }
    @Override
    public void exitGame(Activity activity) {
        YYReleaseSDK.getInstance().onSdkExit(activity, gameInfo2, callback);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        YYReleaseSDK.getInstance().onSdkPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        YYReleaseSDK.getInstance().onSdkStop(mainAct);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        YYReleaseSDK.getInstance().onSdkStart(mainAct);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);

        YYReleaseSDK.getInstance().onCreate(mainAct);
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        YYReleaseSDK.getInstance().onSdkResume(activity);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        YYReleaseSDK.getInstance().onRestart(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        YYReleaseSDK.getInstance().onNewIntent(data,mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        YYReleaseSDK.getInstance().onSdkDestory(mainAct);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        YYReleaseSDK.getInstance().onActivityResult(requestCode,resultCode,data,mainAct);
    }
}
