package com.juns.sdk.core.own;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.own.account.JunSAccount;
import com.juns.sdk.core.own.common.ExitDialog;
import com.juns.sdk.core.own.event.JSExitEv;
import com.juns.sdk.core.own.event.JSInitEv;
import com.juns.sdk.core.own.event.JSLoginEv;
import com.juns.sdk.core.own.event.JSLogoutEv;
import com.juns.sdk.core.own.event.JSPayEv;
import com.juns.sdk.core.own.fw.FWManager;
import com.juns.sdk.core.own.fw.JSBallEv;
import com.juns.sdk.core.own.pay.JunSPay;
import com.juns.sdk.core.own.submit.JunSSubmit;
import com.juns.sdk.core.sdk.IPlatformSDK;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.HeartBeat;
import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.core.sdk.event.EvInit;
import com.juns.sdk.core.sdk.event.EvLogin;
import com.juns.sdk.core.sdk.event.EvLogout;
import com.juns.sdk.core.sdk.event.EvPay;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;

import java.util.HashMap;

/**
 * Data：19/12/2018-3:08 PM
 * Author: ranger
 */
public class JunSPlatformSDK implements IPlatformSDK {
    private static final String TAG = "own";
    public static TNLog logger = LogFactory.getLog(TAG, true);
    private Activity initActivity;
    private JunSAccount junSAccount;
    private JunSPay junSPay;
    private JunSSubmit junSSubmit;
    private FWManager fwManager;

    public JunSPlatformSDK() {
    }

    @Override
    public void preInit(Activity mainAct) {

    }

    @Override
    public void init(Activity mainAct) {
        logger.print("init called.");
        this.initActivity = mainAct;
        fwManager = new FWManager(mainAct);
        Bus.getDefault().register(fwManager);
        junSAccount = new JunSAccount();
        junSPay = new JunSPay();

        Bus.getDefault().post(JSInitEv.getSucc());
    }

    @Override
    public void preLogin(Activity mainAct) {

    }

    @Override
    public void login(Activity mainAct) {
        logger.print("login called.");

        if (!SDKCore.isSdkInitialized()) {
            Bus.getDefault().post(JSLoginEv.getFail(JunSConstants.Status.SDK_ERR, "sdk not initial."));
            return;
        }

        Bus.getDefault().post(JSBallEv.getHide());
        junSAccount.doLogin(mainAct);
    }

    @Override
    public void logout(Activity mainAct) {
        logger.print("logout called.");
        junSAccount.doLogout(mainAct);
    }

    @Override
    public void diyEvent(Activity mainAct, String info) {
        logger.print("diyEvent called.");
    }

    @Override
    public void showFloat(Activity mainAct) {
        //显示悬浮窗
        Bus.getDefault().post(JSBallEv.getShow());
    }

    @Override
    public void hideFloat(Activity mainAct) {
        //隐藏悬浮窗
        Bus.getDefault().post(JSBallEv.getHide());
    }

    @Override
    public String prePay(Activity mainAct) {
        return null;
    }


    @Override
    public void pay(Activity mainAct, HashMap<String, String> payParams) {
        logger.print("pay called." +
                "\npayParams --> " + payParams.toString());

        if (!SDKCore.isSdkInitialized()) {
            Bus.getDefault().post(JSPayEv.getFail(JunSConstants.Status.SDK_ERR, "sdk not initial."));
            return;
        }

        if (!SDKCore.isSdkLogined()) {
            Bus.getDefault().post(JSPayEv.getFail(JunSConstants.Status.SDK_ERR, "sdk not login."));
            return;
        }
        Bus.getDefault().post(JSBallEv.getHide());
        junSPay.doPay(mainAct, payParams);
    }

    @Override
    public void exitGame(final Activity mainAct) {
        logger.print("exitGame called.");
        Bus.getDefault().post(JSBallEv.getHide());
        ExitDialog.showExit(mainAct, new ExitDialog.ExitCallback() {
            @Override
            public void toContinue() {
                Bus.getDefault().post(JSExitEv.getFail(JunSConstants.Status.SDK_ERR, "user cancel."));
            }

            @Override
            public void toExit() {
                Bus.getDefault().post(JSExitEv.getSucc());
            }
        });
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        logger.print("submitInfo called." +
                "\nsubmitInfo --> " + submitInfo.toString());
        //junSSubmit.doSubmit(submitInfo);
//        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
//           // HeartBeat.enterGame = true;
//            if(SDKData.getSdkPeriod()!=9999){
//                if(SDKData.getSdkPeriod()<300){
//                    SDKData.setSdkPeriod(300);
//                }
//                logger.print("heartbeat called.");
//                HeartBeat.getInstance().startHeartBeat(SDKData.getSdkPeriod()*1000);
//            }
//
//        }
    }

    @Override
    public void onCreate(Activity mainAct) {
        logger.print("onCreate called.");
    }

    @Override
    public void onStart(Activity mainAct) {
        logger.print("onStart called.");
    }

    @Override
    public void onRestart(Activity mainAct) {
        logger.print("onRestart called.");
    }

    @Override
    public void onResume(Activity mainAct) {
        logger.print("onResume called.");
        if (fwManager != null) {
            fwManager.handleOnResume();
        }
    }

    @Override
    public void onPause(Activity mainAct) {
        logger.print("onPause called.");
        if (fwManager != null) {
            fwManager.handleOnPause();
        }
    }

    @Override
    public void onStop(Activity mainAct) {
        logger.print("onStop called.");
        if (mainAct != null && mainAct.isFinishing()) {
            destroy();
        }
    }

    @Override
    public void onDestroy(Activity mainAct) {
        logger.print("onDestroy called.");
        destroy();
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        logger.print("onActivityResult called.");
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        logger.print("onNewIntent called.");
    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        logger.print("onConfigurationChanged called.");
//        if (fwManager != null) {
//            fwManager.handleOnConfigurationChange(newConfig);
//        }
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        logger.print("onRequestPermissionsResult called.");
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onTInitEv(JSInitEv tInit) {
        logger.print("JSInitEv --> " + tInit.toString());
        Bus.getDefault().post(new EvInit(tInit));
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onTLogin(JSLoginEv tLogin) {
        logger.print("onTLogin --> " + tLogin.toString());
        Bus.getDefault().post(new EvLogin(tLogin));
        SDKData.setFwSwitchStatus(true);
        Bus.getDefault().post(JSBallEv.getShow());
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onTPayEv(JSPayEv payManager) {
        logger.print("onTPayEv --> " + payManager.toString());
        Bus.getDefault().post(JSBallEv.getShow());
        Bus.getDefault().post(new EvPay(payManager));
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onTExitEv(JSExitEv tExit) {
        logger.print("onTExitEv --> " + tExit.toString());
        if (tExit.getRet() != EvExit.SUCCESS) {
            Bus.getDefault().post(JSBallEv.getShow());
        }
        Bus.getDefault().post(new EvExit(tExit));
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onTLogoutEv(JSLogoutEv tLogout) {
        logger.print("onTLogoutEv");
        Bus.getDefault().post(JSBallEv.getHide());
        Bus.getDefault().post(new EvLogout());
    }

    private void destroy() {
        if (fwManager != null) {
            fwManager.destroy();
            fwManager = null;
        }
        if (junSPay != null) {
            junSPay.destroy();
            junSPay = null;
        }
        if (junSAccount != null) {
            junSAccount.destroy();
            junSAccount = null;
        }
    }
}
