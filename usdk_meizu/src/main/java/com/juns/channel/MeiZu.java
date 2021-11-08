package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.flow.MPayFlow;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.meizu.gamesdk.model.callback.MzExitListener;
import com.meizu.gamesdk.model.callback.MzLoginListener;
import com.meizu.gamesdk.model.callback.MzPayListener;
import com.meizu.gamesdk.model.model.GameRoleInfo;
import com.meizu.gamesdk.model.model.LoginResultCode;
import com.meizu.gamesdk.model.model.MzAccountInfo;
import com.meizu.gamesdk.model.model.MzBuyInfo;
import com.meizu.gamesdk.model.model.PayResultCode;
import com.meizu.gamesdk.online.common.exception.ParamsException;
import com.meizu.gamesdk.online.core.MzGameCenterPlatform;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MeiZu extends OPlatformSDK {
    private static final String TAG = "MeiZu";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;
    private String uid;
    public MeiZu(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mContext = activity;
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        meizuDoLogin();
    }

    @Override
    public void logout(Activity mainAct) {
        MzGameCenterPlatform.logout(mainAct,new MzLoginListener(){
            @Override
            public void onLoginResult(int code, MzAccountInfo mzAccountInfo, String msg) {
                //TODO 在这里处理登出逻辑
                //Bus.getDefault().post(OLoginEv.getSucc(mzAccountInfo.getUid()));
            }
        });
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        doPay(activity, payParams, payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
    }

    @Override
    public void exitGame(Activity activity) {
        doExitGame();
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        if (!submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)) {
            sendRoleInfo(submitInfo);
        }
    }

    @Override
    public String prePay(Activity mainAct) {

        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("puid", uid);
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void meizuDoLogin() {
        MzGameCenterPlatform.login((Activity) mContext, new MzLoginListener() {
            @Override
            public void onLoginResult(int code, MzAccountInfo accountInfo, String errorMsg) {
                switch(code){
                    case LoginResultCode.LOGIN_SUCCESS:
                        // TODO 登录成功，拿到uid 和 session到自己的服务器去校验session合法性
                        uid = accountInfo.getUid();
                        String seesion = accountInfo.getSession();
                        String name = accountInfo.getName();
                        login2RSService(uid,name,seesion);
                        break;
                    case LoginResultCode.LOGIN_ERROR_CANCEL:
                        // TODO 用户取消登陆操作
                        Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL, errorMsg));
                        break;
                    default:
                        // TODO 登陆失败，包含错误码和错误消息。
                        // TODO 注意，错误消息(errorMsg)需要由游戏展示给用户，提示失败原因
//                        displayMsg("登录失败 : " + errorMsg + " , code = " + code);
                        Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, errorMsg));
                        break;
                }
            }
        });
    }



    private void login2RSService(String uid, String name,String seesion) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
            dataJson.put("puname", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(seesion, dataJson.toString());
    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {


        int roleLevel = 1;
        try {
            roleLevel = Integer.parseInt(params.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


        GameRoleInfo gameRoleInfo = new GameRoleInfo()
                .setRoleId(params.get(JunSConstants.SUBMIT_ROLE_ID)) //设置游戏角色 ID, 必填项
                .setRoleName(params.get(JunSConstants.SUBMIT_ROLE_NAME)) //设置游戏角色名称, 必填项
                .setRoleZone(params.get(JunSConstants.SUBMIT_SERVER_NAME)) //设置角色区服-若无区服则提交 0，选填
                .setRoleLevel(roleLevel) //设置游戏角色等级，选填
              //  .setRoleTmTotal(100) //设置游戏角色在线时长(分钟)，选填
              //  .setUserTmTotal(1000) //设置玩家该游戏在线时长（分钟），选填
                .setRoleVip(Integer.parseInt(params.get(JunSConstants.SUBMIT_VIP))); //设置游戏玩家 Vip 等级，选填
               // .setGangsFlag(1) //设置游戏玩家是否参加了帮派(0 否，1 是)，选填
               // .setRechargeTimes(5) //设置该玩家角色在游戏内充值的次数，选填
              //  .setRechargeFlag(0) //设置该玩家角色是否首冲(0 否，1 是)，选填
               // .setRolePower(10000); //设置游戏内角色战力，选填
        try {
            MzGameCenterPlatform.submitRoleInfo((Activity) mContext, gameRoleInfo.toBundle());
        } catch (ParamsException e) {
            //参数异常
            e.printStackTrace();
        }

    }

    /**
     * 退出游戏
     */
    private void doExitGame() {
        MzGameCenterPlatform.exitSDK((Activity) mContext, new MzExitListener() {
            public void callback(int code, String msg) {
                if (code == MzExitListener.CODE_SDK_EXIT) {
                    //TODO 在这里处理退出逻辑
                    Bus.getDefault().post(OExitEv.getSucc());
                } else if (code == MzExitListener.CODE_SDK_CONTINUE) {
                    //TODO 继续游戏
                }
            }
        });


    }

    private void doPay(Context context, HashMap<String, String> payParams,
                       String moid, String mData) {
      //  Log.e("payinfo",payParams.toString());

        if (!TextUtils.isEmpty(mData)) {
            JSONObject data;
            try {
                data = new JSONObject(mData);
                String orderId = moid; // cp_order_id (不能为空)
                String sign = data.getString("sign"); // sign (不能为空)
                String signType = data.getString("sign_type"); // sign_type (不能为空)
                int buyCount = Integer.parseInt(data.getString("buy_amount")); // buy_amount
                String cpUserInfo = data.getString("user_info"); // user_info
                String amount = data.getString("product_per_price"); // total_price
                String productId = data.getString("product_id"); // product_id
                String productSubject = data.getString("product_subject"); // product_subject
                String productBody = data.getString("product_body");// product_body
                String productUnit = data.getString("product_unit");// product_unit
                String appid = data.getString("app_id"); // app_id (不能为空)
                String uid = data.getString("uid"); // uid (不能为空)flyme账号用户ID
                String perPrice = data.getString("product_per_price"); // product_per_price
                long createTime = data.getLong("create_time"); // create_time
                int payType = data.getInt("pay_type"); //pay_type：0-定额；1-不定额
                Bundle buyBundle = new MzBuyInfo().setBuyCount(buyCount).setCpUserInfo(cpUserInfo)
                        .setOrderAmount(amount).setOrderId(orderId).setPerPrice(perPrice)
                        .setProductBody(productBody).setProductId(productId)
                        .setProductSubject(productSubject).setProductUnit(productUnit)
                        .setSign(sign).setSignType(signType).setCreateTime(createTime)
                        .setAppid(appid).setUserUid(uid).setPayType(payType).toBundle();




                MzGameCenterPlatform.payOnline((Activity) mContext, buyBundle, new MzPayListener() {

                    @Override
                    public void onPayResult(int code, Bundle info, String errorMsg) {
                                // TODO 支付结果回调，该回调跑在应用主线程。注意，该回调跑在应用主线程，不能在这里做耗时操作
                        switch(code){
                            case PayResultCode.PAY_SUCCESS:
                                // TODO 如果成功，接下去需要到自己的服务器查询订单结果
                               // MzBuyInfo payInfo = MzBuyInfo.fromBundle(arg1);
                                logger.print("MeiZu --> doPay --> Success");
                                Bus.getDefault().post(OPayEv.getSucc("pay success."));
                                break;
                            case PayResultCode.PAY_ERROR_CANCEL:
                                // TODO 用户主动取消支付操作，不需要提示用户失败
                                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));

                                break;
                            default:
                                // TODO 支付失败，包含错误码和错误消息。
                                // TODO 注意，错误消息(errorMsg)需要由游戏展示给用户，提示失败原因

                                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, errorMsg));
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                logger.print("MeiZu --> doPay --> onException");
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR, "支付参数解析有误"));
            }
        } else {
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR, "平台支付参数为空"));
        }
    }

}
