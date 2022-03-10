package com.juns.sdk.h5;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.RelativeLayout;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.permission.PermissionConstants;
import com.juns.sdk.framework.permission.PermissionUtils;
import com.juns.sdk.framework.utils.Utils;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.wzzx.juns.h5.R;

import java.util.List;


/**
 * Description: H5Activity
 * Data：20/09/2018-2:24 PM
 * Author: ranger
 */
public class H5Activity extends Activity {
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private SdkWebViewHolder sdkWebViewHolder;
    private RelativeLayout contentRl;
    private RelativeLayout headerContentRl;
    private String loadUrl;
    private int showSplash;
    private int isLand;
    private SplashDialog mSplashDialog;
    private PlatformConfig platformConfig;
    private ActivityParam activityParam = new ActivityParam();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This work only for android 4.4+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        //initConfig();
        requestPhonePermission();

    }

    private void initConfig(){
        activityParam.init(this);
        //new MiIdHelper().getDeviceIds(this);
        platformConfig = PlatformConfig.init(this);
        loadUrl = platformConfig.getH5Link() + activityParam.getParam();
        //Log.e("guoinfo","loadUrl:"+loadUrl);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.juns_h5_act);
        headerContentRl = findViewById(ResUtil.getID("header_rl", H5Activity.this));
        contentRl = findViewById(ResUtil.getID("content_rl", H5Activity.this));
        showSplash = platformConfig.getShowSplash();
        isLand = platformConfig.getIsLand();
        if (showSplash == 1){
            showSplash();
        }
        //SDK初始化
        initWebView();
    }

    public void requestPhonePermission(){

        PermissionUtils.permission(PermissionConstants.PHONE)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(final ShouldRequest shouldRequest) {

                        shouldRequest.again(false);
                        //requestSDCardPermission();
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        initConfig();
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever,
                                         List<String> permissionsDenied) {
                        initConfig();

                    }
                })
                .request();

    }





    //dismiss闪屏
    public void dismissInitSplash() {
        if (mSplashDialog != null && mSplashDialog.isShowing()) {
            mSplashDialog.dismiss();
        }
        mSplashDialog = null;
    }
    private void initWebView() {
        sdkWebViewHolder = new SdkWebViewHolder(H5Activity.this, false);
        //添加webView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);
        //处理通用界面开放的接口
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new WebInterface(), "JunS");
        sdkWebViewHolder.loadUrl(loadUrl);
    }

    private void showSplash(){
        mSplashDialog = new SplashDialog(H5Activity.this, new SplashDialog.SplashCallback() {
            @Override
            public void onFinish() {
                //闪屏流程完成后，往下执行初始化流程

            }
        });
        try {
            if (!mSplashDialog.isShowing()) {
                mSplashDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //dismiss闪屏
                dismissInitSplash();
            }
        }, 1500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                AlertDialog.Builder build = new AlertDialog.Builder(this);
                build.setTitle("系统提示").setMessage("确定要退出吗？");
                build.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                build.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }


    private class WebInterface {
        //默认样式
        //1. 无Title导航栏
        //2. 全屏透明
        //3. 可返回键取消

        /**
         * 隐藏title，全屏透明
         */
        @JavascriptInterface
        public void hideTitle() {
            //透明，全屏
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    headerContentRl.setVisibility(View.GONE);
                }
            });
        }

        /**
         * 退出游戏
         */
        @JavascriptInterface
        public void exitGame() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //Bus.getDefault().post(EvExit.getSucc());
                }
            });
        }

        /**
         * 显示title
         *
         * @param title 显示的标题
         */
        @JavascriptInterface
        public void showTitle(final String title) {
            //背景白色，带有title
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    headerContentRl.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(title)) {
                        //titleTv.setText(title);
                    }
                }
            });
        }

        /**
         * 关闭
         */
        @JavascriptInterface
        public void close() {
            //关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //NoticeDialog.this.dismiss();
                }
            });
        }

        /**
         * 设置是否可返回取消
         *
         * @param cancelable 是否可取消，true可以，false不可以
         */
        @JavascriptInterface
        public void setCancelable(final boolean cancelable) {
            //设置是否可返回键关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //NoticeDialog.this.setCancelable(cancelable);
                }
            });
        }
//        @JavascriptInterface
//        public void showPayDialog(String payUrl) {
//            if (payDialog != null) {
//                if (payDialog.isShowing()) {
//                    payDialog.dismiss();
//                }
//            }
//            payDialog = null;
//            payDialog = new PayDialog(H5Activity.this, payUrl, payCallback);
//            payDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
//        }

    }
}

