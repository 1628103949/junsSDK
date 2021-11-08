package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.jolo.sdk.JoloSDK;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class HTC extends OPlatformSDK {
    private static final String TAG = "HTC";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private String session = "";
    private String userId = "";
    private String order = "";
    private String sign = "";
    private String resultOrder;// 支付回执订单
    private String resultSign;// 支付回执订单签名(公钥验签)
    public HTC(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        JoloSDK.initJoloSDK(activity, PartnerConfig.CP_GAME_CODE);
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        JoloSDK.login(activity);
    }

    @Override
    public void logout(Activity mainAct) {
        //JoloSDK.logout(mainAct);
    }

    @Override
    public String prePay(Activity mainAct) {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("user_code", userId);
            dataJson.put("session_id", session);
            dataJson.put("game_name", PartnerConfig.CP_GAME_NAME);
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        Log.e(TAG, "pay: "+payParams);
        try {
            JSONObject jsonData = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            Order or = new Order();
            //int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
            /* 注意：参数里，不要出现类似“1元=10000个金币”的字段，因为“=”原因，会导致微信支付校验失败 */
            or.setAmount(jsonData.getString("amount")); // 设置支付金额，单位分(里面的值必须是整数型数字)
            or.setGameCode(jsonData.getString("game_code")); // 设置游戏唯一ID,由Jolo提供
            or.setGameName(jsonData.getString("game_name")); // 设置游戏名称
            or.setGameOrderid(jsonData.getString("game_order_id")); // 设置游戏订单号
            or.setNotifyUrl(jsonData.getString("notify_url")); // 设置支付通知
            or.setProductDes(jsonData.getString("productDesc")); // 设置产品描述
            or.setProductID(jsonData.getString("productId")); // 设置产品ID
            or.setProductName(jsonData.getString("productName")); // 设置产品名称
            or.setSession(session); // 设置用户session
            or.setUsercode(userId); // 设置用户ID(userCode)
            order = or.toJsonOrder(); // 生成Json字符串订单
            /**
             * 对订单进行签名加密，为了私钥安全这个方法应当放到CP服务器进行，将签名后的sign下发给客户端
             * 注意：这里使用的是OpenSSL工具生成转码后的私钥（private_key_pkcs8.txt文件内容），不是私钥（private_key.pem）
             *
             */
            //sign = RsaSign.sign(order, PartnerConfig.CP_PRIVATE_KEY_PKCS8); // 签名
            Log.e(TAG, "pay2: "+order);
            Log.e(TAG, "pay2: "+sign);
//        Log.i("test", "order = " + order);
//        Log.i("test", "sign = " + sign);

            /*
             * <<<<<  调用支付api  >>>>>
             *
             * 提交支付 注意：如果对订单签名后的sign由服务器下发，那调起支付时传入的order值应该和上面服务器
             * 用于签名的order订单字符串相同，否则会出现校验失败的情况
             */
            JoloSDK.startPay(activity, order, jsonData.getString("order_sign")); // 启动支付
            /**
             * {"amount":"100","session_id":"82149928","product_id":
             * "No100","product_des":"1元购买10000个金币","game_code":
             * "game10001","product_name":"10000个金币","user_code":
             * "100000961","notify_url":
             * "http:\/\/218.245.6.236:12116\/cpserver\/pay.do",
             * "game_order_id":"1444709354577","game_name":"时空猎人"}
             */
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exitGame(Activity activity) {
        JoloSDK.logout(activity);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            if (requestCode == JoloSDK.PAY_REQUESTCODE) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
            }
            return;
        }

        switch (requestCode) {
            /**
             *  支付的回调
             */
            case JoloSDK.PAY_REQUESTCODE: {
                Bus.getDefault().post(OPayEv.getSucc("pay unknown"));
//                resultOrder = data.getStringExtra(JoloSDK.PAY_RESP_ORDER);
//                resultSign = data.getStringExtra(JoloSDK.PAY_RESP_SIGN);
//                Log.i("test", "resultOrder = " + resultOrder);
//                Log.i("test", "resultSign = " + resultSign);
//                if (RsaSign.doCheck(resultOrder, resultSign, PartnerConfig.JOLO_PUBLIC_KEY)) {
//                    // 校验支付订单后，解析订单内容
//                    ResultOrder or = new ResultOrder(resultOrder);
//                    String joloorderid = or.getJoloOrderID(); // jolo唯一订单号
//                    String amount = or.getRealAmount(); // 用户实际支付的金额
//                    int resultcode = or.getResultCode(); // 返回码, == 200为支付成功
//                    String resultmsg = or.getResultMsg(); // 返回提示信息
//                    Log.i("test", "joloorderid = " + joloorderid);
//                    Log.i("test", "amount = " + amount);
//                    Log.i("test", "resultcode = " + resultcode);
//                    Log.i("test", "resultmsg = " + resultmsg);
//                    Toast.makeText(mainAct, "支付结果签名校验成功" + ",金额 = " + amount + "分", Toast.LENGTH_SHORT).show();
//
//                } else {
//                    Toast.makeText(mainAct, "支付结果签名校验失败", Toast.LENGTH_SHORT).show();
//                }
            }
            break;
            /**
             *  账号登录的回调
             */
            case JoloSDK.ACCOUNT_REQUESTCODE: {
                // 用户账号名
                //String userName = data.getStringExtra(JoloSDK.USER_NAME);
                // 用户账号ID
                userId = data.getStringExtra(JoloSDK.USER_ID);
                // 账号的session，支付时使用
                session = data.getStringExtra(JoloSDK.USER_SESSION);
                // 用户帐号信息签名(公钥验签)，密文，CP对该密文用公钥进行校验
                String accountSign = data.getStringExtra(JoloSDK.ACCOUNT_SIGN);
                // 用户帐号信息，明文，用户加密的字符串
                String account = data.getStringExtra(JoloSDK.ACCOUNT);
                // 实名认证标示 0=实名认证待审核 ，1=实名认证，2=实名认证审核不通过 ,3=未实名认证
               // user_realname_type = data.getIntExtra(JoloSDK.USER_REALNAME_TYPE, 3);
                // 用户实名出身年月日信息，格式： yyyyMMdd
                //user_birth = data.getStringExtra(JoloSDK.USER_INFORMATION_BIRTH);
                // 用户实名使用证件类型：1=非海外用户2=港澳台及海外用户
                //certificate_type = data.getIntExtra(JoloSDK.CERTIFICATE_TYPE, 1);

                //updateUserInfoTV();

                Log.i("test", "account = " + account);
                Log.i("test", "account_sign = " + accountSign);
                // 账号合法性检验，注意：安全性考虑，建议将该代码放到服务器进行校验
                //doCheckSign();
                login2RSService(account,accountSign);
            }
            break;
            /**
             *  账号登出的回调
             */
            case JoloSDK.LOGOUT_REQUESTCODE:
                // 需要对返回码resultCode进行判断，只有resultCode等于-1时才是点击了退出
                if (resultCode == RESULT_OK) {
                    // TODO 这部分代码由CP技术自己定义点击退出后的操作
                    Bus.getDefault().post(OExitEv.getSucc());
                    // 清除DEMO游戏的用户信息
                    //resetUserInfo();
                    //updateUserInfoTV();
                    //Toast.makeText(mainAct, "退出成功", Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                break;
        }
    }
    private void login2RSService(String account,String token) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("account",account);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }
    /**
     * DEMO清除用户信息，注销
     */
    private void resetUserInfo() {
       // userName = null;
        userId = null;
        session = null;
//        user_realname_type = 3;
//        user_birth = null;
//        certificate_type = 1;
    }
}
