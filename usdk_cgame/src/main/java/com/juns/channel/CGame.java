package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.cgamex.usdk.api.CGamexSDK;
import com.cgamex.usdk.api.GameInfo;
import com.cgamex.usdk.api.IEventHandler;
import com.cgamex.usdk.api.IExitGameListener;
import com.cgamex.usdk.api.PayParams;
import com.cgamex.usdk.api.SDKConfig;
import com.cgamex.usdk.api.UserInfo;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CGame extends OPlatformSDK {
    private static final String TAG = "CGame";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public CGame(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        CGamexSDK.onCreate(mainAct);
    }

    @Override
    public void init(Activity activity) {
        SDKConfig sdkConfig = new SDKConfig();
        // 必填参数，该参数请找运营申请
        sdkConfig.setAppId(Integer.parseInt(SDKApplication.getPlatformConfig().getExt1()));
        // 必填参数，该参数请找运营申请
        sdkConfig.setAppKey(SDKApplication.getPlatformConfig().getExt2());
        // 设置横竖屏，横屏：SDKConfig.LANDSCAPE，竖屏SDKConfig.PORTRAIT
        if(SDKApplication.getPlatformConfig().getExt1().equals("1")){
            sdkConfig.setOrientation(SDKConfig.PORTRAIT);
        }else {
            sdkConfig.setOrientation(SDKConfig.LANDSCAPE);
        }

        // SDK初始化(如果你的游戏是H5游戏，并且采用H5接入方式时，只需传入mIEventHandler，不需要对handleEvent回调事件进行处理)
        boolean init = CGamexSDK.init(sdkConfig, mIEventHandler);
        if(init){
            Bus.getDefault().post(OInitEv.getSucc());
        }else{
            Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
        }
    }
    // sdk事件回调接口，如果你的游戏是原生的（非H5游戏），需要处理该回调，H5游戏则不需要
    private IEventHandler mIEventHandler = new IEventHandler() {
        @Override
        public void handleEvent(int eventCode, Bundle extra) {
            switch (eventCode) {
                case IEventHandler.EVENT_LOGIN_SUCCESS:
                    // 登录成功回调
                    UserInfo user = (UserInfo) extra.getSerializable(IEventHandler.KEY_USER);
                    // 用户id，可作为用户唯一标识，注意：不能用username作为唯一标识，它有可能会被修改
                    String userId = user.getUserId();
                    // token，sdk登录后，CP可以用它来进行身份二次验证，token验证接口查看服务端对接文档
                    String token = user.getToken();
                    login2RSService(userId,token);
                   // Log.d(TAG, "登录成功. userid=" + userId + ", token=" + token);
                    break;
                case IEventHandler.EVENT_PAY_SUCCESS:
                    // 支付成功回调
                    // 充值成功后，强烈建议使用游戏服务端及时主动通知游戏客户端的方案刷新到账信息，而非依赖SDK回调再拉取刷新
                    String orderid = extra.getString(IEventHandler.KEY_OUT_ORDER_ID);
                    Bus.getDefault().post(OPayEv.getSucc(orderid));
                   // Log.d(TAG, "支付成功，orderid=" + orderid);
                    break;
                case IEventHandler.EVENT_PAY_FAILED:
                    // 支付失败回调
                    String errorMsg = extra.getString(IEventHandler.KEY_MSG);
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,errorMsg));
                   // Log.d(TAG, "支付失败，errorMsg=" + errorMsg);
                    break;
                case IEventHandler.EVENT_PAY_CANCEL:
                    // 支付取消回调
                 //   Log.d(TAG, "支付取消");
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
                    break;
                case IEventHandler.EVENT_ACCOUNT_LOGOUT:
                    // 注销账号回调，可以在这里跳到游戏登录界面，并重新调用sdk的login接口，让用户重新登录
                   // Log.d(TAG, "注销账号");
                  //  Toast.makeText(getBaseContext(), "账号已注销，请重新登录", Toast.LENGTH_SHORT).show();
                    // TODO 跳到游戏登录界面，重新调用sdk的login接口
                    exitGameInfo = new GameInfo();
                    Bus.getDefault().post(new OLogoutEv());
                    break;
            }
        }
    };
    @Override
    public void login(Activity activity) {
        CGamexSDK.login(activity);
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
    GameInfo exitGameInfo = new GameInfo();
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            GameInfo gameInfo = new GameInfo();
            gameInfo.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            gameInfo.setRoldName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            gameInfo.setRoleLevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
            gameInfo.setServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
            gameInfo.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
            exitGameInfo = gameInfo;
            CGamexSDK.submitGameInfo(gameInfo);
        }
    }

    @Override
    public void logout(Activity mainAct) {
        CGamexSDK.logout(mainAct);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        // 支付
        PayParams payParam = new PayParams();
        // 价格，单位：元
        int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        payParam.setPrice(money);
        payParam.setOrderId(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        payParam.setServerId(payParams.get(JunSConstants.PAY_SERVER_ID));
        payParam.setServerName(payParams.get(JunSConstants.PAY_SERVER_NAME));
        // 角色ID，不能为空
        payParam.setRoleId(payParams.get(JunSConstants.PAY_ROLE_ID));
        payParam.setRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));
        payParam.setRoleLevel(payParams.get(JunSConstants.PAY_ROLE_LEVEL));
        payParam.setExt1(payParams.get(JunSConstants.PAY_EXT));
        CGamexSDK.pay(activity, payParam);
    }

    @Override
    public void exitGame(Activity activity) {
        CGamexSDK.exit(activity, exitGameInfo, new IExitGameListener() {
            @Override
            public void onSdkExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        CGamexSDK.onStart(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        CGamexSDK.onResume(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        CGamexSDK.onRestart(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        CGamexSDK.onNewIntent(mainAct,data);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        CGamexSDK.onPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        CGamexSDK.onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        CGamexSDK.onDestroy(mainAct);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        CGamexSDK.onActivityResult(mainAct,requestCode,resultCode,data);
    }
}
