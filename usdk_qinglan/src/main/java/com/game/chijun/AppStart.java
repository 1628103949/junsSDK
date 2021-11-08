package com.game.chijun;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;

public class AppStart extends Activity {
    final Activity m_active = this;
    int failCount = 0;
    boolean m_showloading = false;
    boolean m_bchecking = false;
    // Setup activity layout
    @Override protected void onCreate (Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CheckAll();
    }

    @Override protected void onResume()
    {
        super.onResume();
		 new Thread(){
            public void run(){
               try {
                  Thread.sleep(1000);
					CheckAll();
               } catch (InterruptedException e) { }
            }
         }.start();
    }

    void  CheckAll(){
        if(m_showloading){
            return;
        }
		
        if(isstartgame){
            return;
        }

        if (m_bchecking){
            return;
        }
        m_bchecking = true;
        SoulPermission.getInstance().checkAndRequestPermissions(
                Permissions.build(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE),
                //if you want do noting or no need all the callbacks you may use SimplePermissionsAdapter instead
                new CheckRequestPermissionsListener() {
                    @Override
                    public void onAllPermissionOk(Permission[] allPermissions) {
                        m_bchecking = false;
                        startGame();
                    }

                    @Override
                    public void onPermissionDenied(Permission[] refusedPermissions) {
                        m_bchecking = false;
                        failCount = failCount + 1;
                        if(failCount > 4){
                            failCount = 0;
                            m_showloading = true;
                            AlertDialog alertDialog1 = new AlertDialog.Builder(m_active)
                                    .setTitle("提示")//标题
                                    .setMessage("需要权限才能继续，请在设置中打开权限")//内容
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            m_showloading = false;
                                            getAppDetailSettingIntent(m_active);
                                        }
                                    })
                                    .create();
                            alertDialog1.show();
                        }else {
                            // At least one permission is denied
                            if(!m_showloading){
                                CheckAll();
                            }
                        }
                    }
                });

//        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE)
//                .subscribe(granted -> {
//                    if (granted) {
//                        startGame();
//                    } else {
//                        failCount = failCount + 1;
//                        if(failCount > 4){
//                            failCount = 0;
//                            m_showloading = true;
//                            AlertDialog alertDialog1 = new AlertDialog.Builder(this)
//                                    .setTitle("提示")//标题
//                                    .setMessage("需要权限才能继续，请在设置中打开权限")//内容
//                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            m_showloading = false;
//                                            getAppDetailSettingIntent(m_active);
//                                        }
//                                    })
//                                    .create();
//                            alertDialog1.show();
//                        }else {
//                            // At least one permission is denied
//                            if(!m_showloading){
//                                CheckAll();
//                            }
//                        }
//                    }
//                });
    }
    private void getAppDetailSettingIntent(Context context){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT >= 9){
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if(Build.VERSION.SDK_INT <= 8){
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(intent);
    }

    private boolean isstartgame = false;
    public void startGame(){
        if (!isstartgame){
            isstartgame = true;
            Intent intent = new Intent(this, UnityPlayerActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
