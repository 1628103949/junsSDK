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
 * Description: juns sdk submit info.
 * Data：25/09/2018-4:11 PM
 * Author: ranger
 */
public class JunSSubmitInfo {
    private String submitRoleId;
    private String submitRoleName;
    private int submitRoleLevel;
    private String submitServerId;
    private String submitServerName;
    private int submitBalance;
    private int submitVip;
    private String submitParty;
    private long submitTimeCreate;
    private long submitTimeUp;
    private String submitLastRoleName;
    private String submitExt;
    private String submitType;
    private String submitPower;

    boolean checkParam() {
        boolean checkOK = true;
        if (TextUtils.isEmpty(submitRoleId)) {
            checkOK = false;
        }
        if (TextUtils.isEmpty(submitRoleName)) {
            checkOK = false;
        }
        if (TextUtils.isEmpty(submitServerId)) {
            checkOK = false;
        }
        if (TextUtils.isEmpty(submitServerName)) {
            checkOK = false;
        }
        if (TextUtils.isEmpty(submitType)) {
            checkOK = false;
        } else {
            if (!submitType.equals(JunSConstants.SUBMIT_TYPE_CREATE) &&
                    !submitType.equals(JunSConstants.SUBMIT_TYPE_ENTER) &&
                    !submitType.equals(JunSConstants.SUBMIT_TYPE_UPGRADE) &&
                    !submitType.equals(JunSConstants.SUBMIT_TYPE_UPDATE)) {
                checkOK = false;
            }

            if (TextUtils.isEmpty(submitLastRoleName) && submitType.equals(JunSConstants.SUBMIT_TYPE_UPDATE)) {
                checkOK = false;
            }
        }
        return checkOK;
    }

