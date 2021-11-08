package com.juns.ads.toutiao;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.bytedance.applog.AppLog;
import com.bytedance.applog.GameReportHelper;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.util.UriConstants;
import com.juns.sdk.core.sdk.ads.IAds;

/**
 * Data：2020-06-07-22:42
 * Author: ranger
 */
public class TouTiaoAds implements IAds {


    @Override
    public void initAds(Context ctx, String appId, String appName, String oaid, String refer) {
        final InitConfig config = new InitConfig(appId, refer);
        config.setUriConfig(UriConstants.DEFAULT);
        //打开心跳模式
        config.setEnablePlay(true);
        //AppLog.setEnableLog(false);
        config.setAutoStart(true);
        //config.setAbEnable(true);
        AppLog.init(ctx, config);
    }

    @Override
    public void onEvResume(Activity act) {

    }

    @Override
    public void onEvPause(Activity act) {

    }

    @Override
    public void onEvActive(Activity act) {

    }

    @Override
    public void onEvRegister(Activity act, String type) {
        GameReportHelper.onEventRegister("type", true);
    }

    @Override
    public void onEvLogin(Activity act, String userId) {
        GameReportHelper.onEventLogin("", true);
    }

    @Override
    public void onEvLoginSucc(Activity act, String userId) {
        //设置帐号体系ID
        AppLog.setUserUniqueID(userId);
    }

    @Override
    public void onEvPay(Activity act, String goodsName, String payWay, String payMoney) {
        try {
            String preMoney = payMoney.substring(0, payMoney.indexOf("."));
            int money = Integer.parseInt(preMoney);
            GameReportHelper.onEventPurchase(goodsName, goodsName, "1", 1, payWay, "¥", true, money);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRoleCreate(Activity act, String roleId, String roleName) {
        GameReportHelper.onEventCreateGameRole(roleId);
    }
}
