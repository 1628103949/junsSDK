package com.juns.sdk.core.sdk.common;

import android.os.Handler;

import com.juns.sdk.core.http.JunSResponse;

import com.juns.sdk.core.http.params.HeartBeatParam;

import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;

import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;



public class HeartBeat {
    public static boolean state = false;
    //public static boolean enterGame = false;
    public static boolean stateRealName = false;
    private static class SingletonHolder {
        private static final HeartBeat INSTANCE = new HeartBeat();
    }
    public static HeartBeat getInstance(){
        return HeartBeat.SingletonHolder.INSTANCE;
    }
    Handler handler = null;
    Runnable runnable = null;
    public void startHeartBeat(final int time){
        if (SDKApplication.isTNPlatform()) {
            if(!state){
                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handler.postDelayed(this, time);
                            SDKCore.logger.print("postHeartbeat --> "+System.currentTimeMillis());
                            HeartBeatParam heartBeatParam = new HeartBeatParam(SDKData.getSdkPeriod());
                            x.http().post(heartBeatParam, new Callback.CommonCallback<JunSResponse>() {
                                @Override
                                public void onSuccess(JunSResponse result) {
                                    dealReqSuccess(result);
                                }

                                @Override
                                public void onError(Throwable ex, boolean isOnCallback) {
                                    //ViewUtils.sdkShowTips(SDKCore.getMainAct(), ((JunSRealNameException) ex).getServerMsg());
                                }

                                @Override
                                public void onCancelled(CancelledException cex) {

                                }

                                @Override
                                public void onFinished() {

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                handler.postDelayed(runnable, time); // 在初始化方法里.
                state = true;
            }
        }


    }
    public void endHeartBeat(){
        if (SDKApplication.isTNPlatform()) {
            if(state){
              //  offline();
                SDKCore.logger.print("endHeartbeat --> "+System.currentTimeMillis());
                handler.removeCallbacks(runnable);
                state = false;
                stateRealName = false;
            }
        }
    }
//    public static void offline(){
//
//        if(enterGame){
//            JunSSdkApi.getInstance().sdkEventPost("logout", "", new JunSCallback() {
//                @Override
//                public void onSuccess(String info) {
//                    Log.e("guoinfo","上报失败");
//                }
//
//                @Override
//                public void onFail(int code, String msg) {
//
//                }
//            });
//            enterGame = false;
//        }
//
//    }
    private void dealReqSuccess(JunSResponse result){
        SDKCore.logger.print(result.toString());
        //String data = result.data;
        int code = 0;
        try {
            JSONObject dataJson = new JSONObject(result.data);
            code = dataJson.optInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (code){
            case 0:
                SDKCore.logger.print(result.msg);
                break;
            case 1:
                break;
            case 2:
                if(!stateRealName){
                    stateRealName = true;
                    RealNameDialog.show(SDKCore.getMainAct(), false, new RealNameDialog.Callback() {
                        @Override
                        public void onSuccess() {
                            //dealCallback();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    JunsNotiDialog.showNoti(SDKCore.getMainAct(),result.msg,true);
                }

                break;
            case 3:
                JunsNotiDialog.showNoti(SDKCore.getMainAct(),result.msg,false);
                break;
            case 4:
                JunsNotiDialog.showNoti(SDKCore.getMainAct(),result.msg,true);
                break;
        }

    }
}
