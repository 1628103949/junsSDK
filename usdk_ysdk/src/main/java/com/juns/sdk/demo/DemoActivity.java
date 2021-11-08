package com.juns.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSPayInfo;
import com.juns.sdk.core.api.JunSSdkApi;
import com.juns.sdk.core.api.JunSSubmitInfo;
import com.juns.sdk.core.api.callback.JunSCallback;
import com.juns.sdk.core.api.callback.JunSLogoutCallback;
import com.juns.sdk.core.api.callback.JunSPayCallback;
import com.tencent.tmgp.nznh.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description: DemoActivity
 * Data：20/09/2018-2:24 PM
 * Author: ranger
 */
public class DemoActivity extends Activity implements View.OnClickListener {
    private String appkey = "AppkeyNeZha";
    private Toast mToast;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private TextView retTv;

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

        setContentView(R.layout.juns_demo_act);
        retTv = (TextView) findViewById(R.id.retTv);

        //SDK初始化
        doSDKInit();

        JunSSdkApi.getInstance().setLogoutCallback(new JunSLogoutCallback() {
            @Override
            public void onLogout() {
                //SDK注销成功
                //【注意】游戏收到回调后，先回调游戏登录界面，再调用SDK登录方法，重新进游戏
                showToast("SDK注销成功！");
                //再次调用SDK登录方法，重新登录
                JunSSdkApi.getInstance().sdkLogin(DemoActivity.this, new JunSCallback() {
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
                            showToast("SDK登录成功：" +
                                    "\ntoken --> " + token +
                                    "\nuserId --> " + userId +
                                    "\nuserName --> " + userName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        //SDK登录失败，message中为失败原因具体信息
                        //建议游戏收到此回调后，无需提示原因信息给玩家，重新调用SDK登录接口。
                        showToast("SDK登录失败：" +
                                "\ncode --> " + code +
                                "\nmsg --> " + msg);
                    }
                });
            }
        });

