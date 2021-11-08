package com.juns.channel;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.nubia.componentsdk.constant.ConstantProgram;
import cn.nubia.nbgame.sdk.GameSdk;
import cn.nubia.nbgame.sdk.entities.AppInfo;
import cn.nubia.nbgame.sdk.entities.ErrorCode;
import cn.nubia.nbgame.sdk.interfaces.CallbackListener;

public class Nubia extends OPlatformSDK {
    private static final String TAG = "Nubia";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public Nubia(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(final Activity activity) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppId(Integer.parseInt(SDKApplication.getPlatformConfig().getExt1()));
        appInfo.setAppKey(SDKApplication.getPlatformConfig().getExt2());
        appInfo.setOrientation(Integer.parseInt(SDKApplication.getPlatformConfig().getExt3()));
        appInfo.setCanUseAdjunct(false);
        logger.print("初始化开始");
        GameSdk.initSdk(activity, appInfo, new CallbackListener<Bundle>() {
            @Override
            public void callback(int responseCode, Bundle bundle) {
                logger.print("初始化成功");
                String msg = "";
                if (responseCode == ErrorCode.SUCCESS) {
                    msg = "sdk初始化成功";
                    Bus.getDefault().post(OInitEv.getSucc());
                } else {
                    msg = "sdk初始化失败（" + responseCode + "）";
                }
                logger.print(msg);
                Toast.makeText(activity, msg,Toast.LENGTH_SHORT).show();
            }
        });

    }
    String uid = "";
    @Override
    public void login(Activity activity) {
        if(GameSdk.isLogined()){
            Toast.makeText(activity,"请勿重复登录",Toast.LENGTH_LONG).show();
            return;
        }
        GameSdk.openLoginActivity(activity, new CallbackListener<Bundle>() {
            @Override
            public void callback(int responseCode, Bundle bundle) {
                switch (responseCode) {
                    case ErrorCode.SUCCESS:
                        //登陆成功，拿uid和sessionId去CP服务器完成角色创建或查询等操作
                        //msg = String.format("账号:%s 昵称:%s 登录成功", GameSdk.getLoginGameId(), GameSdk.getNickName());
                        String gameId = GameSdk.getLoginGameId();
                        uid = GameSdk.getLoginUid();
                        String sessionId = GameSdk.getSessionId();
                        login2RSService(gameId,uid,sessionId);
                        break;
                    case ErrorCode.NO_PERMISSION:
                        // Android6.0没允许安装和更新所需权限，需要运行时请求，主要是存储权限

                        Bus.getDefault().post(OLoginEv.getFail(responseCode,"登录需要安装努比亚联运中心服务，无法获得安装和更新需要权限"));
                        break;
                    default:
                        // 登录失败，包含错误码和错误消息
                        Bus.getDefault().post(OLoginEv.getFail(responseCode,"other"));
                        break;
                }

            }
        });
    }
    private void login2RSService(String gameId,String uid,String token ) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid",uid);
            dataJson.put("game_id",gameId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public String prePay(Activity mainAct) {

        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("uid", uid);
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print(payParams.toString());
        try {
            JSONObject dataJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(ConstantProgram.TOKEN_ID, GameSdk.getSessionId());
            map.put(ConstantProgram.UID, GameSdk.getLoginUid());
            map.put(ConstantProgram.APP_ID, SDKApplication.getPlatformConfig().getExt1());
            map.put(ConstantProgram.APP_KEY, SDKApplication.getPlatformConfig().getExt2());
            map.put(ConstantProgram.AMOUNT, dataJson.getString("amount"));
            map.put(ConstantProgram.PRICE, payParams.get(JunSConstants.PAY_MONEY));
            map.put(ConstantProgram.NUMBER, dataJson.getString("number"));
            map.put(ConstantProgram.PRODUCT_NAME, dataJson.getString("product_name"));
            map.put(ConstantProgram.PRODUCT_DES, dataJson.getString("product_des"));


            //由CP服务器返回的订单编号，订单号不能重复，时间戳和订单号并无关联，这里只是为了测试时订单号不重复
           // MD5Signature.doSign(cpOrderId, "红钻", "1", "0.01", GameSdk.getLoginUid(), timeStamp);
            map.put(ConstantProgram.CP_ORDER_ID, dataJson.getString("cp_order_id"));
            // 为了安全性考虑，doSign最好在服务端执行, 时间戳在DATA_TIMESTAMP和签名两个地方传递的必须是一致的
            map.put(ConstantProgram.SIGN, dataJson.getString("sign"));
            map.put(ConstantProgram.DATA_TIMESTAMP, dataJson.getString("data_timestamp")); //时间戳
            map.put(ConstantProgram.GAME_ID, GameSdk.getLoginGameId());
            GameSdk.doPay(activity, map, new CallbackListener<String>() {
                @Override
                public void callback(int responseCode, String s) {
                    switch (responseCode) {
                        case 0:
                            // 支付完成
                            Bus.getDefault().post(OPayEv.getSucc(s));
                            break;
                        case -1:
                            // 本次支付失败
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10001:
                            // 用户取消了本次支付
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10002:
                            // 网络异常
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10003:
                            // 订单结果确认中，建议客户端向自己业务服务器校验支付结果
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10004:
                            // 支付服务正在升级
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10005:
                            // 支付组件安装失败或是未安装
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10006:
                            // 订单信息有误
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10007:
                            // 获取支付渠道失败
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        case 10008:
                            // Android6.0没允许安装和更新所需权限，需要运行时请求，主要是存储权限
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                        default:
                            // 其他所有场景统一处理为支付失败
                            Bus.getDefault().post(OPayEv.getFail(responseCode, s));
                            break;
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void exitGame(Activity activity) {
        NubiaExitDialog.showExit(activity, new NubiaExitDialog.ExitCallback() {
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
}
