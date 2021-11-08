package com.juns.sdk.core.platform.event;

import com.juns.sdk.core.sdk.event.EvInit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-4:15 PM
 * Author: ranger
 */
public class OInitEv {
    private int ret;
    private int initCode;
    private String initMsg;

    private OInitEv() {
        this.ret = EvInit.SUCCESS;
    }

    private OInitEv(int code, String msg) {
        this.ret = EvInit.FAIL;
        this.initCode = code;
        this.initMsg = msg;
    }

    public static OInitEv getSucc() {
        return new OInitEv();
    }


    public static OInitEv getFail(int code, String msg) {
        return new OInitEv(code, msg);
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