        JunSSdkApi.getInstance().sdkOnCreate(DemoActivity.this);
        findViewById(R.id.sdkLoginBtn).setOnClickListener(this);
        findViewById(R.id.sdkLogoutBtn).setOnClickListener(this);
        findViewById(R.id.sdkPayBtn).setOnClickListener(this);
        findViewById(R.id.sdkExitBtn).setOnClickListener(this);
        findViewById(R.id.creatRoleBtn).setOnClickListener(this);
        findViewById(R.id.submitDataBtn).setOnClickListener(this);
        findViewById(R.id.upgradeDataBtn).setOnClickListener(this);
        findViewById(R.id.updateDataBtn).setOnClickListener(this);
        findViewById(R.id.getConfigBtn).setOnClickListener(this);
        findViewById(R.id.sdkIsLoginBtn).setOnClickListener(this);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        JunSSdkApi.getInstance().sdkOnRestart(DemoActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        JunSSdkApi.getInstance().sdkOnStart(DemoActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JunSSdkApi.getInstance().sdkOnResume(DemoActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JunSSdkApi.getInstance().sdkOnPause(DemoActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        JunSSdkApi.getInstance().sdkOnStop(DemoActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JunSSdkApi.getInstance().sdkOnDestroy(DemoActivity.this);
    }

    private void doSDKInit() {
        JunSSdkApi.getInstance().sdkInit(DemoActivity.this, appkey, new JunSCallback() {
            @Override
            public void onSuccess(String intInfo) {
                //SDK初始化成功，收到此回调后，游戏可往下继续进行相关操作。
                //游戏可从回调info中获取sdkDeviceId
                try {
                    JSONObject initJson = new JSONObject(intInfo);
                    String sdkDeviceId = initJson.getString("sdkDeviceId");
                    showToast("SDK初始化成功：" +
                            "\nsdkDeviceId --> " + sdkDeviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int code, String msg) {
                //SDK初始化失败，游戏可重新调用SDK初始化接口。
                showToast("SDK初始化失败，重新调用SDK初始化接口！" +
                        "\ncode --> " + code +
                        "\nmsg --> " + msg);
                doSDKInit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JunSSdkApi.getInstance().sdkOnActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JunSSdkApi.getInstance().sdkOnNewIntent(DemoActivity.this, intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        JunSSdkApi.getInstance().sdkOnConfigurationChanged(DemoActivity.this, newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //targetApi>=23 必须接入
        JunSSdkApi.getInstance().sdkOnRequestPermissionsResult(DemoActivity.this, requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sdkLoginBtn:
                JunSSdkApi.getInstance().sdkLogin(DemoActivity.this, new JunSCallback() {
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
                            showToast("SDK登录成功成功：" +
                                    "\ntoken --> " + token +
                                    "\nuserId --> " + userId +
                                    "\nuserName --> " + userName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        //SDK登录失败，message中为失败原因具体信息
                        //建议游戏收到此回调后，无需提示原因信息给玩家，重新调用SDK登录接口。
                        showToast("SDK登录失败：" +
                                "\ncode --> " + code +
                                "\nmsg --> " + msg);
                    }
                });
                break;

            case R.id.sdkLogoutBtn:
                //SDK 注销
                JunSSdkApi.getInstance().sdkLogout(DemoActivity.this);
                showToast("SDK已注销!");
                break;

            case R.id.sdkIsLoginBtn:
                //返回SDK是否已登录
                boolean isLogined = JunSSdkApi.getInstance().isLogined();
                showToast("SDK当前登录状态为：" + isLogined);
                break;

            case R.id.sdkPayBtn:
                float goodMoney = 0.1f;
                EditText idEv = (EditText) findViewById(R.id.goodsMoney);
                if (!TextUtils.isEmpty(idEv.getText().toString().trim())) {
                    try {
                        goodMoney = Float.parseFloat(idEv.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        showToast("请填写正确商品价格！");
                        return;
                    }
                }

                JunSSdkApi.getInstance().sdkPay(DemoActivity.this, new JunSPayInfo.PayBuilder()
                        //CP订单号，全局唯一，不可重复，【必传】
                        .payOrderId(System.currentTimeMillis() + "")
                        //充值金额，单位：元，【必传】
                        .payMoney(goodMoney)
                        //商品名称，【必传】
                        .payOrderName("一大袋钻石")
                        //CP扩展字段，长度255字符，无则不传，【选传】
                        .payExt("CP支付扩展字段")
                        //角色ID，建议数字，【必传】
                        .payRoleId("100011")
                        //角色名称，【必传】
                        .payRoleName("海绵宝宝爱睡觉")
                        //角色等级，无则不传，【选传】
                        .payRoleLevel(10)
                        //服务器ID，建议数字，【必传】
                        .payServerId("100000")
                        //服务器名称，【必传】
                        .payServerName("开天辟地")
                        //角色VIP等级，【选传】
                        .payRoleVip(1)
                        //角色钱包余额，【选传】
                        .payRoleBalance(100)
                        .payRate(10)
                        .build(), new JunSPayCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        //SDK支付完成，游戏按需进行处理，发货需以服务端回调为准
                        showToast("SDK支付完成！" +
                                "\ncode --> " + code +
                                "\nmsg --> " + msg);
                    }
                });
                break;

            case R.id.sdkExitBtn:
                JunSSdkApi.getInstance().sdkGameExit(DemoActivity.this, new JunSCallback() {
                    @Override
                    public void onSuccess(String info) {
                        //退出游戏成功，游戏在此进行退出游戏，销毁游戏资源相关操作。
                        showToast("SDK退出成功！");
                        DemoActivity.this.finish();
                        System.exit(1);
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        //游戏无需处理，继续游戏。
                        showToast("SDK退出失败！" +
                                "\ncode --> " + code +
                                "\nmsg --> " + msg);
                    }
                });
                break;

            case R.id.creatRoleBtn:
                JunSSdkApi.getInstance().sdkSubmitInfo(DemoActivity.this,
                        new JunSSubmitInfo.SubmitBuilder()
                                //角色ID，建议数字【必传】
                                .submitRoleId("100100")
                                //角色名称，【必传】
                                .submitRoleName("海绵宝宝爱睡觉")
                                //角色等级，无则不传，【选传】
                                .submitRoleLevel(100)
                                //服务器ID，建议数字，【必传】
                                .submitServerId("111")
                                //服务器名称，【必传】
                                .submitServerName("开天辟地")
                                //玩家余额，数字，无则不传，【选传】
                                .submitBalance(10)
                                //玩家VIP等级，数字，无则不传，【选传】
                                .submitVip(10)
                                //玩家帮派，无则不传，【选传】
                                .submitParty("土豆家")
                                //角色创建时间，单位：秒，获取服务器存储的时间，不可用手机本地时间，无则不传，【选传】
                                .submitTimeCreate(System.currentTimeMillis() / 1000)
                                //CP扩展字段，无则不传，【选传】
                                .submitExt("CP扩展字段")
                                .submitType(JunSConstants.SUBMIT_TYPE_CREATE)
                                .build(),
                        new JunSCallback() {
                            @Override
                            public void onSuccess(String info) {
                                showToast("角色创建上传成功！");
                            }

                            @Override
                            public void onFail(int code, String msg) {
                                showToast("角色创建上传失败！" +
                                        "\ncode --> " + code +
                                        "\nmsg --> " + msg);
                            }
                        }
                );
                break;

            case R.id.submitDataBtn:
                JunSSdkApi.getInstance().sdkSubmitInfo(DemoActivity.this, new JunSSubmitInfo.SubmitBuilder()
                                //角色ID，建议数字，【必传】
                                .submitRoleId("100100")
                                //角色名称，【必传】
                                .submitRoleName("海绵宝宝爱睡觉")
                                //角色等级，无则不传，【选传】
                                .submitRoleLevel(100)
                                //服务器ID，建议数字，【必传】
                                .submitServerId("111")
                                //服务器名称，【必传】
                                .submitServerName("开天辟地")
                                //玩家余额，数字，无则不传，【选传】
                                .submitBalance(10)
                                //玩家VIP等级，数字，无则不传，【选传】
                                .submitVip(10)
                                //玩家帮派，无则不传，【选传】
                                .submitParty("土豆家")
                                //角色创建时间，单位：秒，获取服务器存储的时间，不可用手机本地时间，无则不传，【选传】
                                .submitTimeCreate(System.currentTimeMillis() / 1000)
                                //CP扩展字段，无则不传，【选传】
                                .submitExt("CP扩展字段")
                                .submitType(JunSConstants.SUBMIT_TYPE_ENTER)
                                .build(),
                        new JunSCallback() {
                            @Override
                            public void onSuccess(String info) {
                                showToast("角色进入游戏上传成功！");
                            }

                            @Override
                            public void onFail(int code, String msg) {
                                showToast("角色进入游戏上传失败！" +
                                        "\ncode --> " + code +
                                        "\nmsg --> " + msg);
                            }
                        }
                );
                break;

            case R.id.upgradeDataBtn:
                JunSSdkApi.getInstance().sdkSubmitInfo(DemoActivity.this, new JunSSubmitInfo.SubmitBuilder()
                                //角色ID，建议数字，【必传】
                                .submitRoleId("100100")
                                //角色名称，【必传】
                                .submitRoleName("海绵宝宝爱睡觉")
                                //角色等级，无则不传，【选传】
                                .submitRoleLevel(100)
                                //服务器ID，建议数字，【必传】
                                .submitServerId("111")
                                //服务器名称，【必传】
                                .submitServerName("开天辟地")
                                //玩家余额，数字，无则不传，【选传】
                                .submitBalance(10)
                                //玩家VIP等级，数字，无则不传，【选传】
                                .submitVip(10)
                                //玩家帮派，无则不传，【选传】
                                .submitParty("土豆家")
                                //角色创建时间，单位：秒，获取服务器存储的时间，不可用手机本地时间，无则不传，【选传】
                                .submitTimeCreate(System.currentTimeMillis() / 1000)
                                //角色升级时间，单位：秒，获取服务器存储的时间，不可用手机本地时间，无则不传，【选传】
                                .submitTimeUp(System.currentTimeMillis() / 1000)
                                //CP扩展字段，无则不传，【选传】
                                .submitExt("CP扩展字段")
                                .submitType(JunSConstants.SUBMIT_TYPE_UPGRADE)
                                .build(),
                        new JunSCallback() {
                            @Override
                            public void onSuccess(String info) {
                                showToast("角色升级上传成功！");
                            }

                            @Override
                            public void onFail(int code, String msg) {
                                showToast("角色升级上传失败！" +
                                        "\ncode --> " + code +
                                        "\nmsg --> " + msg);
                            }
                        }
                );
                break;

            case R.id.updateDataBtn:
                JunSSdkApi.getInstance().sdkSubmitInfo(DemoActivity.this, new JunSSubmitInfo.SubmitBuilder()
                                //角色ID，建议数字，【必传】
                                .submitRoleId("100100")
                                //角色名称，【必传】
                                .submitRoleName("海绵宝宝爱睡觉")
                                //角色等级，无则不传，【选传】
                                .submitRoleLevel(100)
                                //服务器ID，建议数字，【必传】
                                .submitServerId("111")
                                //服务器名称，【必传】
                                .submitServerName("开天辟地")
                                //玩家余额，数字，无则不传，【选传】
                                .submitBalance(10)
                                //玩家VIP等级，数字，无则不传，【选传】
                                .submitVip(10)
                                //玩家帮派，无则不传，【选传】
                                .submitParty("土豆家")
                                //角色创建时间，单位：秒，获取服务器存储的时间，不可用手机本地时间，无则不传，【选传】
                                .submitTimeCreate(System.currentTimeMillis() / 1000)
                                //旧角色名称，【必传】
                                .submitLastRoleName("海绵宝宝爱大派星")
                                //CP扩展字段，无则不传，【选传】
                                .submitExt("CP扩展字段")
                                .submitType(JunSConstants.SUBMIT_TYPE_UPDATE)
                                .build(),
                        new JunSCallback() {
                            @Override
                            public void onSuccess(String info) {
                                showToast("角色改名上传成功！");
                            }

                            @Override
                            public void onFail(int code, String msg) {
                                showToast("角色改名上传失败！" +
                                        "\ncode --> " + code +
                                        "\nmsg --> " + msg);
                            }
                        }
                );
                break;

            case R.id.getConfigBtn:
                String tinyConfig = JunSSdkApi.getInstance().sdkGetConfig(DemoActivity.this);
                try {
                    JSONObject configJson = new JSONObject(tinyConfig);
                    String game_id = configJson.getString("game_id");
                    String pt_id = configJson.getString("pid");
                    String refer = configJson.getString("refer");
                    String sdk_version = configJson.getString("sdk_version");
                    showToast("SDK参数为 : " +
                            "\ngame_id --> " + game_id +
                            "\npid --> " + pt_id +
                            "\nrefer --> " + refer +
                            "\nsdk_version --> " + refer);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("SDK参数获取失败");
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            JunSSdkApi.getInstance().sdkGameExit(DemoActivity.this, new JunSCallback() {
                @Override
                public void onSuccess(String info) {
                    //退出游戏成功，游戏在此进行退出游戏，销毁游戏资源相关操作。
                    showToast("SDK退出成功！");
                    DemoActivity.this.finish();
                    System.exit(1);
                }

                @Override
                public void onFail(int code, String msg) {
                    //游戏无需处理，继续游戏。
                    showToast("SDK退出失败！" +
                            "\ncode --> " + code +
                            "\nmsg --> " + msg);
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

    private void showToast(String msg) {
        retTv.setText(msg);
    }

}
