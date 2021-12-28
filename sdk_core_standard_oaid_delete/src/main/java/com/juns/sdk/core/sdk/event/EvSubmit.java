package com.juns.sdk.core.sdk.event;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：18/12/2018-9:53 AM
 * Author: ranger
 */
public class EvSubmit {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    private static final String SUBMIT_SUCC = "submit success.";

    private int ret;
    private String submitInfo;
    private int submitCode;
    private String submitMsg;

    private EvSubmit() {
        this.ret = SUCCESS;
        this.submitInfo = SUBMIT_SUCC;
    }

    private EvSubmit(int code, String msg) {
        this.ret = FAIL;
        this.submitCode = code;
        this.submitMsg = msg;
    }

    public static EvSubmit getSucc() {
        return new EvSubmit();
    }

    public static EvSubmit getFail(int code, String msg) {
        return new EvSubmit(code, msg);
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
