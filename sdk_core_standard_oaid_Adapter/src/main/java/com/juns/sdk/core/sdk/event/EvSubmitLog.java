package com.juns.sdk.core.sdk.event;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：22/10/2020-20:53
 * Author: enjoy
 */
public class EvSubmitLog {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    private static final String SUBMIT_SUCC = "submit success.";

    private int ret;
    private String submitInfo;
    private int submitCode;
    private String submitMsg;

    private EvSubmitLog() {
        this.ret = SUCCESS;
        this.submitInfo = SUBMIT_SUCC;
    }

    private EvSubmitLog(int code, String msg) {
        this.ret = FAIL;
        this.submitCode = code;
        this.submitMsg = msg;
    }

    public static EvSubmitLog getSucc() {
        return new EvSubmitLog();
    }

    public static EvSubmitLog getFail(int code, String msg) {
        return new EvSubmitLog(code, msg);
    }

    public int getRet() {
        return ret;
    }

    public String getSubmitInfo() {
        return submitInfo;
    }

    public int getSubmitCode() {
        return submitCode;
    }

    public String getSubmitMsg() {
        return submitMsg;
    }

    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            data.put("ret", ret);
            data.put("submitInfo", submitInfo);
            data.put("submitCode", submitCode);
            data.put("submitMsg", submitMsg);
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
