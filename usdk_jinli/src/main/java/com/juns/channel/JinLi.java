package com.juns.channel;

import android.app.Activity;
import android.util.Log;

import com.gionee.gamesdk.floatwindow.AccountInfo;
import com.gionee.gamesdk.floatwindow.GameOrder;
import com.gionee.gamesdk.floatwindow.GamePayCallBack;
import com.gionee.gamesdk.floatwindow.GamePayManager;
import com.gionee.gamesdk.floatwindow.GamePlatform;
import com.gionee.gameservice.ui.QuitGameCallback;
import com.gionee.gsp.GnEFloatingBoxPositionModel;
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
import com.lcstudio.comm.support.SdkMng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JinLi extends OPlatformSDK {
    private static final String TAG = "JinLi";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public JinLi(OPlatformBean pBean) {
        super(pBean);
    }



    @Override
    public void init(final Activity activity) {
        SdkMng sdkMng = new SdkMng();
        //先申请权限。 在onRequestPermissionsResult回调， 调用初始化
        if (!sdkMng.needRequestPermission(activity)) {
            //已申请过，或不必申请权限，直接调用初始化
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //  init初始化接口最好闪屏界面就调用。保证在调用登录前，初始化已完成
                    GamePlatform.init(activity, SDKApplication.getPlatformConfig().getExt1(), GnEFloatingBoxPositionModel.RIGHT_TOP);
                }
            }).start();
        }

        Bus.getDefault().post(OInitEv.getSucc());
    }

    private String uid="";
    @Override
    public void login(Activity activity) {
        GamePlatform.loginAccount(activity, true, new GamePlatform.LoginListener() {
            @Override
            public void onError(Object o) {
                Bus.getDefault().post(OLoginEv.getFail(2,o+""));
            }

            @Override
            public void onSuccess(AccountInfo accountInfo) {
                //Constants.sPlayerId = accountInfo.mPlayerId;

                /**
                 * 账号校验通过以后的一个令牌，为商户服务器提供的一种账号安全验证token，是JSON字符串
                 * 使用方式请参考服务端接入指南 “AmigoToken验证” 一项
                 */
                String amigoToken = accountInfo.mToken;

                //账号的唯一标识（下单的时候用到）
                uid = accountInfo.mUserId;
                login2RSService(uid,amigoToken);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void login2RSService(String uid,String token ) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid",uid);
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
            dataJson.put("user_id", SDKData.getpUserId());
            dataJson.put("api_version", 2);
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //logger.print(payParams.toString());
        //调用支付
        try {
            JSONObject jsonData = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
//            JSONObject jsonData2 = new JSONObject(jsonData.getString("order_no"));
//            jsonData2.put("user_id",uid);
            //Log.e("guotest11111",jsonData.getString("order_no"));
//            Log.e("guotest22222",jsonData2.toString());
            GamePayManager.getInstance().pay(activity, jsonData.getString("order_no"), new GamePayCallBack() {
                // 因为是服务器下单，所以这个接口不会返回
                @Override
                public void onCreateOrderSuccess(String s) {
                    logger.print("下单成功"+s);
                    Bus.getDefault().post(OPayEv.getSucc(s));
                }

                // 支付成功
                @Override
                public void onPaySuccess() {
                    Bus.getDefault().post(OPayEv.getSucc("success"));
                }

                // 支付失败,或者下单失败的原因都会在此出现（若是服务器下单，则下单失败原因不会在此出现）
                @Override
                public void onPayFail(Exception e) {
                    logger.print("支付失败"+e);
                    Bus.getDefault().post(OPayEv.getFail(2,e.toString()));
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    @Override
    public void exitGame(Activity activity) {
        GamePlatform.quitGame(activity, new QuitGameCallback() {
            @Override
            public void onQuit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }

            @Override
            public void onCancel() {

            }
        });
    }
}
