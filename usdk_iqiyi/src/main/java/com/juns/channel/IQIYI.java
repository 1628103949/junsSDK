package com.juns.channel;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.iqiyi.sdk.listener.GameBindPhoneCallBack;
import com.iqiyi.sdk.listener.GamePlatformInitListener;
import com.iqiyi.sdk.listener.LoginListener;
import com.iqiyi.sdk.listener.PayListener;
import com.iqiyi.sdk.platform.GamePlatform;
import com.iqiyi.sdk.platform.GameSDKResultCode;
import com.iqiyi.sdk.platform.GameUser;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class IQIYI extends OPlatformSDK {
    private static final String TAG = "IQIYI";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    GamePlatform platform;
    public IQIYI(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(final Activity activity) {
        //绑定手机号的回调
        platform = GamePlatform.getInstance();
        platform.initPlatform(activity, SDKApplication.getPlatformConfig().getExt1(),new GamePlatformInitListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Bus.getDefault().post(OInitEv.getSucc());
                getLaunchFrom(activity);
                final GamePlatform platform = GamePlatform.getInstance();
                platform.addLoginListener(new LoginListener(){
                    @Override
                    public void exitGame(){
                        //游戏退出回调
                      //  finish();
                        Bus.getDefault().post(OExitEv.getSucc());
                    }
                    @Override
                    public void logout(){
                        //账号注销时回调
                        Bus.getDefault().post(new OLogoutEv());
                    }
                    @Override
                    public void changeAccountCancle() {
                        //切换账户取消回调
                    }
                    @Override
                    public void loginResult(int resultCode, GameUser user){
                        if(resultCode == GameSDKResultCode.SUCCESSLOGIN && user != null){
                            //登录成功后初始化侧边栏浮标
                            platform.initSliderBar(activity);
                            login2RSService(user.uid,user.timestamp,user.sign);
                        }
                    }

                    @Override
                    public void changeAccountSuccess(int i, GameUser gameUser) {
                       // Log.d("SDKPlatfrom", "changeAccountSuccess: "+gameUser.uid);
                        Bus.getDefault().post(new OLogoutEv());
                        login2RSService(gameUser.uid,gameUser.timestamp,gameUser.sign);
                    }
                });
              //  GamePlatform platform = GamePlatform.getInstance();
                platform.addPaymentListener(new PayListener() {
                    @Override
                    public void leavePlatform() {
                        Bus.getDefault().post(OPayEv.getFail(2,"user cancel"));
                    }

                    @Override
                    public void paymentResult(int result) {
                        if(result == GameSDKResultCode.SUCCESSPAYMENT){
                            Bus.getDefault().post(OPayEv.getSucc("success"));
                        }else{
                            Bus.getDefault().post(OPayEv.getFail(2,"fail"));
                        }
                    }
                });
            }
            @Override
            public void onFail(String arg0) {
                // TODO Auto-generated method stub
                Bus.getDefault().post(OInitEv.getFail(2,arg0));
            }
        });

    }
    private void getLaunchFrom(Activity activity) {
        if (GamePlatform.getInstance().isLaunchFromGameCenter(activity)) {
            Log.i(TAG, "app from gameCenter");
        } else {
            Log.i(TAG, "unknown from");
        }
    }
    HashMap<String, String> roleInfo = null;
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            platform.createRole(mainAct, "ppsmobile_s"+submitInfo.get(JunSConstants.SUBMIT_SERVER_ID), submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME), submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        }

        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            roleInfo = submitInfo;
            platform.enterGame(mainAct, "ppsmobile_s"+submitInfo.get(JunSConstants.SUBMIT_SERVER_ID), submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME), submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));

        }
    }



    @Override
    public void login(Activity activity) {
        platform.iqiyiUserLogin(activity);
    }

    private void login2RSService(String uid,int time,String token) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid",uid);
            dataJson.put("time",time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OPlatformUtils.loginToServer(token, dataJson.toString());
    }
    @Override
    public void logout(Activity mainAct) {
        platform.iqiyiChangeAccount(mainAct);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print(payParams.toString());
        int money = (int) Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        //logger.print("1111"+money);
        platform.iqiyiPayment(activity, money, roleInfo.get(JunSConstants.SUBMIT_ROLE_ID), "ppsmobile_s"+roleInfo.get(JunSConstants.SUBMIT_SERVER_ID), payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_ORDER_ID));
        //platform.iqiyiPayment(activity, 50, "roleId", "ppsmobile_s1", "userData", "cpOrderId");


    }


    @Override
    public void exitGame(Activity activity) {
        platform.iqiyiUserLogout(activity);
    }
}
