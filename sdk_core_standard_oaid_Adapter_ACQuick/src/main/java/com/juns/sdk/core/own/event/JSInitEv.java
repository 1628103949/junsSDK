package com.juns.sdk.core.own.event;

import com.juns.sdk.core.sdk.event.EvInit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-4:15 PM
 * Author: ranger
 */
public class JSInitEv {

    private int ret;
    private int initCode;
    private String initMsg;

    private JSInitEv() {
        this.ret = EvInit.SUCCESS;
    }

    private JSInitEv(int code, String msg) {
        this.ret = EvInit.FAIL;
        this.initCode = code;
        this.initMsg = msg;
    }

    public static JSInitEv getSucc() {
        return new JSInitEv();
    }

    public static JSInitEv getFail(int code, String msg) {
        return new JSInitEv(code, msg);
    }

    public int getRet() {
        return ret;
    }

    public int getInitCode() {
        return initCode;
    }

    public String getInitMsg() {
        return initMsg;
    }

    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            data.put("ret", ret);
            data.put("initCode", initCode);
            data.put("initMsg", initMsg);
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
