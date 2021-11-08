package com.juns.sdk.core.sdk.event;

import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.own.event.JSLoginEv;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-4:16 PM
 * Author: ranger
 */
public class EvLogin {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    private int ret;
    private int loginCode;
    private String loginMsg;
    private String userInfo;

    private EvLogin(String userInfo) {
        this.ret = SUCCESS;
        this.userInfo = userInfo;
    }

    private EvLogin(int loginCode, String loginMsg) {
        this.ret = FAIL;
        this.loginCode = loginCode;
        this.loginMsg = loginMsg;
    }

    public EvLogin(OLoginEv oLogin) {
        this.ret = oLogin.getRet();
        this.loginCode = oLogin.getLoginCode();
        this.loginMsg = oLogin.getLoginMsg();
        this.userInfo = oLogin.getUserInfo();
    }

    public EvLogin(JSLoginEv tLogin) {
        this.ret = tLogin.getRet();
        this.loginCode = tLogin.getLoginCode();
        this.loginMsg = tLogin.getLoginMsg();
        this.userInfo = tLogin.getUserInfo();
    }

    public static EvLogin getSucc(String userInfo) {
        return new EvLogin(userInfo);
    }

    public static EvLogin getFail(int code, String msg) {
        return new EvLogin(code, msg);
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
