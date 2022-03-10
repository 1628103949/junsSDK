package com.juns.sdk.core.own.event;

import static com.juns.sdk.core.sdk.SDKCore.logger;

import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.HeartBeat;
import com.juns.sdk.core.sdk.event.EvLogin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-4:16 PM
 * Author: ranger
 */
public class JSLoginEv {
    private int ret;
    private int loginCode;
    private String loginMsg;
    private String userInfo;

    private JSLoginEv(String userInfo) {
        this.ret = EvLogin.SUCCESS;
        this.userInfo = userInfo;
    }

    private JSLoginEv(int initCode, String initMsg) {
        this.ret = EvLogin.FAIL;
        this.loginCode = initCode;
        this.loginMsg = initMsg;
    }

    public static JSLoginEv getSucc(String userInfo) {
        if(SDKData.getSdkPeriod()!=9999){
//            if(SDKData.getSdkPeriod()<300){
//                SDKData.setSdkPeriod(300);
//            }
            logger.print("heartbeat called.");
            HeartBeat.getInstance().startHeartBeat(SDKData.getSdkPeriod()*1000);
        }
        return new JSLoginEv(userInfo);
    }

    public static JSLoginEv getFail(int code, String msg) {
        return new JSLoginEv(code, msg);
    }

    public int getRet() {
        return ret;
    }

    public int getLoginCode() {
        return loginCode;
    }

    public String getLoginMsg() {
        return loginMsg;
    }

    public String getUserInfo() {
        return userInfo;
    }

    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            data.put("ret", ret);
            data.put("userInfo", userInfo);
            data.put("loginCode", loginCode);
            data.put("loginMsg", loginMsg);
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
