package com.juns.sdk.h5;

import android.app.Activity;
import android.provider.Settings;
import android.text.TextUtils;

import com.juns.sdk.framework.common.Dev;
import com.juns.sdk.framework.common.TUitls;
import com.juns.sdk.framework.utils.DeviceUtils;
import com.juns.sdk.framework.utils.ScreenUtils;

public class ActivityParam {
    private String mac;
    private String imei;
    private String oaid;
    private String androidid;
    private String os;
    private String brand;
    private String resolution;
    private String model;
    private String sysver;
    private String ntype;

    public void init(Activity activity){

        if (TextUtils.isEmpty(SDKData.getSdkAndroidId())) {
            String androidId = Settings.System.getString(activity.getContentResolver(), Settings.System.ANDROID_ID);
            SDKData.setSdkAndroidId(androidId);
        }

        if (TextUtils.isEmpty(MiIdHelper.mOAID)) {
            SDKData.setSdkOaid("");
        } else {
            SDKData.setSdkOaid(MiIdHelper.mOAID);
        }

        mac = Dev.getMacAddress(activity);
        imei = Dev.getPhoneIMEI(activity);
        oaid = SDKData.getSdkOaid();
        androidid = SDKData.getSdkAndroidId();
        os = "1";
        brand = DeviceUtils.getManufacturer();
        resolution = ScreenUtils.getScreenWidth() + "x" + ScreenUtils.getScreenHeight();
        model = DeviceUtils.getModel();
        sysver = android.os.Build.VERSION.RELEASE;
        ntype = TUitls.getNetWorkTypeName();
    }

    public String getParam(){
        return "&" + "mac=" + mac +
                "&" + "imei=" + imei +
                "&" + "oaid=" + oaid +
                "&" + "androidid=" + androidid +
                "&" + "os=" + os +
                "&" + "brand=" + brand +
                "&" + "resolution=" + resolution +
                "&" + "model=" + model +
                "&" + "sysver=" + sysver +
                "&" + "ntype=" + ntype;

    }
}
