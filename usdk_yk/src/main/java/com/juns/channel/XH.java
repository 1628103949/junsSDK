package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.xh.libcommon.api.XhImp;
import com.xh.libcommon.listener.IExitListener;
import com.xh.libcommon.listener.IInitListener;
import com.xh.libcommon.listener.ILoginListener;
import com.xh.libcommon.listener.ILogoutListener;
import com.xh.libcommon.listener.IPayListener;
import com.xh.libcommon.model.OrderInfo;
import com.xh.libcommon.model.RoleInfo;
import com.xh.libcommon.model.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class XH extends OPlatformSDK {
    private static final String TAG = "XH";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public XH(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        XhImp.getInstance().onCreate(mainAct,new Bundle());
    }
    @Override
    public void init(Activity activity) {
        XhImp.getInstance().init(activity, new IInitListener() {
            @Override
            public void onInitFailed(String paramString) {
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.CHANNEL_ERR,paramString));
            }

            @Override
            public void onInitSuccess() {
                Bus.getDefault().post(OInitEv.getSucc());
                setListener();
            }
        });
        //Bus.getDefault().post(OInitEv.getSucc());
    }

    private void setListener(){

        XhImp.getInstance().setLoginListener(new ILoginListener() {
            @Override
            public void onLoginCancel() {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
            }

            @Override
            public void onLoginFailed(String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR,s));
            }

            @Override
            public void onLoginSuccess(UserInfo userInfo) {
                login2RSService(userInfo.uid,userInfo.token);
            }
        });

        XhImp.getInstance().setPayListener(new IPayListener() {
            @Override
            public void onCancel(String s) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onFailed(String s, String s1) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR,s1));
            }

            @Override
            public void onSuccess(String s, String s1) {
                Bus.getDefault().post(OPayEv.getSucc(s));
            }
        });

        XhImp.getInstance().setLogoutListener(new ILogoutListener() {
            @Override
            public void onLogoutFailed(String s) {

            }

            @Override
            public void onLogoutSuccess() {
                Bus.getDefault().post(new OLogoutEv());
            }
        });

        XhImp.getInstance().setExitListener(new IExitListener() {
            @Override
            public void onFailed(String s) {

            }

            @Override
            public void onSuccess() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }
    @Override
    public void login(Activity activity) {
        XhImp.getInstance().login(activity);
    }

    private void login2RSService(String uid, String session) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(session, dataJson.toString());

    }

    @Override
    public void logout(Activity mainAct) {
        XhImp.getInstance().logout(mainAct);
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setServer_id(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)); //区服id（必要参数）
            roleInfo.setServer_name(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));//区服名称（必要参数）
            roleInfo.setRole_id(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));//角色id（必要参数）
            roleInfo.setRole_name(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME)); //角色名称（必要参数）
            roleInfo.setRole_level(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)); //角色等级（必要参数）
            roleInfo.setRole_createtime(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME)); //创角时间（必要参数）
            roleInfo.setRole_leveltime(String.valueOf(System.currentTimeMillis() / 100)); //升级时间（必要参数）
            roleInfo.setRole_gender("未知");//角色性别（没有则传：未知）
            roleInfo.setRole_vip(submitInfo.get(JunSConstants.SUBMIT_VIP)); //vip等级（没有则传：0）
            roleInfo.setRole_balance("0");//钱包（没有则传：0）
            roleInfo.setRole_fightvalue("0");//战斗力（没有则传：0）
            roleInfo.setRole_profession("未知");//角色职业（没有则传：未知）
            roleInfo.setRole_partyname("未知");//公会名称（没有则传：未知）
            XhImp.getInstance().createNewRole(mainAct, roleInfo);
        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setServer_id(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)); //区服id（必要参数）
            roleInfo.setServer_name(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));//区服名称（必要参数）
            roleInfo.setRole_id(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));//角色id（必要参数）
            roleInfo.setRole_name(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME)); //角色名称（必要参数）
            roleInfo.setRole_level(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)); //角色等级（必要参数）
            roleInfo.setRole_createtime(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME)); //创角时间（必要参数）
            roleInfo.setRole_leveltime(String.valueOf(System.currentTimeMillis() / 100)); //升级时间（必要参数）
            roleInfo.setRole_gender("未知");//角色性别（没有则传：未知）
            roleInfo.setRole_vip(submitInfo.get(JunSConstants.SUBMIT_VIP)); //vip等级（没有则传：0）
            roleInfo.setRole_balance("0");//钱包（没有则传：0）
            roleInfo.setRole_fightvalue("0");//战斗力（没有则传：0）
            roleInfo.setRole_profession("未知");//角色职业（没有则传：未知）
            roleInfo.setRole_partyname("未知");//公会名称（没有则传：未知）
            XhImp.getInstance().enterGame(mainAct, roleInfo);
        }else{
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setServer_id(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)); //区服id（必要参数）
            roleInfo.setServer_name(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));//区服名称（必要参数）
            roleInfo.setRole_id(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));//角色id（必要参数）
            roleInfo.setRole_name(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME)); //角色名称（必要参数）
            roleInfo.setRole_level(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)); //角色等级（必要参数）
            roleInfo.setRole_createtime(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME)); //创角时间（必要参数）
            roleInfo.setRole_leveltime(String.valueOf(System.currentTimeMillis() / 100)); //升级时间（必要参数）
            roleInfo.setRole_gender("未知");//角色性别（没有则传：未知）
            roleInfo.setRole_vip(submitInfo.get(JunSConstants.SUBMIT_VIP)); //vip等级（没有则传：0）
            roleInfo.setRole_balance("0");//钱包（没有则传：0）
            roleInfo.setRole_fightvalue("0");//战斗力（没有则传：0）
            roleInfo.setRole_profession("未知");//角色职业（没有则传：未知）
            roleInfo.setRole_partyname("未知");//公会名称（没有则传：未知）
            XhImp.getInstance().roleLevelUp(mainAct, roleInfo);
        }

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setServer_id(payParams.get(JunSConstants.PAY_SERVER_ID)); //区服id（必要参数）
        roleInfo.setServer_name(payParams.get(JunSConstants.PAY_SERVER_NAME));//区服名称（必要参数）
        roleInfo.setRole_id(payParams.get(JunSConstants.PAY_ROLE_ID));//角色id（必要参数）
        roleInfo.setRole_name(payParams.get(JunSConstants.PAY_ROLE_NAME)); //角色名称（必要参数）
        roleInfo.setRole_level(payParams.get(JunSConstants.PAY_ROLE_LEVEL)); //角色等级（必要参数）
        roleInfo.setRole_gender("未知");//角色性别（没有则传：未知）
        roleInfo.setRole_vip(payParams.get(JunSConstants.PAY_ROLE_VIP)); //vip等级（没有则传：0）
        roleInfo.setRole_balance("0");//钱包（没有则传：0）
        roleInfo.setRole_fightvalue("0");//战斗力（没有则传：0）
        roleInfo.setRole_profession("未知");//角色职业（没有则传：未知）
        roleInfo.setRole_partyname("未知");//公会名称（没有则传：未知）
        int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCpOrderId(payParams.get(JunSConstants.PAY_M_ORDER_ID));//cp订单号
        orderInfo.setPrice(money); //订单金额
        orderInfo.setGoodsName(payParams.get(JunSConstants.PAY_ORDER_NAME)); //商品名称
        orderInfo.setExtraParams(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        XhImp.getInstance().pay(activity, orderInfo, roleInfo);

    }

    @Override
    public void exitGame(Activity activity) {
        XhImp.getInstance().exitGame();
    }



    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        XhImp.getInstance().onStart(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        XhImp.getInstance().onRestart(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        XhImp.getInstance().onResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        XhImp.getInstance().onPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        XhImp.getInstance().onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        XhImp.getInstance().onDestroy(mainAct);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        XhImp.getInstance().onConfigurationChanged(mainAct,newConfig);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        XhImp.getInstance().onNewIntent(mainAct,data);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        XhImp.getInstance().onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        XhImp.getInstance().onActivityResult(mainAct,requestCode,resultCode,data);
    }
}