    void debugParam(Activity act, ConfirmDialog.ConfirmCallback callback) {
        LinkedHashMap<String, String> mustParams = new LinkedHashMap<>();
        LinkedHashMap<String, String> optionalParams = new LinkedHashMap<>();

        if (TextUtils.isEmpty(submitRoleId)) {
            mustParams.put("submitRoleId", "角色ID值为空，此项为【必传】");
        }
        if (TextUtils.isEmpty(submitRoleName)) {
            mustParams.put("submitRoleName", "角色名值为空，此项为【必传】");
        }
        if (TextUtils.isEmpty(submitServerId)) {
            mustParams.put("submitServerId", "区服ID值为空，此项【必传】");
        }
        if (TextUtils.isEmpty(submitServerName)) {
            mustParams.put("submitServerName", "区服名值为空，此项【必传】");
        }
        if (TextUtils.isEmpty(submitType)) {
            mustParams.put("submitType", "提交类型值为空，此项【必传】");
        } else {
            if (!submitType.equals(JunSConstants.SUBMIT_TYPE_CREATE) &&
                    !submitType.equals(JunSConstants.SUBMIT_TYPE_ENTER) &&
                    !submitType.equals(JunSConstants.SUBMIT_TYPE_UPGRADE) &&
                    !submitType.equals(JunSConstants.SUBMIT_TYPE_UPDATE)) {
                mustParams.put("submitType", "提交类型值不在定义范围");
            }

            if (TextUtils.isEmpty(submitLastRoleName) && submitType.equals(JunSConstants.SUBMIT_TYPE_UPDATE)) {
                mustParams.put("submitLastRoleName", "原角色名为空，此项【必传】");
            }
        }

        //可选项
        if (submitRoleLevel == -1) {
            optionalParams.put("submitRoleLevel", "角色等级值为空，此项【选传】");
        }
        if (submitBalance == -1) {
            optionalParams.put("submitBalance", "用户余额值为空，此项【选传】");
        }
        if (submitVip == -1) {
            optionalParams.put("submitVip", "玩家VIP等级值为空，此项【选传】");
        }
        if (TextUtils.isEmpty(submitParty)) {
            optionalParams.put("submitParty", "玩家帮派值为空，此项【选传】");
        }
        if (TextUtils.isEmpty(submitPower)) {
            optionalParams.put("submitPower", "玩家战斗力，此项【选传】");
        }
        if (submitParty.equals("无")) {
            optionalParams.put("submitParty", "玩家帮派值为空，此项【选传】");
        }

        if (submitTimeCreate == -1) {
            optionalParams.put("submitTimeCreate", "角色创建时间值为空，此项【选传】");
        }
        if (submitTimeUp == -1 && submitType != null && submitType.equals(JunSConstants.SUBMIT_TYPE_UPGRADE)) {
            optionalParams.put("submitTimeCreate", "角色升级时间值为空，此项【选传】");
        }
        if (TextUtils.isEmpty(submitExt)) {
            optionalParams.put("submitExt", "扩展参数值为空，此项【选传】");
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

    public String getSubmitRoleId() {
        return submitRoleId;
    }

    public String getSubmitRoleName() {
        return submitRoleName;
    }

    public int getSubmitRoleLevel() {
        return submitRoleLevel;
    }

    public String getSubmitServerId() {
        return submitServerId;
    }

    public String getSubmitServerName() {
        return submitServerName;
    }

    public int getSubmitBalance() {
        return submitBalance;
    }

    public int getSubmitVip() {
        return submitVip;
    }

    public String getSubmitParty() {
        return submitParty;
    }

    public long getSubmitTimeCreate() {
        return submitTimeCreate;
    }

    public long getSubmitTimeUp() {
        return submitTimeUp;
    }

    public String getSubmitLastRoleName() {
        return submitLastRoleName;
    }

    public String getSubmitExt() {
        return submitExt;
    }

    public String getSubmitType() {
        return submitType;
    }

    public HashMap<String, String> toHash() {
        HashMap<String, String> info = new HashMap<>();
        info.put(JunSConstants.SUBMIT_ROLE_ID, submitRoleId);
        info.put(JunSConstants.SUBMIT_ROLE_NAME, submitRoleName);
        info.put(JunSConstants.SUBMIT_ROLE_LEVEL, submitRoleLevel + "");
        info.put(JunSConstants.SUBMIT_SERVER_ID, submitServerId);
        info.put(JunSConstants.SUBMIT_SERVER_NAME, submitServerName);
        info.put(JunSConstants.SUBMIT_BALANCE, submitBalance + "");
        info.put(JunSConstants.SUBMIT_VIP, submitVip + "");
        info.put(JunSConstants.SUBMIT_PARTY_NAME, submitParty);
        info.put(JunSConstants.SUBMIT_POWER, submitPower);
        info.put(JunSConstants.SUBMIT_EXT, submitExt);
        info.put(JunSConstants.SUBMIT_CREATE_TIME, String.valueOf(submitTimeCreate));
        info.put(JunSConstants.SUBMIT_UP_TIME, String.valueOf(submitTimeUp));
        info.put(JunSConstants.SUBMIT_LAST_ROLE_NAME, submitLastRoleName);
        info.put(JunSConstants.SUBMIT_TYPE, submitType);
        return info;
    }

    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            data.put(JunSConstants.SUBMIT_ROLE_ID, submitRoleId);
            data.put(JunSConstants.SUBMIT_ROLE_NAME, submitRoleName);
            data.put(JunSConstants.SUBMIT_ROLE_LEVEL, submitRoleLevel);
            data.put(JunSConstants.SUBMIT_SERVER_ID, submitServerId);
            data.put(JunSConstants.SUBMIT_SERVER_NAME, submitServerName);
            data.put(JunSConstants.SUBMIT_BALANCE, submitBalance);
            data.put(JunSConstants.SUBMIT_VIP, submitVip);
            data.put(JunSConstants.SUBMIT_PARTY_NAME, submitParty);
            data.put(JunSConstants.SUBMIT_POWER, submitPower);
            data.put(JunSConstants.SUBMIT_EXT, submitExt);
            data.put(JunSConstants.SUBMIT_CREATE_TIME, submitTimeCreate);
            data.put(JunSConstants.SUBMIT_UP_TIME, submitTimeUp);
            data.put(JunSConstants.SUBMIT_LAST_ROLE_NAME, submitLastRoleName);
            data.put(JunSConstants.SUBMIT_TYPE, submitType);
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static class SubmitBuilder {
        private String submitRoleId = null;
        private String submitRoleName = null;
        private int submitRoleLevel = -1;
        private String submitServerId = null;
        private String submitServerName = null;
        private int submitBalance = -1;
        private int submitVip = -1;
        private String submitParty = "无";
        private long submitTimeCreate = -1;
        private long submitTimeUp = -1;
        private String submitLastRoleName = null;
        private String submitExt = null;
        private String submitType = null;
        private String submitPower = null;

        public SubmitBuilder submitRoleId(String roleId) {
            this.submitRoleId = roleId;
            return this;
        }

        public SubmitBuilder submitRoleName(String roleName) {
            this.submitRoleName = roleName;
            return this;
        }

        public SubmitBuilder submitRoleLevel(int roleLevel) {
            this.submitRoleLevel = roleLevel;
            return this;
        }

        public SubmitBuilder submitServerId(String serverId) {
            this.submitServerId = serverId;
            return this;
        }

        public SubmitBuilder submitServerName(String serverName) {
            this.submitServerName = serverName;
            return this;
        }

        public SubmitBuilder submitBalance(int balance) {
            this.submitBalance = balance;
            return this;
        }

        public SubmitBuilder submitVip(int vip) {
            this.submitVip = vip;
            return this;
        }

        public SubmitBuilder submitParty(String party) {
            this.submitParty = party;
            return this;
        }
        public SubmitBuilder submitPower(String power) {
            this.submitPower = power;
            return this;
        }

        public SubmitBuilder submitTimeCreate(long timeCreate) {
            this.submitTimeCreate = timeCreate;
            return this;
        }

        public SubmitBuilder submitTimeUp(long timeUp) {
            this.submitTimeUp = timeUp;
            return this;
        }

        public SubmitBuilder submitLastRoleName(String lastRoleName) {
            this.submitLastRoleName = lastRoleName;
            return this;
        }

        public SubmitBuilder submitExt(String ext) {
            this.submitExt = ext;
            return this;
        }

        public SubmitBuilder submitType(String type) {
            this.submitType = type;
            return this;
        }

        public JunSSubmitInfo build() {
            JunSSubmitInfo submitInfo = new JunSSubmitInfo();
            submitInfo.submitRoleId = submitRoleId;
            submitInfo.submitRoleName = submitRoleName;
            submitInfo.submitRoleLevel = submitRoleLevel;
            submitInfo.submitServerId = submitServerId;
            submitInfo.submitServerName = submitServerName;
            submitInfo.submitBalance = submitBalance;
            submitInfo.submitVip = submitVip;
            submitInfo.submitParty = submitParty;
            submitInfo.submitPower = submitPower;
            submitInfo.submitTimeCreate = submitTimeCreate;
            submitInfo.submitTimeUp = submitTimeUp;
            submitInfo.submitLastRoleName = submitLastRoleName;
            submitInfo.submitExt = submitExt;
            submitInfo.submitType = submitType;
            return submitInfo;
        }
    }

}

