package com.juns.sdk.core.api;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.juns.sdk.framework.view.common.ConfirmDialog;
import com.juns.sdk.framework.view.common.ViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description: juns sdk pay info.
 * Data：25/09/2018-4:11 PM
 * Author: ranger
 */
public class JunSPayInfo {
    private float payMoney;
    private String payOrderId;
    private String payOrderName;
    private String payExt;
    private String payRoleId;
    private String payRoleName;
    private int payRoleLevel;
    private String payServerId;
    private String payServerName;
    private int payRoleVip;
    private int payRoleBalance;
    private int payRate;
    private String mPayData = "";

    boolean checkParam() {
        boolean checkOK = true;

        if (payMoney == -1) {
            checkOK = false;
        }

        if (TextUtils.isEmpty(payOrderId)) {
            checkOK = false;
        }

        if (TextUtils.isEmpty(payOrderName)) {
            checkOK = false;
        }

        if (TextUtils.isEmpty(payRoleId)) {
            checkOK = false;
        }
        if (TextUtils.isEmpty(payRoleName)) {
            checkOK = false;
        }
        if (TextUtils.isEmpty(payServerId)) {
            checkOK = false;
        }
        if (TextUtils.isEmpty(payServerName)) {
            checkOK = false;
        }
        return checkOK;
    }

