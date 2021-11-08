package com.juns.channel;

import android.app.Activity;
import android.content.Intent;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.lexin.sdk.LexinSdk;
import com.lexin.sdk.bean.PayParams;
import com.lexin.sdk.bean.RoleInfo;
import com.lexin.sdk.callback.InitCallback;
import com.lexin.sdk.callback.LoginCallback;
import com.lexin.sdk.callback.LogoutCallback;
import com.lexin.sdk.callback.PayCallback;
import com.lexin.sdk.callback.ReportRoleInfoCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CZ extends OPlatformSDK {
    private static final String TAG = "CZ";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public CZ(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        LexinSdk.getInstance().init(activity, SDKApplication.getPlatformConfig().getExt1(), new InitCallback() {
            @Override
            public void onInitSucceed() {
                LexinSdk.getInstance().setLogoutCallback(new LogoutCallback() {
                    @Override
                    public void onLogout() {
                        Bus.getDefault().post(new OLogoutEv());
                    }
                });
                Bus.getDefault().post(OInitEv.getSucc());
            }

            @Override
            public void onInitFailed(String s) {
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }
        });
    }

    @Override
    public void login(Activity activity) {
        LexinSdk.getInstance().login(new LoginCallback() {
            @Override
            public void onLoginSucceed(String s, String s1) {
                login2RSService(s,s1);
            }

            @Override
            public void onLoginFailed(String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }
        });
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

    @Override
    public void logout(Activity mainAct) {
        LexinSdk.getInstance().logout();
    }

    @Override
    public void submitInfo(Activity mainAct, final HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        LexinSdk.getInstance().setReportRoleInfoCallback(new ReportRoleInfoCallback() {
            @Override
            public RoleInfo getRoleInfo() {
                if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
                    return new RoleInfo.Builder()
                            .reportType(RoleInfo.ReportType.CREATE_ROLE)
                            .roleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID))
                            .roleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME))
                            .roleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)))
                            .serverId(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)))
                            .serverName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME))
                            .build();
                }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
                    return new RoleInfo.Builder()
                            .reportType(RoleInfo.ReportType.ENTER_GAME)
                            .roleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID))
                            .roleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME))
                            .roleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)))
                            .serverId(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)))
                            .serverName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME))
                            .build();
                }else {
                    return new RoleInfo.Builder()
                            .reportType(RoleInfo.ReportType.LEVEL_UP)
                            .roleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID))
                            .roleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME))
                            .roleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)))
                            .serverId(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)))
                            .serverName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME))
                            .build();
                }

            }
        });
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        LexinSdk.getInstance().pay(PayParams.newBuilder()
                .serverId(Integer.parseInt(payParams.get(JunSConstants.PAY_SERVER_ID)))
                .serverName(payParams.get(JunSConstants.PAY_SERVER_NAME))
                .roleId(payParams.get(JunSConstants.PAY_ROLE_ID))
                .roleName(payParams.get(JunSConstants.PAY_ROLE_NAME))
                .productId("1")
                .productName(payParams.get(JunSConstants.PAY_ORDER_NAME))
                .amount(payParams.get(JunSConstants.PAY_MONEY))
                .extension(payParams.get(JunSConstants.PAY_M_ORDER_ID))
                .build(), new PayCallback() {
            @Override
            public void onPaySuccess() {
                Bus.getDefault().post(OPayEv.getSucc("pay success"));
            }

            @Override
            public void onPayFail(String s) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }
        });
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        LexinSdk.getInstance().onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void exitGame(Activity activity) {

    }
}
