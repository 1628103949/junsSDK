package com.juns.sdk.core.own.event;

import com.juns.sdk.core.sdk.event.EvPay;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-4:16 PM
 * Author: ranger
 */
public class JSPayEv {
    private int ret;
    private String payInfo;
    private int payCode;
    private String payMsg;

    private JSPayEv(String payInfo) {
        this.ret = EvPay.SUCCESS;
        this.payInfo = payInfo;
    }

    private JSPayEv(int payCode, String payMsg) {
        this.ret = EvPay.FAIL;
        this.payCode = payCode;
        this.payMsg = payMsg;
    }

    public static JSPayEv getSucc(String payInfo) {
        return new JSPayEv(payInfo);
    }

    public static JSPayEv getFail(int payCode, String payMsg) {
        return new JSPayEv(payCode, payMsg);
    }

    public int getRet() {
        return ret;
    }

    public String getPayInfo() {
        return payInfo;
    }

    public int getPayCode() {
        return payCode;
    }

    public String getPayMsg() {
        return payMsg;
    }

    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            data.put("ret", ret);
            data.put("payInfo", payInfo);
            data.put("payCode", payCode);
            data.put("payMsg", payMsg);
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
