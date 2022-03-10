package com.juns.sdk.demo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.InitInfoCallBack;
import com.juns.sdk.core.sdk.common.InitInfoDialog;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!SDKData.getSdkAgree()){
            InitInfoDialog.show(this, new InitInfoCallBack() {
                @Override
                public void toContinue() {
                    SDKData.setSdkAgree(true);
                    onSplashStop();
                }

                @Override
                public void toExit() {
                    finish();
                    System.exit(0);
                }
            });
        }else {
            onSplashStop();
        }
    }


    public void onSplashStop() {
        //闪屏结束后，跳转到游戏界面
        String mainActivity = getMetaDataValue(getApplicationContext(), "mainAct");
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), mainActivity);
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
