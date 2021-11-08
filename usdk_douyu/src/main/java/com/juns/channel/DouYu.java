package com.juns.channel;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.douyu.gamesdk.DouyuCallback;
import com.douyu.gamesdk.DouyuGameSdk;
import com.douyu.gamesdk.DouyuSdkParams;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DouYu extends OPlatformSDK {
    private static final String TAG = "DouYu";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public DouYu(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        // 检查SDK资源配置是否正确接入，提示正常后删除调用
        //DouyuGameSdk.getInstance().checkSdkValid(activity);
       // DouyuGameSdk.
        DouyuGameSdk.getInstance().setSdkCallback(new DouyuCallback() {
            @Override
            public void onSuccess(DouyuSdkParams data) {
                //预留，暂不处理
            }

            @Override
            public void onError(String code, String msg) {
              //  Toast.makeText(MainActivity.this, "游戏客户端 SDK回调 code:"+code+" msg:"+msg, Toast.LENGTH_LONG).show();
                if (DouyuCallback.CODE_SWITCH_ACCOUNT.equals(code)) {
                    //游戏方可用sid直接切换账号
                  //  showGamePage();
                    Bus.getDefault().post(new OLogoutEv());
                }
                else {
                    //错误包括登录态过期，服务器异等，可统一处理为退出游戏，重新登录
                 //   showLoginPage();
                    Bus.getDefault().post(new OLogoutEv());
                }
            }
        });
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        DouyuGameSdk.getInstance().login(activity, mLoginCallback);
    }
    private DouyuCallback mLoginCallback = new DouyuCallback() {
        @Override
        public void onSuccess(DouyuSdkParams data) {
            logger.print("mLoginCallback onSuccess data:"+data.getParams().get(data.SID));
//            showGamePage();
            login2RSService(DouyuGameSdk.getSDKVersion(),data.getParams().get(data.SID));
        }

        @Override
        public void onError(String code, String msg) {
            logger.print("mLoginCallback onError data:"+msg);
            Bus.getDefault().post(OLoginEv.getFail(Integer.parseInt(code),msg));
            //Toast.makeText(MainActivity.this, "游戏客户端 登录失败 code:"+code+" msg:"+msg, Toast.LENGTH_LONG).show();
        }
    };
    private void login2RSService(String version,String sid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("sdk_version", version);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(sid, dataJson.toString());
    }
    @Override
    public void logout(Activity mainAct) {

    }
    HashMap<String, String> roleInfo =null;
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            roleInfo = submitInfo;
        }
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
        //logger.print("llalalallal"+payParams.toString());
        try {
            JSONObject jsonData = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            DouyuSdkParams payParams2 = new DouyuSdkParams()
                    //接口版本，必传，不传默认 METHOD_V1
                    .put(DouyuSdkParams.METHOD_VERSION, DouyuSdkParams.METHOD_V2)
                    //订单号，必传
                    .put(DouyuSdkParams.THIRD_ORDER_ID, payParams.get(JunSConstants.PAY_M_ORDER_ID))
                    //游戏区 ID，必传
                    .put(DouyuSdkParams.AREA_ID, roleInfo.get(JunSConstants.SUBMIT_SERVER_ID))
                    //游戏角色名，必传
                    .put(DouyuSdkParams.ROLE_NAME, roleInfo.get(JunSConstants.SUBMIT_ROLE_NAME))
                    //订单标题，必传
                    .put(DouyuSdkParams.TITLE, jsonData.getString("title"))
                    //订单金额，必传
                    .put(DouyuSdkParams.AMOUNT, money+"")
                    //订单签名，必传
                    .put(DouyuSdkParams.SIGN, jsonData.getString("sign"))
                    //回调信息，必传
                    .put(DouyuSdkParams.CALLBACK, payParams.get(JunSConstants.PAY_M_ORDER_ID));
            DouyuGameSdk.getInstance().pay(payParams2, mPayCallback);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    private DouyuCallback mPayCallback = new DouyuCallback() {
        @Override
        public void onSuccess(DouyuSdkParams data) {
            Bus.getDefault().post(OPayEv.getSucc("success"));
           // Toast.makeText(MainActivity.this, "游戏客户端 支付成功 data:"+data, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(String code, String msg) {
            Bus.getDefault().post(OPayEv.getFail(Integer.parseInt(code),msg));
          //  Toast.makeText(MainActivity.this, "游戏客户端 支付失败 code:"+code+" msg:"+msg, Toast.LENGTH_LONG).show();
        }
    };
    @Override
    public void exitGame(Activity activity) {
        DouyuGameSdk.getInstance().showExitDialog(activity, new DouyuCallback() {
            @Override
            public void onSuccess(DouyuSdkParams data) {
                //推出游戏
               Bus.getDefault().post(OExitEv.getSucc());
            }

            @Override
            public void onError(String code, String msg) {
                //继续游戏
            }
        });
    }
}
