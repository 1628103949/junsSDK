package com.juns.sdk.core.platform.event;

import com.juns.sdk.core.sdk.event.EvLogin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-4:16 PM
 * Author: ranger
 */
public class OLoginEv {
    private int ret;
    private int loginCode;
    private String loginMsg;
    private String userInfo;

    private OLoginEv(String userInfo) {
        this.ret = EvLogin.SUCCESS;
        this.userInfo = userInfo;
    }

    private OLoginEv(int initCode, String initMsg) {
        this.ret = EvLogin.FAIL;
        this.loginCode = initCode;
        this.loginMsg = initMsg;
    }

    public static OLoginEv getSucc(String userInfo) {
        return new OLoginEv(userInfo);
    }

    public static OLoginEv getFail(int code, String msg) {
        return new OLoginEv(code, msg);
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
