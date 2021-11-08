package com.juns.channel;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

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
import com.ld.sdk.LdGameInfo;
import com.ld.sdk.LdInfo;
import com.ld.sdk.LdPayInfo;
import com.ld.sdk.LdSdkManger;
import com.ld.sdk.account.api.EntryCallback;
import com.ld.sdk.account.api.ExitCallBack;
import com.ld.sdk.account.api.InitCallBack;
import com.ld.sdk.account.api.LoginCallBack;
import com.ld.sdk.account.api.PayCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LeiDian extends OPlatformSDK {
    //private static final String TAG = "LeiDian";
    private static final String TAG = "LeiDian";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    //接入参数由雷电提供的


    public LeiDian(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(final Activity activity) {
        //note：1 初始化接口
        LdInfo info = new LdInfo();
        info.gameId = SDKApplication.getPlatformConfig().getExt1();
        info.channel = SDKApplication.getPlatformConfig().getExt2();
        info.sunChannel = SDKApplication.getPlatformConfig().getExt3();
        info.appSecret = SDKApplication.getPlatformConfig().getExt4();
        //Bus.getDefault().post(OInitEv.getSucc());
        LdSdkManger.getInstance().init(activity, info, new InitCallBack() {

            @Override
            public void callback(int code, String desc) {
                if(code==0){
                    Bus.getDefault().post(OInitEv.getSucc());
                }else {
                    Bus.getDefault().post(OInitEv.getFail(code,desc));
                }
            }
        });

    }
    private String puid = "";
    @Override
    public void login(final Activity activity) {
        LdSdkManger.getInstance().showLoginView(activity, new LoginCallBack() {

            public void callback(int code, String uid, String timestamp, String sign, String decs) {
                switch (code){
                    case 0:
                        login2RSService(uid,timestamp,sign);
                        puid = uid;
                        break;
                    case 1:
                        Bus.getDefault().post(OLoginEv.getFail(code,decs));
                        break;
                    case 2:
                        Bus.getDefault().post(new OLogoutEv());
                        break;
                }
//                String result = "demo:登录返回--\ncode:" + code + "\nuid: " + uid + "\ntimestamp:" + timestamp + "\nsign:" + sign + "\ndesc:" + decs;
//                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void login2RSService(String uid, String timestamp,String sign) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("useruid", uid);
            dataJson.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(sign, dataJson.toString());
    }
    HashMap<String, String> roleInfo = null;
    @Override
    public void submitInfo(final Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            roleInfo = submitInfo;
            LdGameInfo ldGameInfo = new LdGameInfo();
            ldGameInfo.uid = puid;
            ldGameInfo.serverId = submitInfo.get(JunSConstants.SUBMIT_SERVER_ID);// 服务器id
            ldGameInfo.serverName = submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME);//服务器名字
            ldGameInfo.roleId = submitInfo.get(JunSConstants.SUBMIT_ROLE_ID);// 角色id
            ldGameInfo.roleName = submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME);// 角色名字
            ldGameInfo.roleType = ""; //角色类型，例如：战士，魔法师，弓箭手
            ldGameInfo.level = submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL); //等级
            ldGameInfo.money = ""; //游戏的金币数
            ldGameInfo.partyName = "";//公会

            LdSdkManger.getInstance().enterGame(mainAct, ldGameInfo, new EntryCallback() {
                @Override
                public void callback(int code, String desc) {
//                    String result = "登录角色, code(" + code + ") desc:" + desc;
//                    //Log.i(TAG, result);
//                    Toast.makeText(mainAct, result, Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public void pay(final Activity activity, HashMap<String, String> payParams) {
        LdPayInfo ldPayInfo = new LdPayInfo();
        ldPayInfo.orderId = payParams.get(JunSConstants.PAY_M_ORDER_ID); //游戏的支付订单号
        int money = (int) Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
        //金额和定额产品 2选1
        ldPayInfo.amount = money+"";//支付金额（单位：分）
       // ldPayInfo.productId = "12";  //定额的产品支付
        ldPayInfo.productDesc = payParams.get(JunSConstants.PAY_ORDER_NAME);//商品描述
        ldPayInfo.productName = payParams.get(JunSConstants.PAY_ORDER_NAME);//商品名称
        ldPayInfo.roleId = roleInfo.get(JunSConstants.SUBMIT_ROLE_ID);// 角色id
        ldPayInfo.roleName = roleInfo.get(JunSConstants.SUBMIT_ROLE_NAME);// 角色名字
        ldPayInfo.serverId = roleInfo.get(JunSConstants.SUBMIT_SERVER_ID);// 服务器id
        ldPayInfo.serverName = roleInfo.get(JunSConstants.SUBMIT_SERVER_NAME);//服务器名字
        logger.print(ldPayInfo.toString());
        LdSdkManger.getInstance().showChargeView(activity, ldPayInfo, new PayCallback() {
            @Override
            public void callback(int code, String uid, String billno, String timestamp, String decs){
                switch (code){
                    case 0:
                        Bus.getDefault().post(OPayEv.getSucc(decs));
                        break;
                    case 1:
                        Bus.getDefault().post(OPayEv.getFail(code,decs));
                        break;
                    case 2:
                        Bus.getDefault().post(OPayEv.getFail(code,decs));
                        break;
                    case 3:
                        Bus.getDefault().post(OPayEv.getFail(code,decs));
                        break;
                }
            }
        });
    }

    @Override
    public void exitGame(Activity activity) {
        LdSdkManger.getInstance().showExitView(activity, new ExitCallBack() {
            @Override
            public void onFinish(int state, String s) {
                if (state == 0) {
                    Bus.getDefault().post(OExitEv.getSucc());
                }
            }
        });
    }

    @Override
    public void onResume(Activity mainAct) {
        LdSdkManger.getInstance().showFloatView(mainAct);
        super.onResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        LdSdkManger.getInstance().hideFlowView(mainAct);
        super.onPause(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        LdSdkManger.getInstance().DoRelease(mainAct);
    }
}
