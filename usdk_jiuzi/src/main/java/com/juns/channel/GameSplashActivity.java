package com.juns.channel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.jzyx.sdk.open.SplashActivity;

public class GameSplashActivity extends SplashActivity {
    private String mainActivity;
    @Override
    public void gotoGame() {
        mainActivity = getMetaDataValue(getApplicationContext(),"mainAct");
        Intent intent = new Intent();
        intent.setClassName(getPackageName(),mainActivity);
        startActivity(intent);
        finish();
    }

    private String getMetaDataValue(Context context, String meatName) {
        String value = null;
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                Object object = applicationInfo.metaData.get(meatName);
                if (object != null) {
                    value = object.toString();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }
}
