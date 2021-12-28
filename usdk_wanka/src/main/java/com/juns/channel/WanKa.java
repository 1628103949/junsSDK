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
import com.wanyugame.wygamesdk.bean.cp.LoginInfo;
import com.wanyugame.wygamesdk.bean.cp.PaymentInfo;
import com.wanyugame.wygamesdk.bean.cp.RoleInfo;
import com.wanyugame.wygamesdk.common.WyGame;
import com.wanyugame.wygamesdk.result.IResult;
import com.wanyugame.wygamesdk.result.OnExitListener;
import com.wanyugame.wygamesdk.result.SwitchAccountListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WanKa extends OPlatformSDK {
    private static final String TAG = "WanKa";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    private String uid;
    public WanKa(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        WyGame.init(SDKApplication.getPlatformConfig().getExt1(), SDKApplication.getPlatformConfig().getExt2(), activity, new IResult<String>() {
            @Override
            public void onSuccess(String s) {
                Bus.getDefault().post(OInitEv.getSucc());
                WyGame.setSwitchAccountListener(new SwitchAccountListener() {
                    @Override
                    public void onLogout() {
                        Bus.getDefault().post(new OLogoutEv());
                    }
                });
            }

            @Override
            public void onFail(String s) {
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.CHANNEL_ERR,s));
            }
        });
    }

    @Override
    public void onOLogoutEv(OLogoutEv oLogout) {
        super.onOLogoutEv(oLogout);
        WyGame.logout();
    }

    @Override
    public void login(Activity activity) {
        WyGame.login(activity, new IResult<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {
                uid=loginInfo.getUid();
                login2RSService(uid,loginInfo.getToken());
            }

            @Override
            public void onFail(String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR,s));
            }
        });

    }

    private void login2RSService(String uid, String token) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //Log.e("guoinfo",payParams.toString());
        PaymentInfo paymentInfo = new PaymentInfo();
        try {
            JSONObject jsonObject = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            paymentInfo.setSubject(jsonObject.getString("productName"));
            paymentInfo.setSubjectId(jsonObject.getString("productId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        paymentInfo.setServerId(payParams.get(JunSConstants.PAY_SERVER_ID));
        paymentInfo.setRoleId(payParams.get(JunSConstants.PAY_ROLE_ID));
        paymentInfo.setRoleLevel(payParams.get(JunSConstants.PAY_ROLE_LEVEL));
        paymentInfo.setRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));
        paymentInfo.setCpBillNo(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        paymentInfo.setOrderAmount(payParams.get(JunSConstants.PAY_MONEY));
        paymentInfo.setUid(uid);
        paymentInfo.setExtraInfo("");
        WyGame.pay(activity, paymentInfo, new IResult<String>() {
            @Override
            public void onSuccess(String s) {
                Bus.getDefault().post(OPayEv.getSucc(s));
            }

            @Override
            public void onFail(String s) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR,s));
            }
        });

    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setUid(uid);
        roleInfo.setGameServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
        roleInfo.setRoleLev(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        roleInfo.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        roleInfo.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
        WyGame.commitRoleInfo(roleInfo);
    }

    @Override
    public void exitGame(Activity activity) {

        WyGame.exit(activity, new OnExitListener() {
            @Override
            public void onExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });

    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        WyGame.onCreate(mainAct);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        WyGame.onStart(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        WyGame.onResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        WyGame.onPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        WyGame.onStop(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        WyGame.onRestart(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        WyGame.onDestroy(mainAct);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        WyGame.onActivityResult(mainAct,requestCode,resultCode,data);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        WyGame.onNewIntent(mainAct,data);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        WyGame.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        WyGame.onConfigurationChanged(mainAct,newConfig);
    }

}