    void debugParam(Activity act, final ConfirmDialog.ConfirmCallback callback) {
        LinkedHashMap<String, String> mustParams = new LinkedHashMap<>();
        LinkedHashMap<String, String> optionalParams = new LinkedHashMap<>();

        if (payMoney == -1) {
            mustParams.put("payMoney", "金额值为空，此项【必传】");
        }

        if (TextUtils.isEmpty(payOrderId)) {
            mustParams.put("payOrderId", "CP订单号值为空，此项【必传】");
        }

        if (TextUtils.isEmpty(payOrderName)) {
            mustParams.put("payOrderName", "商品名称值为空，此项【必传】");
        }

        if (TextUtils.isEmpty(payRoleId)) {
            mustParams.put("payRoleId", "角色ID值为空，此项【必传】");
        }
        if (TextUtils.isEmpty(payRoleName)) {
            mustParams.put("payRoleName", "角色名值为空，此项【必传】");
        }
        if (TextUtils.isEmpty(payServerId)) {
            mustParams.put("payServerId", "区服ID值为空，此项【必传】");
        }
        if (TextUtils.isEmpty(payServerName)) {
            mustParams.put("payServerName", "区服名值为空，此项【必传】");
        }

        //可选项
        if (TextUtils.isEmpty(payExt)) {
            optionalParams.put("payExt", "扩展参数值为空，此项【选传】");
        }
        if (payRoleLevel == -1) {
            optionalParams.put("payRoleLevel", "角色等级值为空，此项【选传】");
        }
        if (payRoleVip == -1) {
            optionalParams.put("payRoleVip", "角色VIP等级值为空，此项【选传】");
            payRoleVip = 0;
        }
        if (payRoleBalance == -1) {
            optionalParams.put("payRoleBalance", "角色钱包余额值为空，此项【选传】");
            payRoleBalance = 0;
        }

        if ( JunSSdkApplication.getDebugConfig() && (!mustParams.isEmpty() || !optionalParams.isEmpty())) {
            SpannableStringBuilder spannedBuilder = new SpannableStringBuilder();
            SpannableString mustContentSS = null;
            SpannableString mustTitleSS = null;
            SpannableString optionContentSS = null;
            SpannableString optionTitleSS = null;
            SpannableString lineSS = null;
            if (!mustParams.isEmpty()) {
                mustTitleSS = new SpannableString("必传参数错误信息，需修复！！\n\n");
                mustTitleSS.setSpan(new ForegroundColorSpan(Color.parseColor("#FB4F4F")), 0, mustTitleSS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mustTitleSS.setSpan(new StyleSpan(Typeface.BOLD), 0, mustTitleSS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                StringBuilder mustSb = new StringBuilder();
                for (Map.Entry<String, String> param : mustParams.entrySet()) {
                    mustSb.append(param.getKey() + " --> ");
                    mustSb.append(param.getValue() + "\n");
                }
                mustContentSS = new SpannableString(mustSb.toString());
                mustContentSS.setSpan(new ForegroundColorSpan(Color.parseColor("#FB4F4F")), 0, mustContentSS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (!optionalParams.isEmpty()) {
                    lineSS = new SpannableString("\n----->分割线<-----\n\n");
                    lineSS.setSpan(new ForegroundColorSpan(Color.parseColor("#51504F")), 0, lineSS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            if (!optionalParams.isEmpty()) {
                optionTitleSS = new SpannableString("可选参数提示信息，尽量传，请留意！！\n\n");
                optionTitleSS.setSpan(new ForegroundColorSpan(Color.parseColor("#CCCCCC")), 0, optionTitleSS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                optionTitleSS.setSpan(new StyleSpan(Typeface.BOLD), 0, optionTitleSS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                StringBuilder optionSb = new StringBuilder();
                for (Map.Entry<String, String> param : optionalParams.entrySet()) {
                    optionSb.append(param.getKey() + " --> ");
                    optionSb.append(param.getValue() + "\n");
                }
                optionContentSS = new SpannableString(optionSb.toString());
                optionContentSS.setSpan(new ForegroundColorSpan(Color.parseColor("#CCCCCC")), 0, optionContentSS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (mustContentSS != null && mustTitleSS != null) {
                spannedBuilder.append(mustTitleSS).append(mustContentSS);
            }

            if (lineSS != null) {
                spannedBuilder.append(lineSS);
            }

            if (optionTitleSS != null && optionContentSS != null) {
                spannedBuilder.append(optionTitleSS).append(optionContentSS);
            }

            ViewUtils.showConfirmDialog(act, spannedBuilder, false, callback);
        } else {
            callback.onConfirm();
        }
    }

    public float getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(float payMoney) {
        this.payMoney = payMoney;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public String getPayOrderName() {
        return payOrderName;
    }

    public String getPayExt() {
        return payExt;
    }

    public String getPayRoleId() {
        return payRoleId;
    }

    public String getPayRoleName() {
        return payRoleName;
    }

    public int getPayRoleLevel() {
        return payRoleLevel;
    }

    public String getPayServerId() {
        return payServerId;
    }

    public String getPayServerName() {
        return payServerName;
    }

    public int getPayRoleVip() {
        return payRoleVip;
    }

    public int getPayRoleBalance() {
        return payRoleBalance;
    }

    public int getPayRate() {
        return payRate;
    }

    public String getmPayData() {
        return mPayData;
    }

    public void setmPayData(String mPayData) {
        this.mPayData = mPayData;
    }

    public HashMap<String, String> toHash() {
        HashMap<String, String> info = new HashMap<>();
        info.put(JunSConstants.PAY_ORDER_ID, payOrderId);
        info.put(JunSConstants.PAY_MONEY, payMoney + "");
        info.put(JunSConstants.PAY_ORDER_NAME, payOrderName);
        info.put(JunSConstants.PAY_EXT, payExt);
        info.put(JunSConstants.PAY_ROLE_ID, payRoleId);
        info.put(JunSConstants.PAY_ROLE_NAME, payRoleName);
        info.put(JunSConstants.PAY_ROLE_LEVEL, payRoleLevel + "");
        info.put(JunSConstants.PAY_SERVER_ID, payServerId);
        info.put(JunSConstants.PAY_SERVER_NAME, payServerName);
        info.put(JunSConstants.PAY_ROLE_VIP, payRoleVip + "");
        info.put(JunSConstants.PAY_ROLE_BALANCE, payRoleBalance + "");
        info.put(JunSConstants.PAY_RATE, payRate + "");
        return info;
    }

    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            HashMap<String, String> info = new HashMap<>();
            data.put(JunSConstants.PAY_ORDER_ID, payOrderId);
            data.put(JunSConstants.PAY_MONEY, payMoney + "");
            data.put(JunSConstants.PAY_ORDER_NAME, payOrderName);
            data.put(JunSConstants.PAY_EXT, payExt);
            data.put(JunSConstants.PAY_ROLE_ID, payRoleId);
            data.put(JunSConstants.PAY_ROLE_NAME, payRoleName);
            data.put(JunSConstants.PAY_ROLE_LEVEL, payRoleLevel + "");
            data.put(JunSConstants.PAY_SERVER_ID, payServerId);
            data.put(JunSConstants.PAY_SERVER_NAME, payServerName);
            data.put(JunSConstants.PAY_ROLE_VIP, payRoleVip);
            data.put(JunSConstants.PAY_ROLE_BALANCE, payRoleBalance);
            info.put(JunSConstants.PAY_RATE, payRate + "");
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static class PayBuilder {
        private float payMoney = -1;
        private String payOrderId = null;
        private String payOrderName = null;
        private String payExt = null;
        private String payRoleId = null;
        private String payRoleName = null;
        private int payRoleLevel = -1;
        private String payServerId = null;
        private String payServerName = null;
        private int payRoleVip = -1;
        private int payRoleBalance = -1;
        private int payRate = 10;

        public PayBuilder payMoney(float money) {
            this.payMoney = money;
            return this;
        }

        public PayBuilder payOrderId(String orderId) {
            this.payOrderId = orderId;
            return this;
        }

        public PayBuilder payOrderName(String orderName) {
            this.payOrderName = orderName;
            return this;
        }

        public PayBuilder payExt(String ext) {
            this.payExt = ext;
            return this;
        }

        public PayBuilder payRoleId(String roleId) {
            this.payRoleId = roleId;
            return this;
        }

        public PayBuilder payRoleName(String roleName) {
            this.payRoleName = roleName;
            return this;
        }

        public PayBuilder payRoleLevel(int roleLevel) {
            this.payRoleLevel = roleLevel;
            return this;
        }

        public PayBuilder payServerId(String serverId) {
            this.payServerId = serverId;
            return this;
        }

        public PayBuilder payServerName(String serverName) {
            this.payServerName = serverName;
            return this;
        }

        public PayBuilder payRoleVip(int vip) {
            this.payRoleVip = vip;
            return this;
        }

        public PayBuilder payRoleBalance(int balance) {
            this.payRoleBalance = balance;
            return this;
        }

        @Deprecated
        public PayBuilder payRate(int rate) {
            this.payRate = rate;
            return this;
        }

        public JunSPayInfo build() {
            JunSPayInfo payInfo = new JunSPayInfo();
            payInfo.payMoney = payMoney;
            payInfo.payOrderId = payOrderId;
            payInfo.payOrderName = payOrderName;
            payInfo.payExt = payExt;
            payInfo.payRoleId = payRoleId;
            payInfo.payRoleName = payRoleName;
            payInfo.payRoleLevel = payRoleLevel;
            payInfo.payServerId = payServerId;
            payInfo.payServerName = payServerName;
            payInfo.payRoleVip = payRoleVip;
            payInfo.payRoleBalance = payRoleBalance;
            payInfo.payRate = payRate;
            return payInfo;
        }
    }
}
