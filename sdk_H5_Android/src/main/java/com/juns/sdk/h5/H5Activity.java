package com.juns.sdk.h5;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.webkit.ValueCallback;
import android.widget.RelativeLayout;

import com.dldl.quwan.h5.R;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSSdkApi;
import com.juns.sdk.core.api.callback.JunSCallback;
import com.juns.sdk.core.api.callback.JunSLogoutCallback;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.webview.SdkWebViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description: H5Activity
 * Data：20/09/2018-2:24 PM
 * Author: ranger
 */
public class H5Activity extends Activity {
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private SdkWebViewHolder sdkWebViewHolder;
    private RelativeLayout contentRl;
    private String appkey = "2020AndroidDemoKeyAppkey";
    private RelativeLayout headerContentRl;
    private String loadUrl = "http://www.520poker.com/jssdk/dist/?pid=45&gid=1000009&refer=45_1000009_0_0_0&sdkver=1.0.1&version=1.0.1";

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.quwan_h5_act);
        doSDKInit();
        JunSSdkApi.getInstance().sdkOnCreate(H5Activity.this);
        JunSSdkApi.getInstance().setLogoutCallback(new JunSLogoutCallback() {
            @Override
            public void onLogout() {
                //SDK注销成功
                //【注意】游戏收到回调后，先回调游戏登录界面，再调用SDK登录方法，重新进游戏
                //再次调用SDK登录方法，重新登录
                JunSSdkApi.getInstance().sdkLogin(H5Activity.this, new JunSCallback() {
                    @Override
                    public void onSuccess(String userInfo) {
                        //SDK登录成功，登录成功userInfo里包含帐号的token以及帐号相关信息，详情参考文档。
                        //【注意】：
                        //1. 获取到token后，游戏用该token通过服务端验证接口获取真实的uid，具体参考服务端接入文档；
                        try {
                            JSONObject userJson = new JSONObject(userInfo);
                            String token = userJson.getString("token");
                            String userId = userJson.getString("userId");
                            String userName = userJson.getString("userName");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        //SDK登录失败，message中为失败原因具体信息
                        //建议游戏收到此回调后，无需提示原因信息给玩家，重新调用SDK登录接口。

                    }
                });
            }
        });
        headerContentRl = findViewById(ResUtil.getID("header_rl", H5Activity.this));
        contentRl = findViewById(ResUtil.getID("content_rl", H5Activity.this));
        //SDK初始化

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        JunSSdkApi.getInstance().sdkOnRestart(H5Activity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        JunSSdkApi.getInstance().sdkOnStart(H5Activity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JunSSdkApi.getInstance().sdkOnResume(H5Activity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JunSSdkApi.getInstance().sdkOnPause(H5Activity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        JunSSdkApi.getInstance().sdkOnStop(H5Activity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JunSSdkApi.getInstance().sdkOnDestroy(H5Activity.this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JunSSdkApi.getInstance().sdkOnActivityResult(H5Activity.this, requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JunSSdkApi.getInstance().sdkOnNewIntent(H5Activity.this, intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        JunSSdkApi.getInstance().sdkOnConfigurationChanged(H5Activity.this, newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //targetApi>=23 必须接入
        JunSSdkApi.getInstance().sdkOnRequestPermissionsResult(H5Activity.this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    private void doSDKInit() {
        JunSSdkApi.getInstance().sdkInit(H5Activity.this, appkey, new JunSCallback() {
            @Override
            public void onSuccess(String intInfo) {
                //SDK初始化成功，收到此回调后，游戏可往下继续进行相关操作。
                //游戏可从回调info中获取sdkDeviceId
                try {
                    JSONObject initJson = new JSONObject(intInfo);
                    String sdkDeviceId = initJson.getString("sdkDeviceId");
                    initWebView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int code, String msg) {
                //SDK初始化失败，游戏可重新调用SDK初始化接口。

//                doSDKInit();
            }
        });
    }

    private void initWebView() {
        sdkWebViewHolder = new SdkWebViewHolder(H5Activity.this, false);
        //添加webView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);
        //处理通用界面开放的接口
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new H5Activity.WebInterface(), JunSConstants.JUNS_WEB_OBJ);
        sdkWebViewHolder.loadUrl(loadUrl);
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
         * 登录
         */
        @JavascriptInterface
        public void callLogin() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    JunSSdkApi.getInstance().sdkLogin(H5Activity.this, new JunSCallback() {
                        @Override
                        public void onSuccess(String userInfo) {
                            //SDK登录成功，登录成功userInfo里包含帐号的token以及帐号相关信息，详情参考文档。
                            //【注意】：
                            //1. 获取到token后，游戏用该token通过服务端验证接口获取真实的uid，具体参考服务端接入文档；
                            try {
                                JSONObject userJson = new JSONObject(userInfo);
                                String token = userJson.getString("token");
                                String userId = userJson.getString("userId");
                                String userName = userJson.getString("userName");
                                String js = "javascript:showInfoFromJava('" + userJson.toString() + "')";
                                sdkWebViewHolder.getSdkWebView().evaluateJavascript(js, new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {

                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(int code, String msg) {
                            //SDK登录失败，message中为失败原因具体信息
                            //建议游戏收到此回调后，无需提示原因信息给玩家，重新调用SDK登录接口。

                        }
                    });
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

