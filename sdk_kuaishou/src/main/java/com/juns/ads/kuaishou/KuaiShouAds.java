package com.juns.ads.kuaishou;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.juns.sdk.core.sdk.ads.IAds;
import com.kwai.monitor.log.OAIDProxy;
import com.kwai.monitor.log.TurboAgent;
import com.kwai.monitor.log.TurboConfig;

/**
 * Data：2020-06-07-22:42
 * Author: ranger
 */
public class KuaiShouAds implements IAds {
    @Override
    public void onApplication(Application application) {

    }

    @Override
    public void initAds(Context ctx, String appId, String appName, final String oaid, String refer) {

        TurboAgent.init(TurboConfig.TurboConfigBuilder.create(ctx)
                .setAppId(appId)
                .setAppName(appName)
                .setAppChannel(refer)
                .setEnableDebug(false)
                .setOAIDProxy(new OAIDProxy() {
                    //设置获取oaid的接⼝口
                    @Override
                    public String getOAID() { //请您将您app内获取的oaid在此⽅方法返回，SDK内需要时会调⽤用
                        //getOAID进⾏行行获取。未获取可以为空
                        return oaid;
                    }
                })
                .build());
    }

    @Override
    public void onEvResume(Activity act) {
        TurboAgent.onPageResume(act);
    }

    @Override
    public void onEvPause(Activity act) {
        TurboAgent.onPagePause(act);
    }

    @Override
    public void onEvActive(Activity act) {

    }

    @Override
    public void onEvRegister(Activity act, String type) {
        TurboAgent.onRegister();
    }

    @Override
    public void onEvLogin(Activity act, String userId) {

    }

    @Override
    public void onEvLoginSucc(Activity act, String userId) {

    }

    @Override
    public void onEvPay(Activity act, String goodsName, String payWay, String payMoney) {
        try {
            String preMoney = payMoney.substring(0, payMoney.indexOf("."));
            double money = Double.parseDouble(preMoney);
            //付费事件
            TurboAgent.onPay(money);
            //支付成功事件
            TurboAgent.onOrderPayed(money);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRoleCreate(Activity act, String roleId, String roleName) {
        TurboAgent.onGameCreateRole(roleName);
    }
}
