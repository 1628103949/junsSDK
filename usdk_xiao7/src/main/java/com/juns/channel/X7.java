package com.juns.channel;

import android.app.Activity;
import android.util.Log;

import com.game.chijun.UniversalID;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSSdkApi;
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
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.smwl.smsdk.abstrat.SMInitListener;
import com.smwl.smsdk.abstrat.SMLoginListener;
import com.smwl.smsdk.abstrat.SMLoginOutListener;
import com.smwl.smsdk.abstrat.SMMallStatusObserver;
import com.smwl.smsdk.abstrat.SMPayListener;
import com.smwl.smsdk.app.SMPlatformManager;
import com.smwl.smsdk.bean.PayInfo;
import com.smwl.smsdk.bean.RoleInfo;
import com.smwl.smsdk.bean.SMUserInfo;
import com.smwl.smsdk.bean.mall.MallEntryRequestBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class X7 extends OPlatformSDK {
    private static final String TAG = "X7";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private MallEntryRequestBean mallEntryRequestBean= new MallEntryRequestBean();
    public X7(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {

        //Log.e("guoinfo","uuid:"+UniversalID.getUniversalID(activity));
        SMPlatformManager.getInstance().init(activity, SDKApplication.getPlatformConfig().getExt1(), new SMInitListener() {
            @Override
            public void onSuccess() {
                Bus.getDefault().post(OInitEv.getSucc());
            }

            @Override
            public void onFail(String s) {
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }
        });


    }

    @Override
    public void login(Activity activity) {
        SMPlatformManager.getInstance().login(activity, new SMLoginListener() {
            @Override
            public void onLoginSuccess(SMUserInfo smUserInfo) {
             //   logger.print("token"+smUserInfo.tokenkey);
                login2RSService(smUserInfo.tokenkey);
            }

            @Override
            public void onLoginFailed(String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onLoginCancell(String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onLogoutSuccess() {
                super.onLogoutSuccess();
                Bus.getDefault().post(new OLogoutEv());
            }
        });
    }

    private void login2RSService(String token) {
        OPlatformUtils.loginToServer(token);
    }
    @Override
    public void logout(Activity mainAct) {
        SMPlatformManager.getInstance().loginOut();
    }

    @Override
    public void submitInfo(Activity activity, HashMap<String, String> hashMap) {
        super.submitInfo(activity, hashMap);
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setGame_area(hashMap.get(JunSConstants.SUBMIT_SERVER_NAME));
        mallEntryRequestBean.guid = SDKData.getpUserId();
        mallEntryRequestBean.roleCE = "-1";
        mallEntryRequestBean.roleId = hashMap.get(JunSConstants.SUBMIT_ROLE_ID);
        mallEntryRequestBean.roleLevel = hashMap.get(JunSConstants.SUBMIT_ROLE_LEVEL);
        mallEntryRequestBean.roleName = hashMap.get(JunSConstants.SUBMIT_ROLE_NAME);
        mallEntryRequestBean.roleRechargeAmount = "-1";
        mallEntryRequestBean.roleStage = "-1";
        mallEntryRequestBean.serverId = hashMap.get(JunSConstants.SUBMIT_SERVER_ID);
        mallEntryRequestBean.serverName = hashMap.get(JunSConstants.SUBMIT_SERVER_NAME);
        roleInfo.setGame_area_id(hashMap.get(JunSConstants.SUBMIT_SERVER_ID));
        roleInfo.setGame_guid(SDKData.getpUserId());
        roleInfo.setGame_role_id(hashMap.get(JunSConstants.SUBMIT_ROLE_ID));
        roleInfo.setGame_role_name(hashMap.get(JunSConstants.SUBMIT_ROLE_NAME));
        roleInfo.setRoleLevel(hashMap.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        roleInfo.setRoleCE("-1");
        roleInfo.setRoleStage("-1");
        roleInfo.setRoleRechargeAmount("-1");

        SMPlatformManager.getInstance().smAfterChooseRoleSendInfo(activity,roleInfo);
        //Log.e("guoinfo","isshop"+JunSSdkApi.getInstance().isShop());
    }

    @Override
    public String prePay(Activity activity) {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("puid", SDKData.getpUserId());
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        PayInfo payInfo = new PayInfo();
        payInfo.setExtends_info_data(payParams.get(JunSConstants.PAY_EXT));
        payInfo.setGame_area(payParams.get(JunSConstants.PAY_SERVER_NAME));
        payInfo.setGame_guid(SDKData.getpUserId());
        payInfo.setGame_level(payParams.get(JunSConstants.PAY_ROLE_LEVEL));
        payInfo.setGame_orderid(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        payInfo.setGame_role_id(payParams.get(JunSConstants.PAY_ROLE_ID));
        payInfo.setGame_role_name(payParams.get(JunSConstants.PAY_ROLE_NAME));
        JSONObject payJson = null;
        try {
            payJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            payInfo.setGame_sign(payJson.getString("game_sign"));
            payInfo.setGame_price(payJson.getString("money"));
            payInfo.setSubject(payJson.getString("subject"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        payInfo.setNotify_id("-1");
        SMPlatformManager.getInstance().pay(activity, payInfo, new SMPayListener() {
            @Override
            public void onPayFailed(Object o) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"pay fail"));
            }

            @Override
            public void onPaySuccess(Object o) {
                Bus.getDefault().post(OPayEv.getSucc("success"));
            }

            @Override
            public void onPayCancell(Object o) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"pay cancel"));
            }
        });
    }

    @Override
    public void diyEvent(Activity mainAct, String info) {
        super.diyEvent(mainAct, info);
        if(info.equals("小七商城")){
            SMPlatformManager.getInstance().getMallStatus(mallEntryRequestBean, new SMMallStatusObserver() {
                @Override
                public void fail(String s) {

                }

                @Override
                public void succeeded() {

                }

                @Override
                public void parameterError(String s) {

                }
            });
        }
    }

    @Override
    public void exitGame(Activity activity) {
        SMPlatformManager.getInstance().exitApp(new SMLoginOutListener() {
            @Override
            public void loginOutSuccess() {
                SMPlatformManager.getInstance().smExitCurrent();
                Bus.getDefault().post(OExitEv.getSucc());
            }

            @Override
            public void loginOutFail(String s) {

            }

            @Override
            public void loginOutCancel() {

            }
        });
    }
}
