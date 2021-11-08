package com.juns.sdk.h5;

import android.app.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSPayInfo;
import com.juns.sdk.core.api.JunSSdkApi;
import com.juns.sdk.core.api.JunSSubmitInfo;
import com.juns.sdk.core.api.callback.JunSCallback;
import com.juns.sdk.core.api.callback.JunSLogoutCallback;
import com.juns.sdk.core.api.callback.JunSPayCallback;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.common.ToastUtil;
import com.juns.sdk.framework.utils.Utils;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.mps.nznh.R;

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
    private RelativeLayout headerContentRl;
    private String loadUrl;
    private SafeInset safeInset = new SafeInset();
    private String appkey = "2020AndroidDemoKeyAppkey";
    private PlatformConfig platformConfig;
    private String sdkDeviceId;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            //下面图2
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getNotchParams();
        }

        Utils.init(this);
        platformConfig = PlatformConfig.init(this);
        loadUrl = platformConfig.getH5Link();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.juns_h5_act);
        headerContentRl = findViewById(ResUtil.getID("header_rl", H5Activity.this));
        contentRl = findViewById(ResUtil.getID("content_rl", H5Activity.this));
        //SDK初始化
        doSDKInit();
        JunSSdkApi.getInstance().setLogoutCallback(new JunSLogoutCallback() {
            @Override
            public void onLogout() {
                //SDK注销成功
                //【注意】游戏收到回调后，先回调游戏登录界面，再调用SDK登录方法，重新进游戏
                //再次调用SDK登录方法，重新登录
                String jsCode = "javascript:logoutLisenner ()";
                load(jsCode);
            }
        });
        JunSSdkApi.getInstance().sdkOnCreate(H5Activity.this);
    }
    private void doSDKInit() {
        JunSSdkApi.getInstance().sdkInit(H5Activity.this, appkey, new JunSCallback() {
            @Override
            public void onSuccess(String intInfo) {
                //SDK初始化成功，收到此回调后，游戏可往下继续进行相关操作。
                //游戏可从回调info中获取sdkDeviceId
                try {
                    JSONObject initJson = new JSONObject(intInfo);
                    sdkDeviceId = initJson.getString("sdkDeviceId");
                    initWebView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int code, String msg) {
                //SDK初始化失败，游戏可重新调用SDK初始化接口。
                //                doSDKInit();
                ToastUtil.showLong(H5Activity.this,"初始化异常，请联系客服");
            }
        });
    }
    public void getNotchParams() {
        final View decorView = getWindow().getDecorView();

        decorView.post(new Runnable() {
            @Override
            public void run() {
                WindowInsets rootWindowInsets = decorView.getRootWindowInsets();
                if (rootWindowInsets == null) {
                    Log.e("guoinfo", "rootWindowInsets为空了");
                    return;
                }
                DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
                if(displayCutout == null){
                    Log.e("guoinfo", "displayCutout为空了");
                    return;
                }
                safeInset.setLeft(displayCutout.getSafeInsetLeft());
                safeInset.setRight(displayCutout.getSafeInsetRight());
                safeInset.setTop(displayCutout.getSafeInsetTop());
                safeInset.setBottom(displayCutout.getSafeInsetBottom());
            }
        });
    }
    private void initWebView() {
        sdkWebViewHolder = new SdkWebViewHolder(H5Activity.this, false);
        //添加webView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);
        //处理通用界面开放的接口
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new H5Activity.WebInterface(), "JunS");
        sdkWebViewHolder.getSdkWebView().setBackgroundColor(Color.parseColor("#FFFFFF"));
        sdkWebViewHolder.loadUrl(loadUrl);
    }
    private void load(String jsCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sdkWebViewHolder.getSdkWebView().evaluateJavascript(jsCode, null);
        } else {
            sdkWebViewHolder.getSdkWebView().loadUrl(jsCode);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            H5ExitDialog.showExit(H5Activity.this, new H5ExitDialog.ExitCallback() {
                @Override
                public void toContinue() {

                }

                @Override
                public void toExit() {
                    System.exit(0);
                    finish();

                }
            });
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
    public void sdkPay(String payOrderId,float money,String orderName,String ext,String roleId,String roleName,int roleLevel,String serverId,String serverName,int roleVip,int roleBalance,int rate) {
        JunSSdkApi.getInstance().sdkPay(this, new JunSPayInfo.PayBuilder()
                //CP订单号，全局唯一，不可重复，【必传】
                .payOrderId(payOrderId)
                //充值金额，单位：元，【必传】
                .payMoney(money)
                //商品名称，【必传】
                .payOrderName(orderName)
                //CP扩展字段，长度255字符，无则不传，【选传】
                .payExt(ext)
                //角色ID，建议数字，【必传】
                .payRoleId(roleId)
                //角色名称，【必传】
                .payRoleName(roleName)
                //角色等级，无则不传，【选传】
                .payRoleLevel(roleLevel)
                //服务器ID，建议数字，【必传】
                .payServerId(serverId)
                //服务器名称，【必传】
                .payServerName(serverName)
                //角色VIP等级，【选传】
                .payRoleVip(roleVip)
                //角色钱包余额，【选传】
                .payRoleBalance(roleBalance)
                .payRate(rate)
                .build(), new JunSPayCallback() {
            @Override
            public void onFinish(int code, String msg) {
                //SDK支付完成，游戏按需进行处理，发货需以服务端回调为准
                String jsCode = "javascript:payLisenner ('"+code+"','"+msg+"')";
                load(jsCode);
                //Log.d(TAG, "SDK支付完成！" + "\ncode --> " + code + "\nmsg --> " + msg);
            }
        });
    }
    public void sdkSubmitInfo(int type,String roleId,String roleName,int roleLevel,String serverId,String serverName,int balance,int roleVip,String party,String roleCreateTime){
        String submitType="";
        if(type==0){
            submitType = JunSConstants.SUBMIT_TYPE_CREATE;
        }
        if(type==1){
            submitType = JunSConstants.SUBMIT_TYPE_ENTER;
        }
        if(type==2){
            submitType = JunSConstants.SUBMIT_TYPE_UPGRADE;
        }
        if(type==3){
            submitType = JunSConstants.SUBMIT_TYPE_UPDATE;
        }
        JunSSdkApi.getInstance().sdkSubmitInfo(this,
                new JunSSubmitInfo.SubmitBuilder()
                        //角色ID，建议数字【必传】
                        .submitRoleId(roleId)
                        //角色名称，【必传】
                        .submitRoleName(roleName)
                        //角色等级，无则不传，【选传】
                        .submitRoleLevel(roleLevel)
                        //服务器ID，建议数字，【必传】
                        .submitServerId(serverId)
                        //服务器名称，【必传】
                        .submitServerName(serverName)
                        //玩家余额，数字，无则不传，【选传】
                        .submitBalance(balance)
                        //玩家VIP等级，数字，无则不传，【选传】
                        .submitVip(roleVip)
                        //玩家帮派，无则不传，【选传】
                        .submitParty(party)
                        //角色创建时间，单位：秒，获取服务器存储的时间，不可用手机本地时间，无则不传，【选传】
                        .submitTimeCreate(Long.parseLong(roleCreateTime))
                        //CP扩展字段，无则不传，【选传】
                        // .submitExt("CP扩展字段")
                        .submitType(submitType)
                        .build(),
                new JunSCallback() {
                    @Override
                    public void onSuccess(String info) {
                        //Log.e("guoinfo","onSuccess");
                        //Log.d(TAG,"角色创建上传成功！"+info);
                        String jsCode = "javascript:submitLisenner ('"+"0"+"','"+info+"')";
                        load(jsCode);
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        //Log.e("guoinfo","onFail");
                        String jsCode = "javascript:submitLisenner ('"+code+"','"+msg+"')";
                        load(jsCode);
                        //Log.d(TAG,"角色创建上传失败！" + "\ncode --> " + code + "\nmsg --> " + msg);
                    }
                }
        );
    }
    private class WebInterface {

        @JavascriptInterface
        public void login() {
            //透明，全屏
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
                                String jsCode = "javascript:loginSuccessLisenner ('"+userId+"','"+userName+"','"+token+"')";
                                load(jsCode);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFail(int code, String msg) {
                            String jsCode = "javascript:loginFailLisenner ('"+code+"','"+msg+"')";
                            load(jsCode);
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
        public void logout() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    JunSSdkApi.getInstance().sdkLogout(H5Activity.this);
                }
            });
        }


        @JavascriptInterface
        public void pay(final String payInfo) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e("guoinfo",payInfo);
                        JSONObject params = new JSONObject(payInfo);
                        String cpOrderId = params.getString("cpOrderId");
                        float money =(float) params.getInt("amount");
                        String orderName =params.getString("orderName");
                        String ext=params.getString("ext");
                        String roleId=params.getString("roleId");
                        String roleName=params.getString("roleName");
                        int  roleLevel=params.getInt("roleLevel");
                        String serverId=params.getString("serverId");
                        String serverName=params.getString("serverName");
                        int  roleVip=params.getInt("roleVip");
                        int  roleBalance=params.getInt("roleBalance");
                        int  rate=params.getInt("rate");
                        sdkPay(cpOrderId,money,orderName,ext,roleId,roleName,roleLevel,serverId,serverName,roleVip,roleBalance,rate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        /**
         * 关闭
         */
        @JavascriptInterface
        public void submit(final String submitInfo) {
            //关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e("guoinfo",submitInfo);
                        JSONObject params = new JSONObject(submitInfo);
                        int type =params.getInt("type");
                        String roleId =params.getString("roleId");
                        String roleName=params.getString("roleName");
                        int roleLevel=params.getInt("roleLevel");
                        String serverId=params.getString("serverId");
                        String serverName=params.getString("serverName");
                        int roleVip=params.getInt("roleVip");
                        int roleBalance=params.getInt("roleBalance");
                        String party=params.getString("party");
                        String roleCreateTime=params.getString("roleCreateTime");
                        sdkSubmitInfo(type,roleId,roleName,roleLevel,serverId,serverName,roleBalance,roleVip,party,roleCreateTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        /**
         * 设置是否可返回取消
         *
         */
        @JavascriptInterface
        public void getConfig() {
            //设置是否可返回键关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String tinyConfig = JunSSdkApi.getInstance().sdkGetConfig(H5Activity.this);
                    try {
                        JSONObject configJson = new JSONObject(tinyConfig);
                        String game_id = configJson.getString("game_id");
                        String pt_id = configJson.getString("pid");
                        String refer = configJson.getString("refer");
                        String sdk_version = configJson.getString("sdk_version");
                        String jsCode = "javascript:configLisenner ('"+game_id+"','"+pt_id+"','"+refer+"','"+sdk_version+"','"+sdkDeviceId+"')";
                        load(jsCode);
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }

                }
            });
        }

        @JavascriptInterface
        public void getScreen() {
            //设置是否可返回键关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String jsCode = "javascript:screenLisenner ('"+safeInset.getLeft()+"','"+safeInset.getRight()+"','"+safeInset.getTop()+"','"+safeInset.getBottom()+"')";
                    load(jsCode);
                }
            });
        }
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
        //Log.e("guoinfo","下线上报开始");
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
}

