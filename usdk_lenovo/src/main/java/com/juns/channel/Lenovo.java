package com.juns.channel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;


import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.lenovo.lsf.gamesdk.GamePayRequest;
import com.lenovo.lsf.gamesdk.IAuthResult;
import com.lenovo.lsf.gamesdk.IPayResult;
import com.lenovo.lsf.gamesdk.LenovoGameApi;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Lenovo extends OPlatformSDK {
    private static final String TAG = "Lenovo";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;
    public Lenovo(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mContext = activity;
        LenovoGameApi.doInit(activity, SDKApplication.getPlatformConfig().getExt1());
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @SuppressLint("WrongConstant")
    @Override
    public void login(Activity activity) {
        LenovoGameApi.doAutoLogin(activity, new IAuthResult() {
            @Override
            public void onFinished(boolean ret, String st) {
                if(ret){
                    login2RSService(st);
                }else{
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "fail"));

                }
            }
        });
    }

    @Override
    public void logout(Activity mainAct) {

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





    private void login2RSService(String st) {

        OPlatformUtils.loginToServer(st, null);
    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {


    }

    /**
     * 退出游戏
     */
    private void doExitGame() {
        LenovoGameApi.doQuit((Activity) mContext, new IAuthResult() {
            @Override
            public void onFinished(boolean ret, String s) {
                if(ret){
                    Bus.getDefault().post(OExitEv.getSucc());
                }else{


                }
            }
        });

    }

    private void doPay(Context context, HashMap<String, String> payParams,
                       String moid, String mData) {
        GamePayRequest payRequest = new GamePayRequest();
        try {
            JSONObject data = new JSONObject(mData);
            payRequest.addParam("waresid", data.getString("waresid"));// 即chargepoint
            payRequest.addParam("exorderno", moid);
            payRequest.addParam("price", data.getInt("price"));
            payRequest.addParam("quantity", 1);
            payRequest.addParam("cpprivateinfo", payParams.get(JunSConstants.PAY_EXT));
            //Log.e("guoinfo",data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LenovoGameApi.doPay((Activity) context, "", payRequest, new IPayResult() {
            @Override
            public void onPayResult(int resultCode, String signValue, String resultInfo) {
                if (LenovoGameApi.PAY_SUCCESS == resultCode) {
                    // 支付成功
                    String[] strResult = resultInfo.split("&");
                    logger.print("Lenovo --> doPay --> Success --> resultMsg --> " + strResult);
                    Bus.getDefault().post(OPayEv.getSucc("pay success."));
                } else {
                    // 支付失败处理
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, resultInfo));
                }
            }
        });




    }

}
