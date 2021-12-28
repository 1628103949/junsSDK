package com.juns.sdk.core.sdk.event;

import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.own.event.JSPayEv;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：17/12/2018-4:16 PM
 * Author: ranger
 */
public class EvPay {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    private int ret;
    private String payInfo;
    private int payCode;
    private String payMsg;

    private EvPay(String payInfo) {
        this.ret = SUCCESS;
        this.payInfo = payInfo;
    }

    private EvPay(int payCode, String payMsg) {
        this.ret = FAIL;
        this.payCode = payCode;
        this.payMsg = payMsg;
    }

    public EvPay(OPayEv oPay) {
        this.ret = oPay.getRet();
        this.payInfo = oPay.getPayInfo();
        this.payCode = oPay.getPayCode();
        this.payMsg = oPay.getPayMsg();
    }

    public EvPay(JSPayEv tPay) {
        this.ret = tPay.getRet();
        this.payInfo = tPay.getPayInfo();
        this.payCode = tPay.getPayCode();
        this.payMsg = tPay.getPayMsg();
    }


    public static EvPay getSucc(String payInfo) {
        return new EvPay(payInfo);
    }

    public static EvPay getFail(int payCode, String payMsg) {
        return new EvPay(payCode, payMsg);
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
