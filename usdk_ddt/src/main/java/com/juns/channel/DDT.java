package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.ddtsdk.KLSDK;
import com.ddtsdk.listener.ApiListenerInfo;
import com.ddtsdk.listener.IDdtListener;
import com.ddtsdk.listener.IKLExitListener;
import com.ddtsdk.listener.InitListener;
import com.ddtsdk.listener.UserApiListenerInfo;
import com.ddtsdk.model.data.LoginMessageInfo;
import com.ddtsdk.model.data.PaymentInfo;
import com.ddtsdk.model.data.RoleData;
import com.juns.sdk.core.api.JunSConstants;
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

public class DDT extends OPlatformSDK {
    private static final String TAG = "DDT";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public DDT(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        KLSDK.getInstance().initInterface(mainAct, Integer.parseInt(SDKApplication.getPlatformConfig().getExt1()), SDKApplication.getPlatformConfig().getExt2(),new InitListener() {
            @Override
            public void Success(String msg) {
                // TODO Auto-generated method stub
                KLSDK.getInstance().setUserListener(new UserApiListenerInfo() {
                    @Override
                    public void onLogout(Object obj) {
                        Bus.getDefault().post(new OLogoutEv());
                        // 切换账号，处理自己的逻辑，比如重新登录，进行选服进入游戏
                    }
                });
            }
            @Override
            public void fail(String msg) {
                // TODO Auto-generated method stub
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,msg));
            }
        });
        KLSDK.getInstance().onCreate(mainAct);
    }

    @Override
    public void init(Activity activity) {
        Bus.getDefault().post(OInitEv.getSucc());

    }

    @Override
    public void login(Activity activity) {
        KLSDK.getInstance().login(activity, Integer.parseInt(SDKApplication.getPlatformConfig().getExt1()), SDKApplication.getPlatformConfig().getExt2(), new IDdtListener<LoginMessageInfo>() {
            @Override
            public void onSuccess(LoginMessageInfo data) {
                // TODO Auto-generated method stub
                if (data != null) {
                    String gametoken = data.getGametoken();
                    String time = data.getTime();
                    String uid = data.getUid();
                    String sessid = data.getSessid();
                    login2RSService(gametoken,time,uid,sessid);
                }
            }
        });
    }

    private void login2RSService(String gametoken,String time,String uid,String sessid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
            dataJson.put("time", time);
            dataJson.put("sessid", sessid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(gametoken, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {
        KLSDK.getInstance().switchAccount();
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            RoleData roleData = new RoleData();
            roleData.setScene_Id("createRole");
            roleData.setRoleid(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            roleData.setRolename(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            roleData.setRolelevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
            roleData.setZoneid(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
            roleData.setZonename(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
            roleData.setBalance("");
            roleData.setVip(submitInfo.get(JunSConstants.SUBMIT_VIP));
            roleData.setPartyname("");
            roleData.setRolectime("");
            roleData.setRolelevelmtime("");
            KLSDK.getInstance().setExtData(mainAct,roleData);
        } else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            RoleData roleData = new RoleData();
            roleData.setScene_Id("enterServer");
            roleData.setRoleid(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            roleData.setRolename(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            roleData.setRolelevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
            roleData.setZoneid(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
            roleData.setZonename(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
            roleData.setBalance("");
            roleData.setVip(submitInfo.get(JunSConstants.SUBMIT_VIP));
            roleData.setPartyname("");
            roleData.setRolectime("");
            roleData.setRolelevelmtime("");
            KLSDK.getInstance().setExtData(mainAct,roleData);
        }else {
            RoleData roleData = new RoleData();
            roleData.setScene_Id("levelUp");
            roleData.setRoleid(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            roleData.setRolename(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            roleData.setRolelevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
            roleData.setZoneid(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
            roleData.setZonename(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
            roleData.setBalance("");
            roleData.setVip(submitInfo.get(JunSConstants.SUBMIT_VIP));
            roleData.setPartyname("");
            roleData.setRolectime("");
            roleData.setRolelevelmtime("");
            KLSDK.getInstance().setExtData(mainAct,roleData);
        }

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //Log.e("guoinfo", payParams.toString());
        int money = (int) Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAppid(Integer.parseInt(SDKApplication.getPlatformConfig().getExt1()));  //游戏id
        paymentInfo.setAppKey(SDKApplication.getPlatformConfig().getExt2());  //游戏密钥
        paymentInfo.setAgent(SDKApplication.getPlatformConfig().getExt3());  //渠道
        paymentInfo.setAmount(money+"");//金额(元)
        paymentInfo.setBillno(payParams.get(JunSConstants.PAY_M_ORDER_ID));//订单主题
        paymentInfo.setExtrainfo(payParams.get(JunSConstants.PAY_M_ORDER_ID));//额外参数
        paymentInfo.setSubject(payParams.get(JunSConstants.PAY_ORDER_NAME));//主题
        paymentInfo.setIstest("0");//是否测试
        paymentInfo.setRoleid(payParams.get(JunSConstants.PAY_ROLE_ID));//
        paymentInfo.setRolename(payParams.get(JunSConstants.PAY_ROLE_NAME));//
        paymentInfo.setRolelevel(payParams.get(JunSConstants.PAY_ROLE_LEVEL));//
        paymentInfo.setServerid(payParams.get(JunSConstants.PAY_SERVER_ID));//
        paymentInfo.setUid("");//
        KLSDK.getInstance().payment(activity, paymentInfo, new ApiListenerInfo() {
            @Override
            public void onSuccess(Object obj) {

                // TODO Auto-generated method stub
                super.onSuccess(obj);
                if (obj != null) {
                    //Log.i("kk", "充值界面关闭" + obj.toString());  //注意：只会返回close字段
                    //回调接口只会返回支付界面关闭状态:close
                }
            }
        });
        Bus.getDefault().post(OPayEv.getSucc("支付状态未知"));

    }

    @Override
    public void exitGame(Activity activity) {
        KLSDK.getInstance().exit(activity, new IKLExitListener() {

            @Override
            public void exitSuccess(String s) {
                Bus.getDefault().post(OExitEv.getSucc());
            }

            @Override
            public void fail(String msg) {
                // TODO Auto-generated method stub
                //退出失败，cp自行处理退出逻辑
            }
        });
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        KLSDK.getInstance().onActivityResult(mainAct, requestCode, resultCode, data);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        KLSDK.getInstance().onDestroy(mainAct);
        super.onDestroy(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        KLSDK.getInstance().onPause(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        KLSDK.getInstance().onRestart(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        KLSDK.getInstance().onResume(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        KLSDK.getInstance().onStop(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        KLSDK.getInstance().onNewIntent(data);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        KLSDK.getInstance().onConfigurationChanged(newConfig);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        KLSDK.getInstance().onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
