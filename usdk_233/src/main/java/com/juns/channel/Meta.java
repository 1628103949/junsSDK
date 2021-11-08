package com.juns.channel;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.callback.JunSPayCallback;

import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;

import com.juns.sdk.core.sdk.SDKData;


import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.meta.android.mpg.account.callback.AccountChangedListener;
import com.meta.android.mpg.account.callback.LoginResultCallback;
import com.meta.android.mpg.account.model.UserInfo;
import com.meta.android.mpg.common.MetaApi;
import com.meta.android.mpg.initialize.InitCallback;
import com.meta.android.mpg.payment.callback.PayResultCallback;
import com.meta.android.mpg.payment.constants.MetaPayResult;
import com.meta.android.mpg.payment.model.PayInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class Meta extends OPlatformSDK {
    private static final String TAG = "Meta";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private HashMap<String, String> payParm;
    private boolean isPay = false;
    public Meta(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        Log.e("guoinfo",System.currentTimeMillis()+"");
        MetaApi.initMetaSdk(activity.getApplication(), SDKApplication.getPlatformConfig().getExt1(), new InitCallback() {
            @Override
            public void onInitializeSuccess() {
                Bus.getDefault().post(OInitEv.getSucc());
                MetaApi.registerAccountChangedListener(mAccountChangedListener);
            }

            @Override
            public void onInitializeFail(int i, String s) {
                Bus.getDefault().post(OInitEv.getFail(i,s));
            }
        });
    }
    private AccountChangedListener mAccountChangedListener = new AccountChangedListener() {
        @Override
        public void onAccountChanged(UserInfo userInfo) {
            Bus.getDefault().post(new OLogoutEv());
            //Log.i("ActivityForTestLoginPay", "账号变更通知=" + userInfo);
            login2RSService(userInfo.uid,userInfo.sid);
        }
    };
    public String cpUid,cpSid;
    @Override
    public void login(Activity activity) {
        MetaApi.login(activity, new LoginResultCallback() {
            @Override
            public void loginSuccess(UserInfo userInfo) {
                login2RSService(userInfo.uid,userInfo.sid);
            }

            @Override
            public void loginFail(int i, String s) {
                Bus.getDefault().post(OLoginEv.getFail(i,s));
            }
        });
    }
    //渠道登录成功后到服务器验证
    private void login2RSService(String uid,String sid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(sid, dataJson.toString());
    }
    @Override
    public void logout(Activity mainAct) {

    }


//    @Override
//    public String firstPay(Activity mainAct, final HashMap<String, String> payParams, final JunSPayCallback junSPayCallback) {
//        final int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
//
//        MetaApi.pay(mainAct,
//                payParams.get(JunSConstants.PAY_ORDER_NAME),
//                payParams.get(JunSConstants.PAY_MONEY),
//                1,
//                money,
//                new PayCallback() {
//                    @Override
//                    public void onPay(String pName, String pCode, int pCount, int price, int pType, String voucherId, final PayParams pay) {
//                        // 请求服务器根据现有参数生成支付相关的完整参数
////                        HashMap<String, String> params = Server.getPayParams(uid, sid, pName, pCode, pCount, price, pType, voucherId);
////                        // 调用填充回调
//
//                        try {
//                            JSONObject dataJson = new JSONObject();
//                            dataJson.put("cpUid", cpUid);
//                            dataJson.put("cpSid", cpSid);
//                            dataJson.put("pName", pName);
//                            dataJson.put("pCode", pCode);
//                            dataJson.put("pCount", pCount);
//                            dataJson.put("price", price);
//                            dataJson.put("pType", pType);
//                            if(voucherId != null){
//                                dataJson.put("voucherId", voucherId);
//                            }else {
//                                dataJson.put("voucherId", "");
//                            }
//                            SDKData.setFirstPayData(dataJson.toString());
//                            junSPayCallback.onFinish(1,"111");
//                            final java.util.Timer timer = new java.util.Timer(true);
//                            TimerTask task = new TimerTask() {
//                                public void run() {
//                                    //每次需要执行的代码放到这里面。
//                                    //当获取成功后释放timer对象
//                                    //int count = 0;
//                                    if(isPay){
//                                        timer.cancel();
//                                        pay.fill(params);
//                                        isPay = false;
//                                    }
//                                    //timer.cancel();
//                                }
//                            };
//                            timer.schedule(task, 0,100);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//
//                    @Override
//                    public void payResult(EventResult eventResult) {
//                        if (eventResult == EventResult.SUCCESS) {
//                            Bus.getDefault().post(OPayEv.getSucc("pay success"));
//                        } else {
//                            Bus.getDefault().post(OPayEv.getFail(eventResult.getCode(),eventResult.getMessage()));
//                        }
//                    }
//                });
//
//        return "233";
//    }


    @Override
    public String prePay(Activity mainAct) {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("apiversion", "1.3.3");
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
//        params = new HashMap<>();
//
//        try {
//            JSONObject mdata = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
//
//            params.put("cpUid", mdata.getString("cpUid"));
//
//            params.put("cpSid", mdata.getString("cpSid"));
//
//            params.put("pName", mdata.getString("productName"));
//
//            params.put("pCode", mdata.getString("productCode"));
//
//            params.put("pCount", mdata.getInt("productCount")+"");
//
//            params.put("amount", mdata.getInt("payAmount")+"");
//
//            params.put("type", mdata.getInt("payType")+"");
//
//            params.put("a", mdata.getString("appKey"));
//
//            params.put("o", mdata.getString("cpOrderId"));
//
//            params.put("t", mdata.getInt("timestamp")+"");
//
//            if(!mdata.optString("voucherId").equals("")){
//                params.put("v", mdata.getString("voucherId"));
//            }
//
//            params.put("e", mdata.getString("cpExtra"));
//
//            params.put("s", mdata.getString("sign"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        isPay = true;
        //String cpOrderId = "cp订单号";
        //透传字段
        String extra = String.format("extra_%s", "1234abc");
        PayInfo payInfo = new PayInfo.Builder()
                .setCpOrderId(payParams.get(JunSConstants.PAY_M_ORDER_ID)) //游戏方自定义订单号
                .setProductCode("pCode0101") //商品编码
                .setProductName("10钻石") //商品名称
                .setPrice(1) //商品价格，单位(分)
                .setProductCount(1) //商品数量，默认是1
                .setCpExtra(extra)
                .build();

        MetaApi.pay(activity, payInfo, new PayResultCallback() {

            @Override
            public void onPayResult(int code, String msg) {
                if (code == MetaPayResult.CODE_PAY_SUCCESS) {
                    Bus.getDefault().post(OPayEv.getSucc("pay success"));
                } else {
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
                }
            }
        });

    }

    @Override
    public void exitGame(Activity activity) {
        MetaExitDialog.showExit(activity, new MetaExitDialog.ExitCallback() {
            @Override
            public void toContinue() {

            }

            @Override
            public void toExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }
}
