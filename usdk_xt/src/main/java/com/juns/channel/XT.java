package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.gggame.gamesdk.ApiCallback;
import com.gggame.gamesdk.GGGameSDK;
import com.gggame.gamesdk.LogoutCallback;
import com.gggame.gamesdk.SdkExitCallBack;
import com.gggame.gamesdk.bean.PayParams;
import com.gggame.gamesdk.bean.UserExtraData;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class XT extends OPlatformSDK {
    private static final String TAG = "XT";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public XT(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        GGGameSDK.getInstance().init(activity);
        Bus.getDefault().post(OInitEv.getSucc());
        GGGameSDK.getInstance().setOnLogoutCallback(new LogoutCallback() {
            @Override
            public void onLogout() {
                Bus.getDefault().post(new OLogoutEv());
            }
        });
    }

    @Override
    public void login(Activity activity) {
        GGGameSDK.getInstance().login(activity, new ApiCallback() {
            @Override
            public void onSuccess(String s) {
                try {
                    JSONObject data = new JSONObject(s);
                    JSONObject dataUser = new JSONObject(data.getString("data"));
                    //Log.e("guoinfo:",data.toString()+dataUser.toString());
                    login2RSService(dataUser.getString("code"),dataUser.getString("uid"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    private void login2RSService(String token,String uid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        UserExtraData data = new UserExtraData();
        data.serverId = submitInfo.get(JunSConstants.SUBMIT_SERVER_ID);
        data.serverName = submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME);
        data.roleId = submitInfo.get(JunSConstants.SUBMIT_ROLE_ID);
        data.roleName = submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME);
        data.roleLevel = submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL);
        data.updateTime = submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME);
        GGGameSDK.getInstance().submitExtraData(data);
    }

    @Override
    public void logout(Activity mainAct) {
        GGGameSDK.getInstance().logout(new LogoutCallback() {
            @Override
            public void onLogout() {
                Bus.getDefault().post(new OLogoutEv());
            }
        });
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        int money = (int) Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
        PayParams info = new PayParams();
        info.serverId = payParams.get(JunSConstants.PAY_SERVER_ID);
        info.serverName = payParams.get(JunSConstants.PAY_SERVER_NAME);
        info.roleId = payParams.get(JunSConstants.PAY_ROLE_ID);
        info.roleName = payParams.get(JunSConstants.PAY_ROLE_NAME);
        info.roleLevel = payParams.get(JunSConstants.PAY_ROLE_LEVEL);
        info.totalFee = money+"";
        info.ext = payParams.get(JunSConstants.PAY_EXT);
        info.cpOrderNum = payParams.get(JunSConstants.PAY_M_ORDER_ID);
        GGGameSDK.getInstance().pay(activity, info);
    }

    @Override
    public void exitGame(Activity activity) {
        GGGameSDK.getInstance().sdkExit(activity, new SdkExitCallBack() {
            @Override
            public void onSdkExit(int i, String s) {
                if(i == 1){
                    Bus.getDefault().post(OExitEv.getSucc());
                }
            }
        });
    }

    @Override
    public void onStart(Activity mainAct) {
        GGGameSDK.getInstance().onStart();
        super.onStart(mainAct);

    }

    @Override
    public void onResume(Activity mainAct) {
        GGGameSDK.getInstance().onResume();
        super.onResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        GGGameSDK.getInstance().onPause();
        super.onPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        GGGameSDK.getInstance().onStop();
        super.onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        GGGameSDK.getInstance().onDestroy();
        super.onDestroy(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        GGGameSDK.getInstance().onRestart();
        super.onRestart(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        GGGameSDK.getInstance().onNewIntent(data);
        super.onNewIntent(mainAct, data);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        GGGameSDK.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(mainAct, newConfig);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        GGGameSDK.getInstance().onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(mainAct, requestCode, resultCode, data);
    }
}
