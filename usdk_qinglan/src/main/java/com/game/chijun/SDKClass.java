package com.game.chijun;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSPayInfo;
import com.juns.sdk.core.api.JunSSdkApi;
import com.juns.sdk.core.api.JunSSubmitInfo;
import com.juns.sdk.core.api.callback.JunSCallback;
import com.juns.sdk.core.api.callback.JunSLogoutCallback;
import com.juns.sdk.core.api.callback.JunSPayCallback;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.game.UMGameAgent;
import com.umeng.commonsdk.UMConfigure;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SDKClass {

    static String M_SPLIT_STR = "!!$$!!";

    public static int m_channelId = 188;
    public static boolean m_isInit = false;
    public static final String TAG = "GameActivity";
    //sdk配置参数
    public static String m_game_id = "";
    public static String m_pt_id = "";
    public static String m_refer = "";
    public static String m_sdk_version = "";
    public static String m_sdkDeviceId = "";

    public static  String m_oldUserId = "";

    public static Activity mUnityActivity;
    public  static void onCreate(Activity player){
        mUnityActivity = player;

        //初始化友盟sdk
        //UMENG_CHANNEL
        UMConfigure.init(mUnityActivity,"5d5389053fc195a763000247","S602", UMConfigure.DEVICE_TYPE_PHONE,null);
        UMGameAgent.init(mUnityActivity);
        // 打开统计SDK调试模式
        //UMConfigure.setLogEnabled(true);

        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

        InitSdk();

        JunSSdkApi.getInstance().sdkOnCreate(UnityPlayer.currentActivity);
    }

    public static String m_verifyRealName = "";

    static Toast toast;
    private static void showToast(String msg)
    {
        if (true)
        {
            return;
        }

        if (toast == null) {
            toast = Toast.makeText(UnityPlayer.currentActivity, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setText(msg);
        }

        toast.show();
    }

    private static void Log(String msg)
    {
        if (false)
        {
            return;
        }

        Log.w(TAG, msg);
    }

    public  static void onCreateBefore(Bundle bundle){
        //sdk在界面之前做
        //mUnityActivity 无效
        //部分sdk使用

    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //生命周期===begin
    /////////////////////////////////////////////////////////////////////////////////////////
    public static void onDestroy ()
    {
        JunSSdkApi.getInstance().sdkOnDestroy(UnityPlayer.currentActivity);
    }
    public static void onPause ()
    {
        JunSSdkApi.getInstance().sdkOnPause(UnityPlayer.currentActivity);
    }
    public static void onResume ()
    {
        JunSSdkApi.getInstance().sdkOnResume(UnityPlayer.currentActivity);
    }
    public static void onStart ()
    {
        JunSSdkApi.getInstance().sdkOnStart(UnityPlayer.currentActivity);
    }
    public static void onRestart ()
    {
        JunSSdkApi.getInstance().sdkOnRestart(UnityPlayer.currentActivity);
    }
    public static void onBack ()
    {
        OnGameExit();
    }
    public static void onStop ()
    {
        JunSSdkApi.getInstance().sdkOnStop(UnityPlayer.currentActivity);
    }
    public static void onLowMemory ()
    {

    }
    public static void onTrimMemory (int level)
    {

    }
    public static void onConfigurationChanged (Configuration configuration)
    {
        JunSSdkApi.getInstance().sdkOnConfigurationChanged(UnityPlayer.currentActivity, configuration);
    }
    public static void onWindowFocusChanged (boolean hasFocus)
    {

    }
    public static void dispatchKeyEvent (KeyEvent event)
    {

    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        JunSSdkApi.getInstance().sdkOnActivityResult(UnityPlayer.currentActivity, requestCode, resultCode, data);
    }

    public static void onNewIntent(Intent intent)
    {
        JunSSdkApi.getInstance().sdkOnNewIntent(UnityPlayer.currentActivity, intent);
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        JunSSdkApi.getInstance().sdkOnRequestPermissionsResult(UnityPlayer.currentActivity, requestCode, permissions, grantResults);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //生命周期===end
    /////////////////////////////////////////////////////////////////////////////////////////
	//刷新相册
	public static void scanFile(String filePath){
		Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		scanIntent.setData(Uri.fromFile(new File(filePath)));
		mUnityActivity.sendBroadcast(scanIntent);
	}

    //获取上次保存的内容
	public  static  String  GetKeychain(String key){
        return Keychain.GetFileValue(mUnityActivity, key);
    }

    //存储字典类型文件
    public  static void SetKeychain(String key, String value){
        Keychain.SaveFileValue(mUnityActivity, key, value);
    }

    //获取剩余存储空间大小
    public static long GetExternalStorage(){
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } catch (Throwable e) {
            Log.e("GetExternalStorage", "获取空间大小出错 将返回1024字节");
        }
        return 1024;
    }
	
    //unity 调用java函数
    public static String  OnCallCSharp(String method, String args) throws JSONException {
        Log.w("java=====>OnCallCSharp", method);
        Log.w("OnCallCSharpargs", args);
        showToast(method);

        String retstr = "false";

        if (args.equals("") || args.isEmpty())
        {
            args = "{}";
        }

        JSONObject json = null;
        try {
             json = new JSONObject(args);
        }
        catch (JSONException e)
        {
            e.printStackTrace();

            json = new  JSONObject();
        }

        if(method.equals("login"))
        {
            if(m_isInit)
            {
                if(!IsLogin())
                {
                    //打开sdk界面
                    sdkLoginShow();
                }
                else
                {
                    //退出登錄
                }
            }
            else
            {
                InitSdk();
            }
        }
        else if(method.equals("loginsuccess"))
        {
            MobclickAgent.onProfileSignIn(json.getString("nomalkey"));
        }
        else if(method.equals("sdkpay"))
        {
            SdkPay(json);
        }
        else if (method.equals("sdkloginout"))
        {
            MobclickAgent.onProfileSignOff();
            OnGameLoginOut();
        }
        else if (method.equals("loginother"))
        {
            MobclickAgent.onProfileSignIn(json.getString("channel"),json.getString("userid"));
        }
        else if (method.equals("eventcount"))
        {
            MobclickAgent.onEvent(UnityPlayer.currentActivity,json.getString("nomalkey"));
        }
        else if (method.equals("eventthings"))
        {
            if (json.getString("nomalkey").equals( "maintask")
                    || json.getString("nomalkey").equals( "shoptag")
                    || json.getString("nomalkey").equals( "shopitem")
                    )
            {
                HashMap<String,String> map = new HashMap<String,String>();
                map.put("type",json.getString("nomalkey"));
                MobclickAgent.onEventValue(UnityPlayer.currentActivity,json.getString("nomalkey"), map,json.getInt("id"));
            }
        }
        else if (method.equals("startguanqia"))
        {
            showToast(json.getString("nomalkey"));
            UMGameAgent.startLevel(json.getString("nomalkey"));
        }
        else if (method.equals("finishguanqia"))
        {
            showToast(json.getString("nomalkey"));
            UMGameAgent.finishLevel(json.getString("nomalkey"));
        }
        else if (method.equals("failguanqia"))
        {
            showToast(json.getString("nomalkey"));
            UMGameAgent.failLevel(json.getString("nomalkey"));
        }
        else if (method.equals("guanqiaclick"))
        {
            UMGameAgent.failLevel(json.getString("nomalkey"));
        }
        else if(method.equals("imei"))
        {
            retstr = UniversalID.getUniversalID(UnityPlayer.currentActivity);
        }
        else if(method.equals("sdksendplayerinfo"))
        {
            SendPayerInfo(json);
        }
        else if(method.equals("sdkgetchannelid"))
        {
            retstr = String.valueOf(m_channelId);
        }
        else if(method.equals("sdkgetpackagename"))
        {
            retstr = PackageName();
        }
        else if(method.equals("sdkgetchildchannelid"))
        {
            retstr = GetChildChannelId();
        }
        else if(method.equals("sdkpaysuccess"))
        {
            SdkPaySuccess(json);
        }
        else if(method.equals("sdkrengzheng"))
        {
            retstr = m_verifyRealName;
        }
        else if(method.equals("sdkswitchlogin"))
        {
            SwichLogin();
        }
        else  if(method.equals("sdkexipgame"))
        {
            OnGameExit();
        }
        else if(method.equals("sdkskipwxgame"))
        {
            openWechatApi(json.getString("nomalkey"));
        }
        else if (method.equals("event_report")){
            ReportEvent(json.getString("event"), args);
        }
        return retstr;
    }

    private static  void ReportEvent(String eventstr, String jsonstr){

        Log.w("java=====>ReportEvent", eventstr);
        JunSSdkApi.getInstance().sdkEventPost(eventstr, jsonstr, new JunSCallback() {
            @Override
            public void onSuccess(String info) {
//               showToast("埋点上报成功:" + eventstr);
            }

            @Override
            public void onFail(int code, String msg) {
//                showToast("埋点上报失败"+ eventstr);
            }
        });
    }

    //调用c#
    public  static void CallCSharp(String method, String args){

        if(method == null){
            method = "";
        }
        if(args == null){
            args = "";
        }
        String retstr = method + M_SPLIT_STR + args;
        Log("java=====>CallCSharp:::" + retstr);
        UnityPlayer.UnitySendMessage("PlatformObject", "OnPlatFormCall", retstr);
    }

    public static String PackageName( )
    {
        return UnityPlayer.currentActivity.getBaseContext().getPackageName();
    }

    static void SendLogin(String strType, JSONObject jsonData)
    {
        JSONObject jsonLogin = new JSONObject();
        try {
            jsonLogin.put("op", "createUser");
            jsonLogin.put("osid", 1);
            jsonLogin.put("imei",UniversalID.getUniversalID(UnityPlayer.currentActivity));
            jsonLogin.put("dataJson", jsonData);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log("sdk login success: " + jsonLogin.toString());
        CallCSharp(strType,jsonLogin.toString());
    }

    static void InitSdk()
    {
        Log(" GameSdk.sdkInit");

        InitSdkInfo();
    }

    static boolean IsLogin()
    {
        //return JunSSdkApi.getInstance().isLogined();
        return false;
    }

    static void SendPayerInfo(final JSONObject json)
    {
        System.out.println("====================");
        Log( " GameSdk.SendPayerInfo");

//打开sdk界面
        UnityPlayer.currentActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    Log("SdkPay");

                    String playerInfoType = JunSConstants.SUBMIT_TYPE_UPGRADE;
                    if(json.getInt("infoType") == 1)
                    {
                        //创角
                        playerInfoType = JunSConstants.SUBMIT_TYPE_CREATE;
                    }
                    else  if(json.getInt("infoType") == 2)
                    {
                        playerInfoType = JunSConstants.SUBMIT_TYPE_ENTER;
                    }

                    Log( "playerInfoType: " + playerInfoType );

                    JunSSdkApi.getInstance().sdkSubmitInfo(UnityPlayer.currentActivity,
                            new JunSSubmitInfo.SubmitBuilder()
                                    //角色ID，建议数字【必传】
                                    .submitRoleId(json.getString("roleId"))
                                    //角色名称，【必传】
                                    .submitRoleName(json.getString("roleName"))
                                    //角色等级，无则不传，【选传】
                                    .submitRoleLevel(json.getInt("roleLevel"))
                                    //服务器ID，建议数字，【必传】
                                    .submitServerId(json.getString("serverId"))
                                    //服务器名称，【必传】
                                    .submitServerName(json.getString("serverName"))
                                    //玩家余额，数字，无则不传，【选传】
                                    .submitBalance(json.getInt("balance"))
                                    //玩家VIP等级，数字，无则不传，【选传】
                                    .submitVip(json.getInt("vipLevel"))
                                    //玩家帮派，无则不传，【选传】
                                    .submitParty(json.getString("guildName"))
                                    //角色创建时间，单位：秒，获取服务器存储的时间，不可用手机本地时间，无则不传，【选传】
                                    .submitTimeCreate(json.getInt("roleCreateTime"))
                                    //CP扩展字段，无则不传，【选传】
                                    .submitExt("1111111")
                                    .submitType(playerInfoType)
                                    .build(),
                            new JunSCallback() {
                                @Override
                                public void onSuccess(String info)
                                {
                                    Log("角色创建上传成功！");
                                    Log( "GameSdk playerinfo: onSuccess ");
                                }

                                @Override
                                public void onFail(int code, String msg)
                                {
                                    Log("角色创建上传失败！" +
                                            "\ncode --> " + code +
                                            "\nmsg --> " + msg);

                                    Log( "GameSdk playerinfo: onFail ");
                                }
                            }
                    );
                }
                catch (Exception e){
                    Log(  "GameSdk.SendPayerInfo err");
                }finally {
                    Log( "GameSdk.SendPayerInfo end ");
                }
            }
        });
    }

    static void SdkPay(final JSONObject json)
    {
        Log( " GameSdk.SdkPay");

        try {
            //打开sdk界面
            UnityPlayer.currentActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Log( "SdkPay");

                        int cast = Integer.parseInt(json.getString("item_price")) / 100;
                        if(cast < 1)
                        {
                            cast = 1;
                        }

                        JunSSdkApi.getInstance().sdkPay(UnityPlayer.currentActivity, new JunSPayInfo.PayBuilder()
                                //CP订单号，全局唯一，不可重复，【必传】
                                .payOrderId(json.getString("order"))
                                //充值金额，单位：元，【必传】
                                .payMoney(cast)
                                //商品名称，【必传】
                                .payOrderName(json.getString("item_name"))
                                //CP扩展字段，长度255字符，无则不传，【选传】
                                .payExt("111")
                                //角色ID，建议数字，【必传】
                                .payRoleId(json.getString("role_id"))
                                //角色名称，【必传】
                                .payRoleName(json.getString("role_name"))
                                //角色等级，无则不传，【选传】
                                .payRoleLevel(json.getInt("role_level"))
                                //服务器ID，建议数字，【必传】
                                .payServerId(json.getString("server_id"))
                                //服务器名称，【必传】
                                .payServerName(json.getString("server_name"))
                                //角色VIP等级，【选传】
                                .payRoleVip(json.getInt("viplevel"))
                                //角色钱包余额，【选传】
                                .payRoleBalance(json.getInt("balance"))
                                .payRate(10)
                                .build(), new JunSPayCallback() {
                            @Override
                            public void onFinish(int code, String msg) {
                                //SDK支付完成，游戏按需进行处理，发货需以服务端回调为准
                                Log("SDK支付完成！" +
                                        "\ncode --> " + code +
                                        "\nmsg --> " + msg);

                                //关闭菊花
                                CallCSharp("payfinish","");
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        Log( "GameSdk.SdkPay err");
                    }
                }
            });
        }catch (Exception e){
            Log( "GameSdk.SdkPay err");
        }finally {
            Log( "GameSdk.SdkPay end ");
        }
    }

    static void SdkPaySuccess(JSONObject json)
    {
        Log( " GameSdk.SdkPaySuccess start");

        try {
            Double price = Double.parseDouble(json.getString("item_price")) / 100;
            int count = Integer.parseInt(json.getString("item_count"));
            if(count < 1)
            {
                count = 1;
            }

            Log( "GameSdk.SdkPaySuccess price: " + price + " count: " + count + "type: " + json.getString("currencytype") + " order: " + json.getString("order"));

            //UMGameAgent.pay(price,count,1);
            Log("UM-charge-send-start");
            UMGameAgent.exchange(price,json.getString("currencytype"),count,1,json.getString("order"));
            Log("UM-charge-send-end");
        }catch (Exception e){
            Log( "GameSdk.SdkPaySuccess err");
        }finally {
            Log( "GameSdk.SdkPaySuccess end ");
        }
    }

    public static String GetChildChannelId()
    {
        return "0";
    }

    static void InitSdkInfo()
    {
        Log(" GameSdk.sdkInit");

        JunSSdkApi.getInstance().sdkInit(UnityPlayer.currentActivity, "20200606sanceQILINGShi", new JunSCallback()
        {
            @Override
            public void onSuccess(String intInfo) {
                //SDK初始化成功，收到此回调后，游戏可往下继续进⾏相关操作。
                //游戏可从回调info中获取sdkDeviceId
                try {
                    JSONObject initJson = new JSONObject(intInfo);
                    m_sdkDeviceId = initJson.getString("sdkDeviceId");
                    Log("SDK初始化成功：" + "\n sdkDeviceId --> " + m_sdkDeviceId);

                    m_isInit = true;

                    getSdkInfo();
                    initNotifiers();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int code, String msg) {
                //SDK初始化失败，游戏可重新调⽤SDK初始化接⼝。
                Log("SDK初始化失败，重新调⽤SDK初始化接⼝！" + "\ncode --> " + code + "\nmsg --> " + msg);
                // doSDKInit();

                m_isInit = false;
            }
        });
    }

    static void getSdkInfo()
    {
        String tinyConfig = JunSSdkApi.getInstance().sdkGetConfig(UnityPlayer.currentActivity);
        try {
            JSONObject configJson = new JSONObject(tinyConfig);
            m_game_id = configJson.getString("game_id");
            m_pt_id = configJson.getString("pid");
            m_refer = configJson.getString("refer");
            m_sdk_version = configJson.getString("sdk_version");

            initNotifiers();

            Log("SDK参数为 : " +
                    "\ngame_id --> " + m_game_id +
                    "\npid --> " + m_pt_id +
                    "\nrefer --> " + m_refer +
                    "\nsdk_version --> " + m_sdk_version);

        } catch (JSONException e) {
            e.printStackTrace();

            Log("error: SDK参数获取失败");
        }
    }

    static void initNotifiers()
    {
        Log("initNotifiers");

        //登出回调
        JunSSdkApi.getInstance().setLogoutCallback(new JunSLogoutCallback() {
            @Override
            public void onLogout() {
                //SDK注销成功
                //【注意】游戏收到回调后，先回调游戏登录界面，再调用SDK切换帐号方法
                showToast("SDK注销成功！");

                CallCSharp("sdkloginout","");
            }
        });
    }

    //登出游戲
    static  void OnGameLoginOut(){
        Log("OnGameLoginOut");

        UnityPlayer.currentActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JunSSdkApi.getInstance().sdkLogout(UnityPlayer.currentActivity);

                    CallCSharp("sdkloginout","");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    static void  sdkLoginShow()
    {
        //打开sdk界面
        UnityPlayer.currentActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log( "login");

                    JunSSdkApi.getInstance().sdkLogin(UnityPlayer.currentActivity, new JunSCallback() {
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
                                Log("SDK登录成功成功：" +
                                        "\ntoken --> " + token +
                                        "\nuserId --> " + userId +
                                        "\nuserName --> " + userName);

                                JSONObject jsonSidData = new JSONObject();
                                try {
                                    // 游戏作为生成角色ID 的唯�?ID
                                    jsonSidData.put("token",token);
                                    jsonSidData.put("userid", userId);
                                    jsonSidData.put("sdk", m_channelId);
                                    jsonSidData.put("channelId", m_channelId);
                                    jsonSidData.put("childChannelId", Integer.parseInt(m_pt_id) );
                                    jsonSidData.put("package", PackageName());
                                    jsonSidData.put("username",userName);
                                    jsonSidData.put("pwd", "96e79218965eb72c92a549dd5a330112");

                                    jsonSidData.put("pid", Integer.parseInt(m_pt_id) );
                                    jsonSidData.put("game_id",m_game_id);
                                    jsonSidData.put("refer", m_refer);
                                    jsonSidData.put("sdk_version", m_sdk_version);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                SendLogin("sdkloginsuccess", jsonSidData);

                                m_oldUserId = userId;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(int code, String msg) {
                            //SDK登录失败，message中为失败原因具体信息
                            //建议游戏收到此回调后，无需提示原因信息给玩家，重新调用SDK登录接口。
                            Log("SDK登录失败：" +
                                    "\ncode --> " + code +
                                    "\nmsg --> " + msg);
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    static void Exit(){
        if(mUnityActivity != null){
            mUnityActivity.finish();
            android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
            System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
        }
    }

    //退出游戏
    static  void OnGameExit(){
        Log("OnGameExit");

        UnityPlayer.currentActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JunSSdkApi.getInstance().sdkGameExit( UnityPlayer.currentActivity, new
                    JunSCallback() {
                        @Override
                        public void onSuccess(String info) {
                            //退出游戏成功，游戏在此进⾏退出游戏，销毁游戏资源相关操作。
                            Log("SDK退出成功！");
                            CallCSharp("sdkexipgame","");
                            Exit();
                        }
                        @Override
                        public void onFail(int code, String msg) {
                            //游戏⽆需处理，继续游戏。
                            Log("SDK退出失败！" +
                                    "\ncode --> " + code +
                                    "\nmsg --> " + msg);
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    static void SwichLogin()
    {
        Log("SwichLogin");

        /*
        UnityPlayer.currentActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //SDK注销成功
                    //【注意】游戏收到回调后，先回调游戏登录界面，再调用SDK切换帐号方法
                    Log("SDK注销成功！");
                    //再次调用SDK切换帐号方法，重新登录
                    JunSSdkApi.getInstance().sdkSwitchAccount(UnityPlayer.currentActivity, new JunSCallback() {
                        @Override
                        public void onSuccess(String userInfo) {
                            //SDK切换帐号成功，切换帐号成功userInfo里包含帐号的token以及帐号相关信息，详情参考文档。
                            //【注意】：
                            //1. 获取到token后，游戏用该token通过服务端验证接口获取真实的uid，具体参考服务端接入文档；
                            try {
                                JSONObject userJson = new JSONObject(userInfo);
                                String token = userJson.getString("token");
                                String userId = userJson.getString("userId");
                                String userName = userJson.getString("userName");
                                Log("SDK登录成功成功：" +
                                        "\ntoken --> " + token +
                                        "\nuserId --> " + userId +
                                        "\nuserName --> " + userName);

                                JSONObject jsonSidData = new JSONObject();
                                try {
                                    // 游戏作为生成角色ID 的唯�?ID
                                    jsonSidData.put("token",token);
                                    jsonSidData.put("userid", userId);
                                    jsonSidData.put("sdk", m_channelId);
                                    jsonSidData.put("channelId", m_channelId);
                                    jsonSidData.put("childChannelId", Integer.parseInt(m_pt_id) );
                                    jsonSidData.put("package", PackageName());
                                    jsonSidData.put("username",userName);
                                    jsonSidData.put("pwd", "96e79218965eb72c92a549dd5a330112");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                SendLogin("sdkrelogin", jsonSidData);
                            } catch (Exception e) {
                                Log("=====================================");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(int code, String msg) {
                            //SDK切换帐号失败，message中为失败原因具体信息
                            //建议游戏收到此回调后，无需提示原因信息给玩家，重新调用SDK切换帐号接口。
                            Log("SDK切换帐号失败：" +
                                    "\ncode --> " + code +
                                    "\nmsg --> " + msg);
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });*/
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///sdk函数
    /////////////////////////////////////////////////////////////////////////////////////////////////

    public static String TestBack(JSONObject args){

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                /**
                 *要执行的操作
                 */
                CallCSharp("back____", "back___args_");
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 3000);//3秒后执行TimeTask的run方法

        return "true";
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    static void openWechatApi(String prame)
    {
        Log( " GameSdk.openWechatApi start");

        try {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            mUnityActivity.startActivity(intent);

            Log("GameSdk.openWechatApi over");
        }catch (Exception e){
            CallCSharp("sdkskipwxgame", "");
            Log( "GameSdk.openWechatApi err");
        }finally {
            Log( "GameSdk.openWechatApi end ");
        }
    }
}
