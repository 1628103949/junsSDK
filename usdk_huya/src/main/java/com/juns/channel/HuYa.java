package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.huya.berry.client.HuyaBerry;
import com.huya.berry.client.HuyaBerryConfig;
import com.huya.berry.pay.data.PayShopData;
import com.huya.berry.report.function.ReportInfo;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HuYa extends OPlatformSDK {
    private static final String TAG = "HuYa";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public HuYa(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        HuyaBerry.instance().setBerryEventDelegate(new HuyaBerry.BerryEvent() {
            @Override
            public void onEventCallback(Map<String, String> params) {
                if (params == null) {
                    return;
                }
                logger.print(params.toString());
                String eventType = params.get(HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE);
                switch (eventType) {
                    // 下面所有回调中resultCode为0代表成功
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_INIT:
                        // 表示sdk初始化完成，可以做后续的登录，支付等操作
                        // eg:{eventType=init, resultCode=0, msg=}
                        //Bus.getDefault().post(OInitEv.getSucc());
                        break;
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_LOGIN:
                        // 表示登录回调，返回的msg数据里resultCode为0代表登录成功，可以拿key为msg数据里的
                        // uniconid信息跟游戏服务器校验进行游戏登录操作，msg数据为json格式
                        // eg:{eventType=login, resultCode=0, msg={“unionid”:”xxx”，“accessToken”：“”}
                        if(Integer.parseInt(params.get("resultCode")) == 0){
                            try {
                                JSONObject jsonObject = new JSONObject(params.get("msg"));
                                login2RSService(jsonObject.getString("accessToken"),jsonObject.getString("unionid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        break;
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_LOGOUT:
                        // 表示成功退出登录，可以进行游戏退出操作
                        // eg:{eventType=logout, resultCode=0}
                        Bus.getDefault().post(new OLogoutEv());
                        break;
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_TOURIST_ENTERGAME:
                        // 表示游客点了进入游戏操作，以游客身份登录游戏，可以拿key为msg里unionid和accessToken
                        // 信息跟游戏服务器校验，进行游客登录
                        // eg:{eventType=touristEnterGamet, resultCode=0, msg={“unionid”:”xxx”，“accessToken”：“”}}
                        break;
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_QUERY_CERTIFICATION:
                        // 表示实名认证结果返回，成功返回0，失败为-1
                        // eg;{eventType=queryCertification, resultCode=0}
                        break;
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_CLOSE_LOGIN:
                        // 表示登录面板被手动关闭了，游戏方操作自己的逻辑，比如打开登录时把游戏的登录按钮隐藏了，可以通过这个回调重新显示
                        // eg:{eventType=closeLogin}
                        Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"user cancel"));
                        break;
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_PAY:
                        // 支付结果，成功返回0，失败返回-1，用户取消返回-2，成功会返回orderid
                        // 注意⚠️，这里的支付结果仅仅指sdk完成唤醒支付对应的客户端，具体用户支付结果需要游戏方服务器查询我方，具体见支付接口3.10
                        // eg:{eventType=pay, resultCode=0, msg={“orderId”:”xxx”}}
                        if(Integer.parseInt(params.get("resultCode")) == 0){
                            try {
                                JSONObject jsonObject = new JSONObject(params.get("msg"));
                                Bus.getDefault().post(OPayEv.getSucc(jsonObject.getString("orderId")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"pay fail"));
                        }
                        break;
                    case HuyaBerry.BerryEvent.BERRYEVENT_EVENTTYPE_QUIT:
                        // TODO: 表示已经被防沉迷，需要退出app
                        Bus.getDefault().post(OExitEv.getSucc());
                        break;
                    default:
                        break;
                }
            }
        });
        if(SDKApplication.getPlatformConfig().getExt3().equals("debug")){
            HuyaBerryConfig huyaBerryConfig = new HuyaBerryConfig.Builder()
                    .gameId(SDKApplication.getPlatformConfig().getExt1())  // 后台申请
                    .payAppId(SDKApplication.getPlatformConfig().getExt2())  // 支付模块的appId，由业务方指定
                    //支付是否使用测试环境（正式环境为false）
                    .payDebugMode(true)
                    //登录是否使用测试环境（正式环境为false）
                    .debugMode(true)
                    .landscapeMode(true)
                    //横竖屏设置（默认为竖屏）
                    //.isOpenBugly(true)
                    //是否开启SDK中的bugly（默认开启）
                    .isCanTourisLogin(false)  //是否可以游客登录（默认可以，游戏没有游客登录的填false）
                    .loginClientID(SDKApplication.getPlatformConfig().getExt4())//登录id
                    .loginClientSecret(SDKApplication.getPlatformConfig().getExt5())//登录secret
                    .build();
            HuyaBerry.instance().init(activity.getApplication(),
                    huyaBerryConfig);
        }else{
            HuyaBerryConfig huyaBerryConfig = new HuyaBerryConfig.Builder()
                    .gameId(SDKApplication.getPlatformConfig().getExt1())  // 后台申请
                    .payAppId(SDKApplication.getPlatformConfig().getExt2())  // 支付模块的appId，由业务方指定
                    //支付是否使用测试环境（正式环境为false）
                    .payDebugMode(false)
                    .debugMode(false)  // 一般传false，是否用测试模式（需要有虎牙测试环境）
                    .landscapeMode(true)		//横竖屏设置（默认为竖屏）
                    .isCanTourisLogin(false)  //是否可以游客登录（默认可以，游戏没有游客登录的填false）
                    .loginClientID(SDKApplication.getPlatformConfig().getExt4())//登录id
                    .loginClientSecret(SDKApplication.getPlatformConfig().getExt5())//登录secret
                    .build();
            HuyaBerry.instance().init(activity.getApplication(),
                    huyaBerryConfig);
        }
        Bus.getDefault().post(OInitEv.getSucc());

    }

    @Override
    public void login(Activity activity) {
        HuyaBerry.instance().login(activity);
    }
    private void login2RSService(String token,String uid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }
    @Override
    public void logout(Activity mainAct) {
        HuyaBerry.instance().logout(mainAct);
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        ReportInfo.Builder builder = new ReportInfo.Builder();
        builder.setRoleId(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));//游戏厂商角色id
        builder.setRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));//角色名称
        builder.setRealmId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//区服id
        builder.setRealmName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));//区服名称
        builder.setChapter("");//关卡，注册时为空可以传空
        builder.setRoleLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));//角色等级，注册时为空可以传0
        builder.setCareer("");//角色职业
        builder.setServerId(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//角色服务器id
        builder.setServerName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));//角色服务器名称
        builder.setSdkchannelId("34");//安装包渠道编号
        HuyaBerry.instance().reportRegisterInfo(builder.build());
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print("payParams:"+payParams.toString());
        int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
        PayShopData payShopData = new PayShopData();
        try {
            JSONObject payJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            payShopData.bizSign = payJson.getString("sign");//订单签名，签名规则：SHA256(厂商订单号+游戏ID+支付金额+payappKey)，说明：payappKey由虎牙充值中心分配，请妥善保管。
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //payShopData.bizSign = "testsign";
        payShopData.amount = money;//价格，以分位单位，int值
        payShopData.bizOrderId = payParams.get(JunSConstants.PAY_M_ORDER_ID);//订单id
        payShopData.prodName = payParams.get(JunSConstants.PAY_ORDER_NAME);//订单描述
        HuyaBerry.instance().pay(activity,payShopData);
    }

    @Override
    public void exitGame(Activity activity) {
        HuYaExitDialog.showExit(activity, new HuYaExitDialog.ExitCallback() {

            @Override
            public void toContinue() {
                Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
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
        HuyaBerry.instance().uninit();
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        HuyaBerry.instance().onLoginActivityResult(requestCode,resultCode,data);
    }
}
