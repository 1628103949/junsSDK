package com.juns.sdk.core.platform;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.IPlatformSDK;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.core.sdk.event.EvInit;
import com.juns.sdk.core.sdk.event.EvLogin;
import com.juns.sdk.core.sdk.event.EvLogout;
import com.juns.sdk.core.sdk.event.EvPay;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;

import java.util.HashMap;

/**
 * Data：27/09/2018-11:23 AM
 * Author: ranger
 */
public class OPlatformSDK implements IPlatformSDK {

    public OPlatformBean pBean = null;

    public OPlatformSDK(OPlatformBean pBean) {
        this.pBean = pBean;
    }

    @Override
    public void preInit(Activity mainAct) {
    }

    @Override
    public void init(Activity mainAct) {

    }

    @Override
    public void preLogin(Activity mainAct) {

    }

    @Override
    public void login(Activity mainAct) {

    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public String prePay(Activity mainAct) {
        return null;
    }

    @Override
    public void pay(Activity mainAct, HashMap<String, String> payParams) {

    }

    @Override
    public void exitGame(Activity mainAct) {

    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {

    }

    @Override
    public void onCreate(Activity mainAct) {

    }

    @Override
    public void onStart(Activity mainAct) {

    }

    @Override
    public void onRestart(Activity mainAct) {

    }

    @Override
    public void onResume(Activity mainAct) {

    }

    @Override
    public void onPause(Activity mainAct) {

    }

    @Override
    public void onStop(Activity mainAct) {

    }

    @Override
    public void onDestroy(Activity mainAct) {
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {

    }

    @Override
    public void onConfigurationChanged(Activity mainAct, Configuration newConfig) {

    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {

    }

    //平台，联运商
    @BusReceiver(mode = Bus.EventMode.Main)
    public void onOInitEv(OInitEv oInit) {
        SDKCore.logger.print("onOInitEv --> " + oInit.toString());
        Bus.getDefault().post(new EvInit(oInit));
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onOLoginEv(OLoginEv oLogin) {
        SDKCore.logger.print("onOLoginEv --> " + oLogin.toString());
        Bus.getDefault().post(new EvLogin(oLogin));
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onOPayEv(OPayEv oPay) {
        SDKCore.logger.print("onPPayEv --> " + oPay.toString());
        Bus.getDefault().post(new EvPay(oPay));
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onOExitEv(OExitEv oExit) {
        SDKCore.logger.print("onOExitEv --> " + oExit.toString());
        Bus.getDefault().post(new EvExit(oExit));
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void onOLogoutEv(OLogoutEv oLogout) {
        SDKCore.logger.print("onOLogoutEv");
        Bus.getDefault().post(new EvLogout());
    }

}
