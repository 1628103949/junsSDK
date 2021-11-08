package com.juns.sdk.h5;

import android.app.Activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.RelativeLayout;


import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.utils.Utils;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.mps.nznh.R;


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
        Utils.init(this);
        platformConfig = PlatformConfig.init(this);
        loadUrl = platformConfig.getH5Link();
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
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new H5Activity.WebInterface(), "Enjoy");
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
            H5ExitDialog.showExit(H5Activity.this, new H5ExitDialog.ExitCallback() {
                @Override
                public void toContinue() {
                    //Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
                }

                @Override
                public void toExit() {
                    System.exit(0);
                    finish();
                    //YSDKApi.setAntiAddictGameEnd();
                    //Bus.getDefault().post(OExitEv.getSucc());
                }
            });
                return true;
        }
        return super.onKeyDown(keyCode, event);

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

