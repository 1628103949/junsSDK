package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.gm88.gmcore.GM;
import com.gm88.gmcore.GmListener;
import com.gm88.gmcore.GmStatus;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GuaM extends OPlatformSDK {
    private static final String TAG = "GuaM";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public GuaM(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        GM.setListener(new GmListener() {
            @Override
            public void onCallBack(Message mMessage) {
                // TODO Auto-generated method stub
                switch (mMessage.what) {
                    case GmStatus.INIT_SUCCESS:// 初始化sdk成功回调
                        String s = (String) mMessage.obj;
                        Bus.getDefault().post(OInitEv.getSucc());
                        //SDKLog.d(TAG,"初始化sdk成功回调  " +  s);
                        //toast(s);
                        break;
                    case GmStatus.INIT_FALIED:// 初始化sdk失败回调
                        String s1 = (String) mMessage.obj;
                        Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,s1));
//                        SDKLog.d(TAG,"初始化sdk失败回调  " +  s1);
//                        toast(s1);
                        break;
                    case GmStatus.LOGIN_SUCCESS:// 登录成功回调
                        String res = (String) mMessage.obj;
                        try {
                            JSONObject object = new JSONObject(res);
                            login2RSService(object.getString("token"),object.getString("uid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case GmStatus.LOGIN_FALIED:// 登录失败回调
                        Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
                        break;
                    case GmStatus.LOGIN_CANCEL:// 登录取消回调
                        Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
                        break;
                    case GmStatus.LOGOUT_SUCCESS:// 注销账号成功回调，收到回调后游戏需返回主界面
                        Bus.getDefault().post(new OLogoutEv());
                        break;
                    case GmStatus.LOGOUT_FALIED:// 注销账号失败回调

                        break;
                    case GmStatus.PAY_SUCCESS:// 支付成功回调
                        Bus.getDefault().post(OPayEv.getSucc("success"));
                        break;
                    case GmStatus.PAY_FALIED:// 支付失败回调
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
                        break;
                    case GmStatus.PAY_CANCEL:// 支付取消回调
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
                        break;
                    case GmStatus.GAME_EXIT:// 退出游戏回调
                        Bus.getDefault().post(OExitEv.getSucc());
                        break;
                    case GmStatus.REALNAME_CHECK:// 实名认证回调
                        int realNameType = (int) mMessage.obj;
                        switch (realNameType){
                            //0-7岁
                            case GmStatus.ANTI_CHILD:
                                //toast("0-7岁");
                                break;
                            //8-15岁
                            case GmStatus.ANTI_MINOR:
                                //toast("8-15岁");
                                break;
                            //16-17岁
                            case GmStatus.ANTI_MINOR2:
                                //toast("16-17岁");
                                break;
                            //成年
                            case GmStatus.ANTI_AUDLT:
                                //toast("成年");
                                break;
                            //查询失败或者是未实名
                            case GmStatus.ANTI_UNREGISTER:
                                //toast("查询失败或者是未实名");
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        GM.init(mainAct);
    }

    @Override
    public void init(Activity activity) {
    }

    @Override
    public void login(Activity activity) {
        GM.login();
    }
    private void login2RSService(String token,String uid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {
        GM.logout();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        //Log.e("guoinfo",payParams.toString());
        Map<String, String> payInfo = new HashMap<String, String>();
        try {
            JSONObject datajson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            payInfo.put("productId", datajson.getString("productId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int money = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        payInfo.put("productName", payParams.get(JunSConstants.PAY_ORDER_NAME));
        payInfo.put("productPrice", money+"");
        payInfo.put("productCount", "1");
        payInfo.put("productDesc", payParams.get(JunSConstants.PAY_ORDER_NAME));
        payInfo.put("coinName", "钻石");
        payInfo.put("coinRate", payParams.get(JunSConstants.PAY_RATE));
        payInfo.put("roleId", payParams.get(JunSConstants.PAY_ROLE_ID));
        payInfo.put("roleName", payParams.get(JunSConstants.PAY_ROLE_NAME));
        payInfo.put("roleGrade", payParams.get(JunSConstants.PAY_ROLE_LEVEL));
        payInfo.put("roleBalance", "1");
        payInfo.put("vipLevel", payParams.get(JunSConstants.PAY_ROLE_VIP));
        payInfo.put("partyName", "");
        payInfo.put("zoneId", payParams.get(JunSConstants.PAY_SERVER_ID));
        payInfo.put("zoneName", payParams.get(JunSConstants.PAY_SERVER_NAME));
        payInfo.put("gameReceipts", payParams.get(JunSConstants.PAY_M_ORDER_ID));
        GM.pay(payInfo);
    }

    @Override
    public void exitGame(Activity activity) {
        GuaMExitDialog.showExit(activity, new GuaMExitDialog.ExitCallback() {
            @Override
            public void toContinue() {

            }

            @Override
            public void toExit() {
                GM.quit();
            }
        });
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        Map<String, String> data = new HashMap<String, String>();
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            data.put("dataType", "2");
            data.put("roleCTime", submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME));
        }
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            data.put("dataType", "1");
            data.put("roleCTime", "-1");
        }
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_UPGRADE)){
            data.put("dataType", "3");
            data.put("roleCTime", "-1");
        }
        data.put("roleId", submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
        data.put("roleName", submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        data.put("roleLevel", submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        data.put("zoneId", submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));
        data.put("zoneName", submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
        data.put("balance", "0");
        data.put("partyName", "");
        data.put("vipLevel", submitInfo.get(JunSConstants.SUBMIT_VIP));
        data.put("roleLevelMTime", "-1");
        GM.submitRoleInfo(data);
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        GM.onStart();
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        GM.onResume();
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        GM.onRestart();
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        GM.onPause();
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        GM.onStop();
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        GM.onDestroy();
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        GM.onNewIntent(data);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        GM.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        GM.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
