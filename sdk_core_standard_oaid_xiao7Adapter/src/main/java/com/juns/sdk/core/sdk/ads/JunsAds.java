package com.juns.sdk.core.sdk.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.framework.utils.ReflectUtils;

/**
 * Data：2020-06-07-21:33
 * Author: ranger
 */
public class JunsAds implements IAds {
    private IAds mAds;

    private JunsAds() {

    }

    public static JunsAds getInstance() {
        return JunsAds.SingletonHolder.INSTANCE;
    }

    @Override
    public void onApplication(Application application) {
        if (mAds == null) {
            mAds = getRealAds();
        }
        if (mAds != null) {
            SDKCore.logger.print("ads --> onApplication");
            mAds.onApplication(application);
        }
    }

    @Override
    public void initAds(Context ctx, String appId, String appName, String oaid, String refer) {
        if (mAds == null) {
            mAds = getRealAds();
        }

        if (mAds != null) {
            SDKCore.logger.print("ads --> initAds" +
                    "\nappId --> " + appId +
                    "\nappName --> " + appName +
                    "\noaid --> " + oaid +
                    "\nrefer --> " + refer);
            mAds.initAds(ctx, appId, appName, oaid, refer);
        }
    }

    @Override
    public void onEvResume(Activity act) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onEvResume");
            mAds.onEvResume(act);
        }
    }

    @Override
    public void onEvPause(Activity act) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onEvPause");
            mAds.onEvPause(act);
        }
    }

    @Override
    public void onEvActive(Activity act) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onEvActive");
            mAds.onEvActive(act);
        }
    }

    @Override
    public void onEvRegister(Activity act, String type) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onEvRegister");
            mAds.onEvRegister(act, type);
        }
    }

    @Override
    public void onEvLogin(Activity act, String userId) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onEvLogin");
            mAds.onEvLogin(act, userId);
        }
    }

    @Override
    public void onEvLoginSucc(Activity act, String userId) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onEvLoginSucc");
            mAds.onEvLoginSucc(act, userId);
        }
    }

    @Override
    public void onEvPay(Activity act, String goodsName, String payWay, String payMoney) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onEvPay" +
                    "\ngoodsName --> " + goodsName +
                    "\npayWay --> " + payWay +
                    "\npayMoney --> " + payMoney);
            mAds.onEvPay(act, goodsName, payWay, payMoney);
        }
    }

    @Override
    public void onRoleCreate(Activity act, String roleId, String roleName) {
        if (mAds != null) {
            SDKCore.logger.print("ads --> onRoleCreate" +
                    "\nroleId --> " + roleId +
                    "\nroleName --> " + roleName);
            mAds.onRoleCreate(act, roleId, roleName);
        }
    }

    private IAds getRealAds() {
        if (!TextUtils.isEmpty(SDKApplication.getPlatformConfig().getAdsClass())) {
            return (IAds) ReflectUtils.reflect(SDKApplication.getPlatformConfig().getAdsClass())
                    .newInstance()
                    .get();
        } else {
            return null;
        }
    }

    private static class SingletonHolder {
        private static final JunsAds INSTANCE = new JunsAds();
    }
}
