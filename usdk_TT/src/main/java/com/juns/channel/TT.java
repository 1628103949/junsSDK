package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Toast;

import com.google.gson1.JsonObject;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.wett.cooperation.container.SdkCallback;
import com.wett.cooperation.container.TTSDKV2;
import com.wett.cooperation.container.bean.GameInfo;
import com.wett.cooperation.container.bean.PayInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

public class TT extends OPlatformSDK {
    private static final String TAG = "TT";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    public TT(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(final Activity activity) {
        int i = 0;
        if(SDKApplication.getPlatformConfig().getExt2().equals("0")){
            i=Configuration.ORIENTATION_LANDSCAPE;
        }else{
            i=Configuration.ORIENTATION_PORTRAIT;
        }
        boolean debugMode = false;
        GameInfo gameInfo = new GameInfo();
        gameInfo.setGameId(SDKApplication.getPlatformConfig().getExt1());
        TTSDKV2.getInstance().init(activity, gameInfo, debugMode, i, new SdkCallback<String>() {
            @Override
            protected boolean onResult(int i, String s) {
                if (i == 0) {
                    //Log.e("test", "init成功");
                    TTSDKV2.getInstance().onCreate(activity);
                    Bus.getDefault().post(OInitEv.getSucc());
                    TTSDKV2.getInstance().setLogoutListener(new SdkCallback<String>() {
                        @Override
                        protected boolean onResult(int i, String s) {
                            if (i == 0) {
                                Bus.getDefault().post(new OLogoutEv());
                            } else {

                            }
                            return false;
                        }
                    });
//                    loginImpl();
                } else {
                   // Toast.makeText(instance, "init失败", Toast.LENGTH_SHORT).show();
                    Bus.getDefault().post(OInitEv.getFail(i,"fail"));
                }
                return false;
            }
        });
    }
    String uid = "";
    @Override
    public void login(final Activity activity) {
        TTSDKV2.getInstance().login(activity, "TT", new SdkCallback<String>() {
            @Override
            protected boolean onResult(int i, String s) {
                if (i == 0) {
                    TTSDKV2.getInstance().showFloatView(activity);
                    uid = TTSDKV2.getInstance().getUid();
                    String session = TTSDKV2.getInstance().getSession();
                    login2RSService(activity,uid,session);
                } else {

                }
                return false;
            }
        });
    }

    private void login2RSService(Context ctx, String uid,String token) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid",uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        if(TTSDKV2.getInstance().isLogin()){
            TTSDKV2.getInstance().showFloatView(mainAct);
        }
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        TTSDKV2.getInstance().onPause(mainAct);
        TTSDKV2.getInstance().hideFloatView(mainAct);
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        logger.print(submitInfo.toString());
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            TTSDKV2.getInstance().submitGameRoleInfo(mainAct,"create",Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)),submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME),
                    submitInfo.get(JunSConstants.SUBMIT_ROLE_ID), submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME), Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)),Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_VIP)), Long.valueOf(10000),"");

        }else if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            TTSDKV2.getInstance().submitGameRoleInfo(mainAct,"enter",Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)),submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME),
                    submitInfo.get(JunSConstants.SUBMIT_ROLE_ID), submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME), Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)),Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_VIP)), Long.valueOf(10000),"");

        }else{
            TTSDKV2.getInstance().submitGameRoleInfo(mainAct,"upgrade",Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID)),submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME),
                    submitInfo.get(JunSConstants.SUBMIT_ROLE_ID), submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME), Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)),Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_VIP)), Long.valueOf(10000),"");

        }

    }

    @Override
    public void logout(Activity mainAct) {
        TTSDKV2.getInstance().logout(mainAct);
        //TTSDKV2.getInstance().hideFloatView(mainAct);
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
        JSONObject payJson = null;
        try {
            payJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            PayInfo payInfo = new PayInfo();
            payInfo.setRoleId(payJson.getString("roleId"));
            payInfo.setRoleName(payJson.getString("roleName"));
            payInfo.setBody(payJson.getString("roleName"));
            payInfo.setCpFee(payJson.getInt("cpFee"));
            payInfo.setCpTradeNo(payJson.getString("cpTradeNo"));
            payInfo.setServerId(Integer.parseInt(payJson.getString("serverId")));
            payInfo.setServerName(payJson.getString("serverName"));
            payInfo.setExInfo(payJson.getString("exInfo"));
            payInfo.setSubject(payJson.getString("subject"));
            payInfo.setPayMethod(payInfo.PAY_METHOD_ALL);
//                payInfo.setCpCallbackUrl("http://120.132.68.148/game/rest/mock/callback.shtml");
            payInfo.setCpCallbackUrl(payJson.getString("cpCallbackUrl"));
            payInfo.setChargeDate(new Date().getTime());
            payInfo.setTs(payJson.getString("ts"));
            payInfo.setExInfo("拓展信息");
            TTSDKV2.getInstance().pay(activity, payInfo, new SdkCallback<String>() {
                @Override
                protected boolean onResult(int i, String s) {
                    if (i == 0) {
                        Bus.getDefault().post(OPayEv.getSucc(s));
                    }else if(i == 2){
                        Bus.getDefault().post(OPayEv.getFail(i,s));
                    }else{
                        Bus.getDefault().post(OPayEv.getFail(i,s));
                    }
                    return false;
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exitGame(Activity activity) {
        TTSDKV2.getInstance().uninit(activity, new SdkCallback<String>() {
            @Override
            protected boolean onResult(int i, String s) {
                if (i == 0) {
                    Bus.getDefault().post(OExitEv.getSucc());
                } else {

                }
                return false;
            }
        });
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        TTSDKV2.getInstance().onRestart(mainAct);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        TTSDKV2.getInstance().onStart(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        TTSDKV2.getInstance().onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        TTSDKV2.getInstance().onDestroy(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        TTSDKV2.getInstance().onNewIntent(data);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        TTSDKV2.getInstance().onActivityResult(mainAct,requestCode,resultCode,data);
    }
}
