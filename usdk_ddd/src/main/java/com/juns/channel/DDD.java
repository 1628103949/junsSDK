package com.juns.channel;

import android.app.Activity;

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
import com.yysy.yygamesdk.bean.cp.LoginInfo;
import com.yysy.yygamesdk.bean.cp.PaymentInfo;
import com.yysy.yygamesdk.bean.cp.RoleInfo;
import com.yysy.yygamesdk.common.YyGame;
import com.yysy.yygamesdk.listener.IResult;
import com.yysy.yygamesdk.listener.OnExitListener;
import com.yysy.yygamesdk.listener.SwitchAccountListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DDD extends OPlatformSDK {
    private static final String TAG = "DDD";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private boolean isSwitch = false;
    private String uid = "";
    private RoleInfo roleInfo = new RoleInfo();
    public DDD(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        YyGame.initRequest(activity, new IResult<String>() {
            @Override
            public void onSuccess(String s) {
                //初始化成功回调，请保证拿到初始化成功的回调之后再调用登录
                Bus.getDefault().post(OInitEv.getSucc());
            }
            @Override
            public void onFail(String failMsg) {
                //初始化失败回调
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.SDK_ERR,failMsg));
            }
        });
        YyGame.setSwitchAccountListener(new SwitchAccountListener() {
            @Override
            public void onLogout() {
                isSwitch = true;
                Bus.getDefault().post(new OLogoutEv());
            }
        });
    }

    @Override
    public void login(Activity activity) {
        if(isSwitch){
            YyGame.switchAccount();
        }else {
            YyGame.login(activity, new IResult<LoginInfo>() {
                @Override
                public void onSuccess(LoginInfo loginInfo) {
                    uid = loginInfo.getUid(); //返回的账号的唯一标识
                    login2RSService(loginInfo.getUid(),loginInfo.getToken());
                }
                @Override
                public void onFail(String failMsg) {
                    //登录失败
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.SDK_ERR,failMsg));
                }
            });
        }

    }

    private void login2RSService(String uid,String token) {
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

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderAmount(payParams.get(JunSConstants.PAY_MONEY));            //订单金额（单位：元）           必须字段
        paymentInfo.setSubject(payParams.get(JunSConstants.PAY_ORDER_NAME));              //商品名             必须字段
        paymentInfo.setRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));           //角色名             必须字段
        paymentInfo.setCpBillNo(payParams.get(JunSConstants.PAY_M_ORDER_ID));    //CP订单号           必须字段
        paymentInfo.setUid(uid);                    //登录时成功拿到的UID  必须字段
        paymentInfo.setServerId(payParams.get(JunSConstants.PAY_SERVER_ID));               //区服ID             必须字段
        paymentInfo.setExtraInfo(payParams.get(JunSConstants.PAY_M_ORDER_ID));      //拓展信息            必须字段
        paymentInfo.setRemark("");            //订单备注            必须字段
        paymentInfo.setRoleLevel(payParams.get(JunSConstants.PAY_ROLE_LEVEL));              //角色等级            必须字段
        paymentInfo.setRoleId(payParams.get(JunSConstants.PAY_ROLE_ID));          //角色ID            必须字段
        paymentInfo.setPartyName("帮派");            //帮派               必须字段
        paymentInfo.setServerName(payParams.get(JunSConstants.PAY_SERVER_NAME));         //区服名              必须字段
        YyGame.pay(activity, paymentInfo, new IResult<String>() {
            @Override
            public void onSuccess(String s) {
                //支付成功,此处的支付成功只起通知作用
                //真正的支付成功请以后台回调为准
                Bus.getDefault().post(OPayEv.getSucc(s));
            }
            @Override
            public void onFail(String failMsg) {
                //支付失败
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR,failMsg));
            }
        });
    }

    @Override
    public void exitGame(Activity activity) {
        YyGame.exit(activity, new OnExitListener() {
            @Override
            public void onExit() {
                //CP执行保存账号信息等退出操作
                YyGame.commitRoleInfo(roleInfo);
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        //必传参数
        roleInfo.setUid(uid);                   //登录时成功拿到的UID
        roleInfo.setGameServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));       //游戏的区服ID
        roleInfo.setRoleLev(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));              //角色等级，如果有转生等级，请在对接中与我方核对是否传输转生等级，如果需要传输，按照roleLv=转生等级x10000+角色等级来传，前提是游戏等级不会超过9999，有超过的话可以和我方重新确认规则，有疑问请和我方核对
        roleInfo.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));          //角色名
        roleInfo.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));           //角色ID
        YyGame.commitRoleInfo(roleInfo);
    }
}
