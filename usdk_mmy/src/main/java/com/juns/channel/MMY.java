package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
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
import com.juns.sdk.demo.DemoActivity;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.mumayi.paymentmain.business.ILoginCallback;
import com.mumayi.paymentmain.business.ILogoutCallback;
import com.mumayi.paymentmain.business.ITradeCallback;
import com.mumayi.paymentmain.ui.PaymentCenterInstance;
import com.mumayi.paymentmain.ui.pay.MMYInstance;
import com.mumayi.paymentmain.util.PaymentConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MMY extends OPlatformSDK {
    private static final String TAG = "MMY";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public MMY(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(final Activity activity) {
        PaymentCenterInstance.getInstance(activity).initial("9c494f8ce6c5cc2awYhqKW47B0htOTGRkZu7oWMWvBCrshVRdD6YaBRnznDuvRLfDPjbUA","海底寻宝大作战");
        PaymentCenterInstance.getInstance(activity).setTestMode(true);
        // 确保每次在刚进入游戏都会检测本地数据
        // 在调用登陆之前调用
        PaymentCenterInstance.getInstance(activity).findUserInfo();
        PaymentCenterInstance.getInstance(activity).checkFloatPermission();
        // 设置登陆回调
        PaymentCenterInstance.getInstance(activity).setLoginCallBack(new ILoginCallback() {
            @Override
            public void onLoginSuccess(String loginSuccess) {

                if (!TextUtils.isEmpty(loginSuccess)) {

                    JSONObject loginobject = null;
                    try {
                        loginobject = new JSONObject(loginSuccess);
                        String loginState = loginobject.getString(PaymentConstants.LOGIN_STATE);
                        if (loginState != null && loginState.equals(PaymentConstants.STATE_SUCCESS)) {

                            /**  uname:用户名， uid:用户ID
                             * token:是用来服务器验证登录，注册是不是成功，用seesion来解签,解签方法见文档
                             * 所有注册，一键注册，登录的接口成功最后都会走这个回调接口
                             * 在这里进入游戏
                             *   如果token 有加号不能识别 可以考虑使用这个方法：URLDecoder 服务端    URLEncoder   客户端 不要trim 就是去掉首位的空格
                             */

                            String uname = loginobject.getString("uname");
                            String uid = loginobject.getString("uid");
                            String token = loginobject.getString("token");
                            String session = loginobject.getString("session");
//                    boolean isAuthenticated = loginobject.getBoolean("isAuthenticated");
//                    String birthday = loginobject.getString("birthday");
                            login2RSService(token, uid, uname, session);
                        }
                        } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    }
            }
            @Override
            public void onLoginFail(String s, String s1) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,s1));
            }
        });
        PaymentCenterInstance.getInstance(activity).setLogoutCallback(new ILogoutCallback() {
            @Override
            public void onLogoutSuccess(String s) {
                Bus.getDefault().post(new OLogoutEv());
            }

            @Override
            public void onLogoutFail(String s) {

            }
        });
        PaymentCenterInstance.getInstance(activity).setTradeCallback(new ITradeCallback() {
            @Override
            public void onTradeSuccess(String tradeType, int tradeCode, Intent intent) {
                Bundle bundle = intent.getExtras();
                String orderId = bundle.getString("orderId");
                String productName = bundle.getString("productName");
                String productPrice = bundle.getString("productPrice");
                String productDesc = bundle.getString("productDesc");
                if (tradeCode == MMYInstance.PAY_RESULT_SUCCESS) {
                    // 在每次支付回调结束时候，调用此接口判断用户是否完善了资料
                    Bus.getDefault().post(OPayEv.getSucc(productName));
                    PaymentCenterInstance.getInstance(activity).getUsercenterApi(activity).checkUserState(activity);
                    Toast.makeText(activity, productName + "支付成功 支付金额:" + productPrice, Toast.LENGTH_SHORT).show();
                } else if (tradeCode == MMYInstance.PAY_RESULT_FAILED) {
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
                    Toast.makeText(activity, productName + "支付失败 支付金额:" + productPrice, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTradeFail(String s, int i, Intent intent) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }
        });
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        PaymentCenterInstance.getInstance(activity).go2Login(activity);
    }
    private void login2RSService(String token,String uid,String uname,String session) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid", uid);
            dataJson.put("uname", uname);
            dataJson.put("session", session);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {
        //Bus.getDefault().post(new OLogoutEv());
        PaymentCenterInstance.getInstance(mainAct).getUsercenterApi(mainAct).finish();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //Log.e("guoinfo",payParams.get(JunSConstants.PAY_ORDER_NAME)+payParams.get(JunSConstants.PAY_MONEY));
        PaymentCenterInstance.getInstance(activity).getUsercenterApi(activity).pay(activity
                ,payParams.get(JunSConstants.PAY_ORDER_NAME)
                ,payParams.get(JunSConstants.PAY_MONEY)
                ,payParams.get(JunSConstants.PAY_M_ORDER_ID));
    }

    @Override
    public void exitGame(final Activity activity) {
        MMYExitDialog.showExit(activity, new MMYExitDialog.ExitCallback() {
            @Override
            public void toContinue() {
                Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
            }

            @Override
            public void toExit() {
                PaymentCenterInstance.getInstance(activity).getUsercenterApi(activity).finish();
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if (submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)) {
            // 设置角色所在区服
            PaymentCenterInstance.getInstance(mainAct).setUserArea(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
            // 设置角色名
            PaymentCenterInstance.getInstance(mainAct).setUserName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            // 设置角色等级
            PaymentCenterInstance.getInstance(mainAct).setUserLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));
        }

    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        PaymentCenterInstance.getInstance(mainAct).getUsercenterApi(mainAct).showFloat();
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        PaymentCenterInstance.getInstance(mainAct).getUsercenterApi(mainAct).closeFloat();
    }
}
