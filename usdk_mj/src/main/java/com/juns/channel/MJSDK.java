package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import fusion.mj.communal.utils.various.FLogger;
import fusion.mj.communal.utils.various.GsonUtil;
import fusion.mj.means.callback.FusionsdkListener;
import fusion.mj.means.proxy.FusionCommonSdk;
import fusion.mj.means.proxy.FusionPayParams;
import fusion.mj.means.proxy.FusionUserExtraData;

public class MJSDK extends OPlatformSDK {
    private static final String TAG = "MJSDK";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public MJSDK(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(final Activity activity) {
        FusionCommonSdk.getInstance().FusionInit(activity, new FusionsdkListener() {
            @Override
            public void initSuc(String s) {
                Bus.getDefault().post(OInitEv.getSucc());
            }

            @Override
            public void initFailed(String s) {
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onLoginSuc(String data) {
                FLogger.e(TAG, "收到回调登录成功 " + data);
                try {
                    JSONObject jsonObject = new JSONObject(data);

                    String cur_uid = jsonObject.getString("userId");//服务端登录验证使用,账号唯一标识,  建议和渠道id拼接作为唯一标识,避免多渠道渠道userId重复.某些渠道此字段可能有点长,建议留100位保存.
                    String new_sign = jsonObject.getString("new_sign");//服务端登录验证使用
                    String timestamp = jsonObject.getString("timestamp");//服务端登录验证使用
                    String cp_ext = jsonObject.getString("cp_ext");//服务端登陆验证使用,这是一个json,  目前写死为{"test":"test"}
                    String guid = jsonObject.getString("guid");//服务端登陆验证使用
                    String channelId = jsonObject.getString("channelId");//渠道id  如联想为15   具体参见<各渠道参数>
                    String channelVersion = jsonObject.getString("channelVersion");//渠道版本
                    String packageId = jsonObject.getString("packageId");//包id.对应一个游戏安装包.
                    String age = jsonObject.getString("age");//年龄
                    String realNameStatus = jsonObject.getString("realNameStatus");//实名状态 0：未实名：1：已实名
                    String isVisitors = jsonObject.getString("isVisitors");//是否游客 0：不是 ，1:游客
                    //上面的参数,每次登录成功一定会返回。所以可用getString方法获取.
                    //userId = cur_uid;
                    login2RSService(cur_uid,new_sign,timestamp,cp_ext,guid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoginFailed(String s) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onLogout(boolean b) {
                Bus.getDefault().post(new OLogoutEv());
            }

            @Override
            public void onPaySuc(String s) {
                Bus.getDefault().post(OPayEv.getSucc(s));
            }

            @Override
            public void onPayFail(String s) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,s));
            }

            @Override
            public void onExit() {
                FusionCommonSdk.getInstance().FusionSubmitData(activity, createUserPrams(FusionUserExtraData.TYPE_EXIT_GAME,submitInfo));
                Bus.getDefault().post(OExitEv.getSucc());
            }

            @Override
            public void onResult(int i, String s) {

            }
        });
        //Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FusionCommonSdk.getInstance().FusionLogin(activity);
            }
        });
    }
    private void login2RSService(String cur_uid,String new_sign,String timestamp,String cp_ext,String guid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", cur_uid);
            dataJson.put("timestamp", timestamp);
            dataJson.put("cp_ext", cp_ext);
            dataJson.put("guid", guid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(new_sign, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {
        FusionCommonSdk.getInstance().FusionLogout(mainAct, false);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        FusionCommonSdk.getInstance().FusionPay(activity, createPayParams(payParams));
    }
    private FusionPayParams createPayParams(HashMap<String, String> payHParams) {

        logger.print("guotest"+payHParams.toString()+"");
        FusionPayParams payParams = new FusionPayParams();
        try {
            JSONObject data = new JSONObject(payHParams.get(JunSConstants.PAY_M_DATA));
            payParams.setCallBackUrl(data.getString("callbackurl"));
            payParams.setProductId(data.getString("productId"));
            payParams.setExtension(data.getString("payExt"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        payParams.setBuyNum(1);// 一般只买一份
        // cp测试回调写 http://api.qianxi5.com/ok.php

        int money = (int)Float.parseFloat(payHParams.get(JunSConstants.PAY_MONEY))*100;
        payParams.setOrderId(payHParams.get(JunSConstants.PAY_M_ORDER_ID));//cp的订单id
        payParams.setPartyName(submitInfo.get(JunSConstants.SUBMIT_PARTY_NAME));
        payParams.setPer_price(money);// 单位为分, 但只支持元为单位,所以传100的整数
        payParams.setProductDesc(payHParams.get(JunSConstants.PAY_M_ORDER_ID));
        payParams.setProductName(payHParams.get(JunSConstants.PAY_ORDER_NAME));
        payParams.setRatio(Integer.parseInt(payHParams.get(JunSConstants.PAY_RATE)));// 1人民币 10个
        //payParams.setRemainCoinNum(5000);// 此玩家钱袋里还有5000个元宝
        payParams.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
        payParams.setRoleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));
        payParams.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        payParams.setServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
        payParams.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
        payParams.setTime(System.currentTimeMillis());// 13位毫秒
        payParams.setTotalPrice(money); // 总价格 = 购买份数 * 单价
        payParams.setVip(submitInfo.get(JunSConstants.SUBMIT_VIP));
        String gsonToString = GsonUtil.GsonToString(payParams);
        logger.print("guotest"+payHParams.toString()+"");
        return payParams;
    }

    HashMap<String, String> submitInfo;

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        this.submitInfo = submitInfo;
        if (submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)) {
            FusionCommonSdk.getInstance().FusionSubmitData(mainAct, createUserPrams(FusionUserExtraData.TYPE_CREATE_ROLE,submitInfo));
        }
        if (submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)) {
            FusionCommonSdk.getInstance().FusionSubmitData(mainAct, createUserPrams(FusionUserExtraData.TYPE_ENTER_GAME,submitInfo));
        }
        if (submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_UPGRADE)) {
            FusionCommonSdk.getInstance().FusionSubmitData(mainAct, createUserPrams(FusionUserExtraData.TYPE_LEVEL_UP,submitInfo));
        }
    }
    // 上报参数
    private FusionUserExtraData createUserPrams(int type,HashMap<String, String> submitInfo) {
        FusionUserExtraData userExtraData = new FusionUserExtraData();
        // 设置上报类型
        userExtraData.setDataType(type);
        userExtraData.setServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//玩家所在服务器的ID
        userExtraData.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));//玩家所在服务器的名称
        userExtraData.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));//玩家角色ID
        userExtraData.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));//玩家角色名称 若无则传"无"
        userExtraData.setRoleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));//玩家角色等级
        //userExtraData.setProfessionId(2);//当前玩家职业id
        //userExtraData.setProfession("无");//当前玩家职业名称 若无，传入"无"
        //userExtraData.setPower(10000);//战斗力
        userExtraData.setVip(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_VIP)));//玩家VIP等级
        //userExtraData.setRoleGender("女");//角色性别 (男或女)
        //userExtraData.setPayTotal(2000);// 累计充值金额 元为单位
        //userExtraData.setRemainCoinNum(5000);//  1级货币余额,此玩家钱袋里还有5000个元宝.
        //userExtraData.setPartyId(1);//帮派id
        userExtraData.setPartyName("无");//帮派名称 若无，传入"无"
        //userExtraData.setPartyRoleId(1);//角色在帮派中的称号id
        userExtraData.setPartyRoleName("无");// 角色在帮派中的称号  若无，传入"无"
        userExtraData.setRoleCreateTime((int) (System.currentTimeMillis() / 1000));//10位 秒

        userExtraData.setBanlance("0");//若无则传0

        userExtraData.setFriendShip("无");//若无好友关系则传无

        /* 以上都为常规参数,各个上报事件都需要传. 选择服务器,创角的时候肯定很多参数是没有的，那么只传已经存在参数 */
        if(type == FusionUserExtraData.TYPE_LEVEL_UP){
            userExtraData.setRoleLevelUpTime((int) (System.currentTimeMillis() / 1000));// 10位秒
        }


        String gsonToString = GsonUtil.GsonToString(userExtraData);
        FLogger.d(TAG, "上报参数:" + gsonToString);
        return userExtraData;
    }

    @Override
    public void exitGame(Activity activity) {
        FusionCommonSdk.getInstance().FusionExit(activity);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        FusionCommonSdk.getInstance().onActivityResult(mainAct, requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        FusionCommonSdk.getInstance().onConfigurationChanged(mainAct, newConfig);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        FusionCommonSdk.getInstance().onStart(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        FusionCommonSdk.getInstance().onPause(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        FusionCommonSdk.getInstance().onResume(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        FusionCommonSdk.getInstance().onNewIntent(mainAct, data);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        FusionCommonSdk.getInstance().onRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        FusionCommonSdk.getInstance().onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        FusionCommonSdk.getInstance().onDestroy(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        FusionCommonSdk.getInstance().onRestart(mainAct);
    }

}
