package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.loongcheer.lradsdk.api.StatusCode;
import com.loongcheer.lrsmallsdk.LrSmallApi;
import com.loongcheer.lrsmallsdk.inter.ILrCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LongYue extends OPlatformSDK {
    private static final String TAG = "LongYue";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public LongYue(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        LrSmallApi.init(mainAct, new ILrCallback() {
            @Override
            public void callback(int code, JSONObject jsonObject) {
                //Log.e("guoinfo","jsoninfo"+jsonObject.toString());
                if (code == StatusCode.STATUS_OK) {
                    Bus.getDefault().post(OInitEv.getSucc());
                    //初始化成功

                } else if(code == StatusCode.STATUS_ERROR) {
                    Bus.getDefault().post(OInitEv.getFail(code,"fail"));
                    //初始化失败

                }
            }
        });
        LrSmallApi.onCreate(mainAct, mainAct.getIntent().getExtras());
    }

    @Override
    public void init(Activity activity) {

        //Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(final Activity activity) {
        LrSmallApi.login(activity, new ILrCallback() {
            @Override
            public void callback(int code, JSONObject jsonObject) {
                //Log.e("guoinfo","login" + code + " " + jsonObject);
                if (code == StatusCode.STATUS_OK) {
                    //登录成功
                    try {
                        login2RSService(activity,jsonObject.getString("authCode"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if(code == StatusCode.STATUS_ERROR){
                //登录失败，登录失败时jsonObject没有值
                    Bus.getDefault().post(OLoginEv.getFail(code,"fail"));
            }
        }
    }, new ILrCallback() {
        @Override
        public void callback(int code, JSONObject jsonObject) {
            // 正式包体带悬浮窗，悬浮窗中有”退出登录“功能，需在此回调加上退出登录逻辑，如返回登录界面
            Bus.getDefault().post(new OLogoutEv());
        }
    });
    }

    private void login2RSService(final Activity activity, String token) {
        JSONObject dataJson = new JSONObject();
        OPlatformUtils.loginToServer(token, dataJson.toString(), new OPlatformUtils.OLoginCallBack() {
            @Override
            public void onFinish(String info) {
                //Log.e("guoinfo",info);
                try {
                    JSONObject jsonObject = new JSONObject(info);
                    JSONObject jsonObject1 = new JSONObject(jsonObject.getString("data"));
                    //Log.e("guoinfo",jsonObject1.getString("access_token"));
                    LrSmallApi.onLoginRsp(activity,jsonObject1.getString("access_token"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void logout(Activity mainAct) {
        LrSmallApi.logout(mainAct);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //Log.e("guoinfo",payParams.toString());
        try {
            JSONObject jsonObject = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
            LrSmallApi.buy(activity, payParams.get(JunSConstants.PAY_M_ORDER_ID)
                    , money
                    , jsonObject.getString("productId")
                    , payParams.get(JunSConstants.PAY_ORDER_NAME)
                    , 1
                    , payParams.get(JunSConstants.PAY_ORDER_NAME)
                    , payParams.get(JunSConstants.PAY_ROLE_ID)
                    , payParams.get(JunSConstants.PAY_ROLE_NAME)
                    , payParams.get(JunSConstants.PAY_ROLE_LEVEL)
                    , payParams.get(JunSConstants.PAY_ROLE_VIP)
                    , payParams.get(JunSConstants.PAY_SERVER_NAME)
                    , payParams.get(JunSConstants.PAY_SERVER_ID)
                    , "无"
                    , 0
                    , jsonObject.getString("zhifuaddress")
                    , payParams.get(JunSConstants.PAY_EXT), new ILrCallback() {
                        @Override
                        public void callback(int code, JSONObject jsonObject) {
                            if (code == StatusCode.STATUS_OK) {
                                Bus.getDefault().post(OPayEv.getSucc("success"));
                                //支付成功

                            } else if(code == StatusCode.STATUS_ERROR) {
                                Bus.getDefault().post(OPayEv.getFail(code,"pay fail"));
                                //支付失败

                            }else if(code ==  StatusCode.STATUS_PAY_CANCEL){
                                Bus.getDefault().post(OPayEv.getFail(code,"pay cancel"));
                                //支付取消
                            }else if(code == StatusCode.STATUS_PAY_FINISH){
                                Bus.getDefault().post(OPayEv.getFail(code,"pay fail"));
                                //支付结束
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exitGame(Activity activity) {
        LrSmallApi.exit(activity, new ILrCallback() {
            @Override
            public void callback(int code, JSONObject jsonObject) {
                if (code == StatusCode.STATUS_OK) {
                    //sdk确认退出，请cp 执行退出逻辑
                    Bus.getDefault().post(OExitEv.getSucc());
                }
                if (code == StatusCode.STATUS_EXIT_AGENT){
                    //sdk 不执行退出，请cp 拉起自己的退出框
                }
                else if (code == StatusCode.STATUS_ERROR){
                    //sdk 取消退出，cp 可以无视
                }
            }
        });
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        LrSmallApi.onStart(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        LrSmallApi.onResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        LrSmallApi.onPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        LrSmallApi.onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        LrSmallApi.onDestroy(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        LrSmallApi.onRestart(mainAct);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        LrSmallApi.onActivityResult(mainAct,requestCode,resultCode,data);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        LrSmallApi.onNewIntent(mainAct,data);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        LrSmallApi.onRequestPermissionsResult(mainAct,requestCode,permissions,grantResults);
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        super.onConfigurationChanged(mainAct, newConfig);
        LrSmallApi.onConfigurationChanged(mainAct,newConfig);
    }


}
