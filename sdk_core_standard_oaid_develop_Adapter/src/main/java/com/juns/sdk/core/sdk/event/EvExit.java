package com.juns.sdk.core.sdk.event;

import com.juns.sdk.core.own.event.JSExitEv;
import com.juns.sdk.core.platform.event.OExitEv;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：18/12/2018-9:53 AM
 * Author: ranger
 */
public class EvExit {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    private static final String EXIT_SUCC = "init success.";

    private int ret;
    private String exitInfo;
    private int exitCode;
    private String exitMsg;

    private EvExit() {
        this.ret = SUCCESS;
        this.exitInfo = EXIT_SUCC;
    }

    private EvExit(int code, String msg) {
        this.ret = FAIL;
        this.exitCode = code;
        this.exitMsg = msg;
    }

    public EvExit(OExitEv oExit) {
        this.ret = oExit.getRet();
        this.exitInfo = oExit.getExitInfo();
        this.exitCode = oExit.getExitCode();
        this.exitMsg = oExit.getExitMsg();
    }

    public EvExit(JSExitEv tExit) {
        this.ret = tExit.getRet();
        this.exitInfo = tExit.getExitInfo();
        this.exitCode = tExit.getExitCode();
        this.exitMsg = tExit.getExitMsg();
    }

    public static EvExit getSucc() {
        return new EvExit();
    }

    public static EvExit getFail(int code, String msg) {
        return new EvExit(code, msg);
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
