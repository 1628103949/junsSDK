package com.juns.channel;

import android.app.Activity;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.muzhiwan.sdk.core.MzwSdkController;
import com.muzhiwan.sdk.core.callback.MzwInitCallback;
import com.muzhiwan.sdk.core.callback.MzwLoignCallback;
import com.muzhiwan.sdk.core.callback.MzwPayCallback;
import com.muzhiwan.sdk.service.MzwOrder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MZW extends OPlatformSDK {
    private static final String TAG = "MZW";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public MZW(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        MzwSdkController.getInstance().init(activity, MzwSdkController.ORIENTATION_HORIZONTAL, new MzwInitCallback() {
            @Override
            public void onResult(int code, String msg) {
                if(code == 1){
                    Bus.getDefault().post(OInitEv.getSucc());
                }else{
                    Bus.getDefault().post(OInitEv.getFail(code,msg));
                }
            }
        });

    }

    @Override
    public void login(Activity activity) {
        MzwSdkController.getInstance().doLogin(new MzwLoignCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.e("guoinfo",code+msg);
                if(code == 1){
                    login2RSService(msg,"无");
                }else if(code == 6){
                    Bus.getDefault().post(new OLogoutEv());
                }else{
                    Bus.getDefault().post(OInitEv.getFail(code,msg));
                }
            }
        });
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
        MzwSdkController.getInstance().doLogout();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        float money = Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        MzwOrder order = new MzwOrder();
        order.setMoney(money);
        order.setProductname(payParams.get(JunSConstants.PAY_ORDER_NAME));
        order.setProductdesc(payParams.get(JunSConstants.PAY_ORDER_NAME));
        order.setExtern(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        MzwSdkController.getInstance().doPay(order, new MzwPayCallback() {
            @Override
            public void onResult(int code, MzwOrder mzwOrder) {
                if(code == 1){
                    Bus.getDefault().post(OPayEv.getSucc(mzwOrder.getExtern()));
                }else{
                    Bus.getDefault().post(OInitEv.getFail(code,mzwOrder.getExtern()));
                }
            }
        });
    }

    @Override
    public void exitGame(Activity activity) {
        MZWExitDialog.showExit(activity, new MZWExitDialog.ExitCallback() {
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
        MzwSdkController.getInstance().destory();
    }
}
