package com.juns.channel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;

import com.quicksdk.QuickSdkSplashActivity;
public class SplashActivity extends QuickSdkSplashActivity {
    private String mainActivity;

    @Override
    public void startSplash() {
        super.startSplash();
        mainActivity = getMetaDataValue(getApplicationContext(),"mainAct");
    }

    @Override
    public int getBackgroundColor() {
        return Color.WHITE;
    }

    @Override
    public void onSplashStop() {
        //闪屏结束后，跳转到游戏界面
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
