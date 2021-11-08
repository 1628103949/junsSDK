package com.juns.channel;

import android.app.Activity;
import android.util.Log;

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
import com.kptach.cpgame.CpGameSDK;
import com.kptach.cpgame.SDKEventKey;
import com.kptach.cpgame.SDKEventReceiver;
import com.kptach.cpgame.SDKParamKey;
import com.kptach.cpgame.SDKParams;
import com.kptach.cpgame.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class KPY extends OPlatformSDK {
    private static final String TAG = "KPY";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public KPY(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        CpGameSDK.defaultSdk().registerSDKEventReceiver(receiver);
    }

    private SDKEventReceiver receiver = new SDKEventReceiver(){
        @Subscribe(event = SDKEventKey.ON_INIT_SUCC)
        private void onInitSucc() {
            Bus.getDefault().post(OInitEv.getSucc());
        }
        @Subscribe(event = SDKEventKey.ON_INIT_FAILED)
        private void onInitFailed(String data) {
            //初始化失败
            Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,data));
        }
        @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
        private void onLoginSucc(String sid) {
            //sid ⽤户唯⼀标识
            login2RSService(sid,sid);
            //Toast.makeText(MainActivity.this, "login succ,sid=" + sid, Toast.LENGTH_SHORT).show();
        }
        @Subscribe(event = SDKEventKey.ON_LOGIN_FAILED)
        private void onLoginFailed(String desc) {
            //desc 错误信息
            Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,desc));
            //Toast.makeText(MainActivity.this, desc, Toast.LENGTH_SHORT).show();
        }
        @Subscribe(event = SDKEventKey.ON_LOGOUT_SUCC)
        private void onLogoutSucc() {
           Bus.getDefault().post(new OLogoutEv());
        }
        @Subscribe(event = SDKEventKey.ON_LOGOUT_FAILED)
        private void onLogoutFailed(String msg) {

        }
        @Subscribe(event = SDKEventKey.ON_SDKEXIT_SUCC)
        private void onExitSucc(String msg) {
            Bus.getDefault().post(OExitEv.getSucc());
            //Toast.makeText(MainActivity.this, "exit sdk succ", Toast.LENGTH_SHORT).show();
        }
        @Subscribe(event = SDKEventKey.ON_SDKEXIT_FAILED)
        private void onExitFailed(String err) {
            //Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();
        }
        @Subscribe(event = SDKEventKey.ON_PAY_OVER)
        private void onPayOver(Map map) {
            int payState = -1;
            if (map != null && map.containsKey("payState")){
                payState = (int) map.get("payState");
            }
            if (payState == 1){
                //⽀付成功
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"⽀付成功"));
                //Toast.makeText(GamePayActivity.this, "⽀付成功", Toast.LENGTH_SHORT).show();
            }else if (payState == 0) {
                //⽀付失败
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"⽀付失败"));
                //Toast.makeText(GamePayActivity.this, "⽀付失败", Toast.LENGTH_SHORT).show();
            }else {
                //状态未获取到，超时处理
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"未获取到⽀付状态"));
                //Toast.makeText(GamePayActivity.this, "未获取到⽀付状态", Toast.LENGTH_SHORT).show();
            }
        }
        @Subscribe(event = SDKEventKey.ON_PAY_FAILED)
        private void onPayExit(String err){
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,err));
//            Log.i(TAG, "pay exit" + err);
//            if (err != null){
//                Toast.makeText(GamePayActivity.this, err, Toast.LENGTH_SHORT).show();
//            }
        }
    };

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
    public void init(Activity activity) {
        SDKParams sdkParams = new SDKParams();
        sdkParams.put(SDKParamKey.CONF_APP_KEY, SDKApplication.getPlatformConfig().getExt1());
        sdkParams.put(SDKParamKey.CONF_DEBUG_MODE, true);
        CpGameSDK.defaultSdk().initSdk(activity,sdkParams);
    }

    @Override
    public void login(Activity activity) {
        CpGameSDK.defaultSdk().login(activity,null);
    }

    @Override
    public void logout(Activity mainAct) {
        CpGameSDK.defaultSdk().logout(mainAct,null);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //Log.e("guoinfo",payParams.toString());
        try {
            JSONObject jsonObject = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put(SDKParamKey.PAY_APPKEY, SDKApplication.getPlatformConfig().getExt1());
            paramMap.put(SDKParamKey.PAY_AMOUNT, jsonObject.getString("money"));
            paramMap.put(SDKParamKey.PAY_CP_ORDER_ID, jsonObject.getString("orderID"));
            paramMap.put(SDKParamKey.PAY_PRODUCT_CODE, jsonObject.getString("productcode"));
            paramMap.put(SDKParamKey.PAY_PRODUCT_NAME, jsonObject.getString("productname"));
            paramMap.put(SDKParamKey.PAY_TIME, jsonObject.getString("time"));
            //参数
            SDKParams sdkParams = new SDKParams();
            Map<String, Object> map = new HashMap<String, Object>();
            map.putAll(paramMap);
            sdkParams.putAll(map);
            //获取签名,建议从服务器获取sign
            sdkParams.put(SDKParamKey.PAY_SIGN, jsonObject.getString("sign"));
            CpGameSDK.defaultSdk().pay(activity,sdkParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exitGame(Activity activity) {
        CpGameSDK.defaultSdk().exit(activity,null);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        CpGameSDK.defaultSdk().unregisterSDKEventReceiver(receiver);
    }
}
