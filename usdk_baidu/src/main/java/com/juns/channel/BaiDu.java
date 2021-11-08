package com.juns.channel;

import android.app.Activity;
import android.widget.Toast;

import com.baidu.gamesdk.BDGameSDK;
import com.baidu.gamesdk.BDGameSDKSetting;
import com.baidu.gamesdk.IResponse;
import com.baidu.gamesdk.OnGameExitListener;
import com.baidu.gamesdk.ResultCode;
import com.baidu.platformsdk.PayOrderInfo;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BaiDu extends OPlatformSDK {
    private static final String TAG = "BaiDu";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public BaiDu(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(final Activity activity) {
        BDGameSDKSetting.Orientation orientation=null;
        if(SDKApplication.getPlatformConfig().getExt3().equals("0")){
            orientation = BDGameSDKSetting.Orientation.LANDSCAPE;
        }else{
            orientation = BDGameSDKSetting.Orientation.PORTRAIT;
        }
        BDGameSDKSetting mBDGameSDKSetting = new BDGameSDKSetting();
        mBDGameSDKSetting.setAppID(Long.parseLong(SDKApplication.getPlatformConfig().getExt1()));             //APPID设置
        mBDGameSDKSetting.setAppKey(SDKApplication.getPlatformConfig().getExt2());//APPKEY设置
        mBDGameSDKSetting.setMode(BDGameSDKSetting.SDKMode.ONLINE);  // ONLINE:必须有网才能登录  WEAK_LINE:无网也能登录
        mBDGameSDKSetting.setDomain(BDGameSDKSetting.Domain.RELEASE);    //设置为正式模式
        mBDGameSDKSetting.setOrientation(orientation);//设置为横屏
        BDGameSDK.init(activity, mBDGameSDKSetting, new IResponse<Void>() {
            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                switch(resultCode){
                    case ResultCode.INIT_SUCCESS:
                        // 初始化成功
                        Bus.getDefault().post(OInitEv.getSucc());
//                        Toast.makeText(WelcomeActivity.this, "启动成功",  Toast.LENGTH_LONG).show();
//                        finish();
                        break;
                    case ResultCode.INIT_FAIL:
                        Bus.getDefault().post(OInitEv.getFail(resultCode,resultDesc));
                        break;
                }
            }
        });
        BDGameSDK.setSuspendWindowChangeAccountListener(activity, new IResponse<Void>() {
            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                switch(resultCode){
                    case ResultCode.LOGIN_SUCCESS:
                        Bus.getDefault().post(new OLogoutEv());
                       // BDGameSDK.showFloatView(activity);
                        uid=BDGameSDK.getLoginUid();//获取账号uid
                        String token=BDGameSDK.getLoginAccessToken();   //获取AccessToken
                        String userName = BDGameSDK.getLoginUser(activity).getBaiduAccountName();
                     //   BDGameSDK.queryLoginUserAuthenticateState(activity, null);
                        login2RSService(uid,userName,token);
                        break;
                    case ResultCode.LOGIN_FAIL:
                        Bus.getDefault().post(new OLogoutEv());
                        break;
                    case ResultCode.LOGIN_CANCEL:

                        break;
                }
            }
        });
        BDGameSDK.setSessionInvalidListener(new IResponse<Void>() {
            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                if(resultCode == ResultCode.SESSION_INVALID){
                    Bus.getDefault().post(new OLogoutEv());
                    Toast.makeText(activity, "会话失效，请重新登录", Toast.LENGTH_LONG).show();
                    //TODO 此处CP可调用登录接口
                }
            }
        });
        // 设置防沉迷系统回调，如果用户在线时长累计超过规定值，会触发该回调
        BDGameSDK.setAntiAddictionListener(new IResponse<Long>() {
            @Override
            public void onResponse(int resultCode, String resultDesc, final Long extraData) {
                // 默认直接弹出游玩超时弹窗退出游戏，厂商也可以根据自己需要进行自己的防沉迷设计
                BDGameSDK.forceCloseDialog(activity);
            }
        });
        BDGameSDK.queryGameUpdateInfo(activity);
      //  Bus.getDefault().post(OInitEv.getSucc());
    }
    private boolean state = false;
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(!state){
            JSONObject data = new JSONObject();
            try {
                data.put("nick", submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
                data.put("server", submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
                data.put("level", Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL)));
                BDGameSDK.reportUserData(data.toString());
                state = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    String uid = "";
    @Override
    public void login(final Activity activity) {
        BDGameSDK.login(new IResponse<Void>() {
            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                switch(resultCode){
                    case ResultCode.LOGIN_SUCCESS:
                        // 初始化成功
                       // Bus.getDefault().post(OInitEv.getSucc());
                        BDGameSDK.showFloatView(activity);
                        uid=BDGameSDK.getLoginUid();//获取账号uid
                        String userName = BDGameSDK.getLoginUser(activity).getBaiduAccountName();
                        String token=BDGameSDK.getLoginAccessToken();   //获取AccessToken
                        // 根据国家规定登录成功后需要立刻调用实名查询，回调可以为null
                        BDGameSDK.queryLoginUserAuthenticateState(activity, null);
                        login2RSService(uid,userName,token);
                        break;
                    case ResultCode.LOGIN_CANCEL:
                        Bus.getDefault().post(OLoginEv.getFail(resultCode,resultDesc));
                        break;
                    case ResultCode.LOGIN_FAIL:
                        Bus.getDefault().post(OLoginEv.getFail(resultCode,resultDesc));
                        break;
                }
            }
        });
    }
    private void login2RSService(String uid,String userName,String token ) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid",uid);
            dataJson.put("puname",userName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public void pay(final Activity activity, HashMap<String, String> payParams) {
        try {
            JSONObject dataJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            PayOrderInfo payOrderInfo = new PayOrderInfo();
            payOrderInfo.setCooperatorOrderSerial(payParams.get(JunSConstants.PAY_M_ORDER_ID)); //CP订单号
            payOrderInfo.setProductName(dataJson.getString("gamebiname")); //商品名称
            payOrderInfo.setTotalPriceCent((long) Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100);//以分为单位，long类型
            payOrderInfo.setRatio(Integer.parseInt(dataJson.getString("exchangeratio"))); //兑换比例，此时不生效
            payOrderInfo.setExtInfo("");//该字段在支付通知中原样返回,不超过500个字符
            payOrderInfo.setCpUid(uid);//登录成功后获取的uid
            BDGameSDK.pay(activity, payOrderInfo, null, new IResponse<PayOrderInfo>() {
                @Override
                public void onResponse(int resultCode, String resultDesc, PayOrderInfo payOrderInfo) {
                    switch(resultCode){
                        case ResultCode.PAY_SUCCESS:
                            Bus.getDefault().post(OPayEv.getSucc(resultDesc));
                            break;
                        case ResultCode.PAY_CANCEL:
                            Bus.getDefault().post(OPayEv.getFail(resultCode,resultDesc));
                            break;
                        case ResultCode.PAY_FAIL:
                            Bus.getDefault().post(OPayEv.getFail(resultCode,resultDesc));
                            break;
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exitGame(Activity activity) {
        BDGameSDK.gameExit(activity, new OnGameExitListener() {
            @Override
            public void onGameExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        BDGameSDK.onResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        BDGameSDK.onPause(mainAct);
    }

}
