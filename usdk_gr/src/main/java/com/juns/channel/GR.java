package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.gaore.game.sdk.GRPayParams;
import com.gaore.game.sdk.GRUserExtraData;
import com.gaore.game.sdk.GrSDKCallBack;
import com.gaore.game.sdk.verify.GRToken;
import com.gaore.mobile.GrAPI;
import com.gaore.mobile.base.GRReturnCode;
import com.gaore.mobile.base.NetReturnCode;
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

public class GR extends OPlatformSDK {
    private static final String TAG = "GR";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public GR(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        GrAPI.getInstance().grSetScreenOrientation(GrAPI.SCREEN_ORIENTATION_AUTO);
        GrAPI.getInstance().grOnCreate(mainAct.getIntent().getExtras());
    }

    @Override
    public void init(Activity activity) {
        GrAPI.getInstance().grInitSDK(activity, new GrSDKCallBack() {
            @Override
            public void onInitResult(int resultCode) {
                if (resultCode == GRReturnCode.GR_COM_PLATFORM_SUCCESS) {
                    Bus.getDefault().post(OInitEv.getSucc());
                } else {
                    Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
                }
            }

            @Override
            public void onLoginResult(GRToken authResult) {
                if (authResult.isSuc()) {
                    //Log.e("guoinfo","login:"+authResult.toString());
                    login2RSService(authResult.getToken(),authResult.getUserID()+"");
                } else {
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
                }
            }

            @Override
            public void onLogoutResult(int resultCode) {
                if (resultCode == NetReturnCode.LOGOUT_ACCOUNT_SUCCESS) {
                    Bus.getDefault().post(new OLogoutEv());
                }
            }

            @Override
            public void onPayResult(int result) {
                if (result == NetReturnCode.PAY_GAORE_SUCCESS) {
                    Bus.getDefault().post(OPayEv.getSucc("pay success"));
                } else if (result == NetReturnCode.PAY_GAORE_FAILED) {
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.PAY_UNKNOWN,"fail"));
                }else if (result == NetReturnCode.PAY_GAORE_FINISH) {
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.PAY_UNKNOWN,"cancel"));
                }
            }

            @Override
            public void onExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }

        });
        //Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        GrAPI.getInstance().grLogin(activity);
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
    public void logout(Activity mainAct) {
        GrAPI.getInstance().grLogout(mainAct);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        int money = (int) Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        GRPayParams params = new GRPayParams();
        params.setBuyNum(1);	//写默认1
        params.setCoinNum(100);  //写默认100
        params.setExtension(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        params.setPrice(money); 		//单位是元
        params.setProductId(payParams.get(JunSConstants.PAY_MONEY));
        params.setProductName(payParams.get(JunSConstants.PAY_ORDER_NAME));
        params.setProductDesc(payParams.get(JunSConstants.PAY_ORDER_NAME));
        params.setRoleId(payParams.get(JunSConstants.PAY_ROLE_ID));
        params.setRoleLevel(Integer.parseInt(payParams.get(JunSConstants.PAY_ROLE_LEVEL)));
        params.setRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));
        params.setServerId(payParams.get(JunSConstants.PAY_SERVER_ID));
        params.setServerName(payParams.get(JunSConstants.PAY_SERVER_NAME));
        params.setVip(payParams.get(JunSConstants.PAY_ROLE_VIP));
        GrAPI.getInstance().grPay(activity, params);
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        GRUserExtraData extraData = new GRUserExtraData ();
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            extraData.setDataType(2);
        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            extraData.setDataType(3);
        }else {
            extraData.setDataType(4);
        }
        extraData.setServerID(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)); // 未获取到服务器时传0
        extraData.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME)); // 未获取到服务器名称时传null
        extraData.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME)); // 角色未获取或未创建时传null
        extraData.setRoleLevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)); // 当前角色等级,未获取到角色等级时传null
        extraData.setRoleID(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID)); // 当前角色id,未获取角色id时传null
        extraData.setOrderId(null);// 订单id，未获取时传null
        extraData.setMoney(0);// 充值金额，单位分，未获取时传0
        extraData.setMoneyNum(0 + ""); // 玩家身上元宝数量，拿不到或者未获取时传0
        extraData.setRoleCreateTime(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME))); // 角色创建时间，未获取或未创建角色时传0
        extraData.setGuildId(null);// 公会id，无公会或未获取时传null
        extraData.setGuildName(null);// 公会名称，无公会或未获取时传null
        extraData.setGuildLevel(0 + "");// 公会等级，无公会或未获取时传0
        extraData.setGuildLeader(null);// 公会会长名称，无公会或未获取时传null
        extraData.setPower(0);// 角色战斗力, 不能为空，必须是数字，不能为null,若无,传0
        extraData.setProfessionid(0);//职业ID，不能为空，必须为数字，若无，传入 0
        extraData.setProfession("无");//职业名称，不能为空，不能为 null，若无，传入 “无”
        extraData.setGender("无");//角色性别，不能为空，不能为 null，可传入参数“ 男、女、无”
        extraData.setProfessionroleid(0);//职业称号ID，不能为空，不能为 null，若无，传入 0
        extraData.setProfessionrolename("无");//职业称号，不能为空，不能为 null，若无，传入“ 无”
        extraData.setVip(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_VIP)));//玩家VIP等级，不能为空，必须为数字,若无，传入 0
        GrAPI.getInstance().grSubmitExtendData(mainAct,
                extraData);
    }

    @Override
    public void exitGame(Activity activity) {
        GrAPI.getInstance().grExit(activity);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        GrAPI.getInstance().grOnStart();
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        GrAPI.getInstance().grOnPause();
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        GrAPI.getInstance().grOnResume();
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        GrAPI.getInstance().grOnNewIntent(data);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        GrAPI.getInstance().grOnStop();
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        GrAPI.getInstance().grOnRestart();
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        GrAPI.getInstance().grOnConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        GrAPI.getInstance().grOnActivityResult(requestCode,resultCode,data,mainAct);
    }

}
