package com.juns.sdk.core.platform.event;

import com.juns.sdk.core.sdk.event.EvExit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：18/12/2018-9:53 AM
 * Author: ranger
 */
public class OExitEv {
    private static final String EXIT_SUCC = "init success.";

    private int ret;
    private String exitInfo;
    private int exitCode;
    private String exitMsg;

    private OExitEv() {
        this.ret = EvExit.SUCCESS;
        this.exitInfo = EXIT_SUCC;
    }

    private OExitEv(int code, String msg) {
        this.ret = EvExit.FAIL;
        this.exitCode = code;
        this.exitMsg = msg;
    }

    public static OExitEv getSucc() {
        return new OExitEv();
    }

    public static OExitEv getFail(int code, String msg) {
        return new OExitEv(code, msg);
    }

    public int getRet() {
        return ret;
    }

    public String getExitInfo() {
        return exitInfo;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getExitMsg() {
        return exitMsg;
    }

    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            data.put("ret", ret);
            data.put("exitInfo", exitInfo);
            data.put("exitCode", exitCode);
            data.put("exitMsg", exitMsg);
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
