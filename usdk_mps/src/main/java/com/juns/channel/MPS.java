package com.juns.channel;

import android.app.Activity;

import com.huosdk.gamesdk.HuoSdkManager;
import com.huosdk.gamesdk.listener.OnInitSdkListener;
import com.huosdk.gamesdk.listener.OnLoginListener;
import com.huosdk.gamesdk.listener.OnLogoutListener;
import com.huosdk.gamesdk.listener.OnPaymentListener;
import com.huosdk.gamesdk.listener.SubmitRoleInfoCallBack;
import com.huosdk.gamesdk.model.CustomPayParam;
import com.huosdk.gamesdk.model.LoginErrorMsg;
import com.huosdk.gamesdk.model.LogincallBack;
import com.huosdk.gamesdk.model.PaymentCallbackInfo;
import com.huosdk.gamesdk.model.PaymentErrorMsg;
import com.huosdk.gamesdk.model.RoleInfo;
import com.huosdk.gamesdk.model.RoleType;
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

public class MPS extends OPlatformSDK {
    private static final String TAG = "MPS";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public MPS(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        HuoSdkManager.getInstance().initSdk(activity, new OnInitSdkListener() {
            @Override
            public void initSuccess(String s, String s1) {
                Bus.getDefault().post(OInitEv.getSucc());
            }

            @Override
            public void initError(String s, String s1) {
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,s1));
            }
        });
        HuoSdkManager.getInstance().addLoginListener(new OnLoginListener() {
            @Override
            public void loginSuccess(LogincallBack logincallBack) {
                login2RSService(logincallBack.mem_id,logincallBack.user_token);
            }

            @Override
            public void loginError(LoginErrorMsg loginErrorMsg) {
                Bus.getDefault().post(OLoginEv.getFail(loginErrorMsg.code,loginErrorMsg.msg));
            }
        });
        HuoSdkManager.getInstance().addLogoutListener(new OnLogoutListener() {
            @Override
            public void logoutSuccess(int i, String s, String s1) {
                Bus.getDefault().post(new OLogoutEv());
            }

            @Override
            public void logoutError(int i, String s, String s1) {

            }
        });

    }

    @Override
    public void login(Activity activity) {
        HuoSdkManager.getInstance().showLogin(true);
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
        HuoSdkManager.getInstance().logout();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRole_id(payParams.get(JunSConstants.PAY_ROLE_ID));
        roleInfo.setRole_level(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_LEVEL)));
        roleInfo.setRole_name(payParams.get(JunSConstants.PAY_ROLE_NAME));
        roleInfo.setRole_vip(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_VIP)));
        roleInfo.setServer_id(payParams.get(JunSConstants.PAY_SERVER_ID));
        roleInfo.setServer_name(payParams.get(JunSConstants.PAY_SERVER_NAME));
        roleInfo.setEvent(RoleType.OTHER);
        CustomPayParam customPayParam = new CustomPayParam();
        customPayParam.setRole(roleInfo);
        //订单号由游戏方生成，必须保证每笔支付的订单是唯一的，不可重复
        customPayParam.setCp_order_id(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        customPayParam.setProduct_price(Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY)));
        JSONObject payJson = null;
        try {
            payJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            customPayParam.setProduct_id(payJson.getString("productId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //customPayParam.setProduct_id("" + (random.nextInt(100) + 100));
        customPayParam.setProduct_name(payParams.get(JunSConstants.PAY_ORDER_NAME));
        customPayParam.setProduct_desc(payParams.get(JunSConstants.PAY_ORDER_NAME));
        //默认为中国货币 CNY
        customPayParam.setCurrency("CNY");
        //透传参数，会在支付回调给游戏服务器时返回
        customPayParam.setExt(payParams.get(JunSConstants.PAY_EXT));
        HuoSdkManager.getInstance().showPay(customPayParam, new OnPaymentListener() {
            @Override
            public void paymentSuccess(PaymentCallbackInfo paymentCallbackInfo) {
                Bus.getDefault().post(OPayEv.getSucc(paymentCallbackInfo.msg));
            }

            @Override
            public void paymentError(PaymentErrorMsg paymentErrorMsg) {
                Bus.getDefault().post(OPayEv.getFail(paymentErrorMsg.code,paymentErrorMsg.msg));
            }
        });
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        RoleInfo roleInfo = new RoleInfo();
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            roleInfo.setEvent(2);
        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            roleInfo.setEvent(1);
        }else{
            roleInfo.setEvent(3);
        }
        roleInfo.setRole_id(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
        roleInfo.setRole_level(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));
        roleInfo.setRole_name(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        roleInfo.setRole_vip(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_VIP)));
        roleInfo.setServer_id(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
        roleInfo.setServer_name(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));

        HuoSdkManager.getInstance().setRole(roleInfo, new SubmitRoleInfoCallBack() {
            @Override
            public void submitSuccess() {
                logger.print("submit success");
            }

            @Override
            public void submitFail(String s) {
                logger.print("submit fail:"+s);
            }
        });
    }

    @Override
    public void exitGame(Activity activity) {
        MPSExitDialog.showExit(activity, new MPSExitDialog.ExitCallback() {
            @Override
            public void toContinue() {

            }

            @Override
            public void toExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        HuoSdkManager.getInstance().recycle();
    }
}
