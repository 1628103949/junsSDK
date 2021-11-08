package com.juns.channel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

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
import com.quicksdk.Extend;
import com.quicksdk.Payment;
import com.quicksdk.QuickSDK;
import com.quicksdk.Sdk;
import com.quicksdk.User;
import com.quicksdk.entity.GameRoleInfo;
import com.quicksdk.entity.OrderInfo;
import com.quicksdk.entity.UserInfo;
import com.quicksdk.notifier.ExitNotifier;
import com.quicksdk.notifier.InitNotifier;
import com.quicksdk.notifier.LoginNotifier;
import com.quicksdk.notifier.LogoutNotifier;
import com.quicksdk.notifier.PayNotifier;
import com.quicksdk.notifier.SwitchAccountNotifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Quick extends OPlatformSDK {
    private static final String TAG = "Quick";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public Quick(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        com.quicksdk.Sdk.getInstance().onCreate(mainAct);
        QuickSDK.getInstance().setInitNotifier(new InitNotifier() {
            @Override
            public void onSuccess() {
                //初始化成功
                //Bus.getDefault().post(OInitEv.getSucc());
            }
            @Override
            public void onFailed(String message, String trace) {
                //初始化失败
                Bus.getDefault().post(OInitEv.getFail(JunSConstants.Status.USER_CANCEL,message));
            } });
        QuickSDK.getInstance().setLoginNotifier(new LoginNotifier() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                //登录成功，获取到用户信息userInfo
                //通过userInfo中的UID、token做服务器登录认证
                login2RSService(userInfo.getUID(),Extend.getInstance().getChannelType()+"",userInfo.getToken());
            }
            @Override
            public void onCancel() {
                //登录取消
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
            }
            @Override
            public void onFailed(final String message, String trace) {
                //登录失败
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
            }
        });
        QuickSDK.getInstance().setLogoutNotifier(new LogoutNotifier() {
            @Override
            public void onSuccess() {
                //注销成功
                Bus.getDefault().post(new OLogoutEv());
            }
            @Override
            public void onFailed(String message, String trace) {
                //注销失败，不做处理
            }
        });
        QuickSDK.getInstance().setSwitchAccountNotifier(new SwitchAccountNotifier() {
            @Override
            public void onSuccess(UserInfo userInfo) { //切换账号成功的回调，返回新账号的userInfo
                Bus.getDefault().post(new OLogoutEv());
                login2RSService(userInfo.getUID(), Extend.getInstance().getChannelType()+"",userInfo.getToken());
            }

            @Override
            public void onCancel() {
            //切换账号取消
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
            }
            @Override
            public void onFailed(String message, String trace) {
            //切换账号失败
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
            }
        });
        QuickSDK.getInstance().setPayNotifier(new PayNotifier() {
            @Override
            public void onSuccess(String sdkOrderID, String cpOrderID,
                                  String extrasParams) {
                //支付成功
                //sdkOrderID:quick订单号 cpOrderID：游戏订单号
                Bus.getDefault().post(OPayEv.getSucc(cpOrderID));
            }
            @Override
            public void onCancel(String cpOrderID) {
                //支付取消
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"cancel"));
            }
            @Override
            public void onFailed(String cpOrderID, String message, String trace) {
                //支付失败
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL,"fail"));
            }
        });
        QuickSDK.getInstance().setExitNotifier(new ExitNotifier() {
            @Override
            public void onSuccess() {
                Bus.getDefault().post(OExitEv.getSucc());
                //退出成功，游戏在此做自身的退出逻辑处理
            }
            @Override
            public void onFailed(String message, String trace) {
                //退出失败，不做处理
            }
        });
        com.quicksdk.Sdk.getInstance().init(mainAct, SDKApplication.getPlatformConfig().getExt1(),SDKApplication.getPlatformConfig().getExt2());
    }

    @Override
    public void init(Activity activity) {
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        com.quicksdk.User.getInstance().login(activity);
    }

    private void login2RSService(String uid,String channelId,String token) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid",uid);
            dataJson.put("channelId",channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }
    GameRoleInfo roleInfo;
    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        //注：GameRoleInfo的字段，以下所有参数必须传，没有的请模拟一个参数传入;
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            roleInfo = new GameRoleInfo();
            roleInfo.setServerID(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//数字字符串，不能含有中文字符
            roleInfo.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
            roleInfo.setGameRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            roleInfo.setGameRoleID(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            roleInfo.setGameBalance(submitInfo.get(JunSConstants.SUBMIT_BALANCE));
            roleInfo.setVipLevel(submitInfo.get(JunSConstants.SUBMIT_VIP)); //设置当前用户vip等级，必须为数字整型字符串,请勿传"vip1"等类似字符串
            roleInfo.setGameUserLevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));//设置游戏角色等级
            roleInfo.setPartyName(submitInfo.get(JunSConstants.SUBMIT_PARTY_NAME));//设置帮派名称
            roleInfo.setRoleCreateTime(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME)); //UC，当乐与1881，TT渠道必传，值为10位数时间戳
            roleInfo.setPartyId("1100"); //360渠道参数，设置帮派id，必须为整型字符串
            roleInfo.setGameRoleGender("男");//360渠道参数
            roleInfo.setGameRolePower(submitInfo.get(JunSConstants.SUBMIT_POWER)); //360,TT语音渠道参数，设置角色战力，必须为整型字符串
            roleInfo.setPartyRoleId("11"); //360渠道参数，设置角色在帮派中的id
            roleInfo.setPartyRoleName("帮主"); //360渠道参数，设置角色在帮派中的名称
            roleInfo.setProfessionId("38"); //360渠道参数，设置角色职业id，必须为整型字符串
            roleInfo.setProfession("法师"); //360渠道参数，设置角色职业名称
            roleInfo.setFriendlist("无"); //360渠道参数，设置好友关系列表，格式请参考：http://open.quicksdk.net/help/detail/aid/190
            //创建角色
            User.getInstance().setGameRoleInfo(mainAct, roleInfo, true);
        }else {
            roleInfo = new GameRoleInfo();
            roleInfo.setServerID(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//数字字符串，不能含有中文字符
            roleInfo.setServerName(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
            roleInfo.setGameRoleName(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
            roleInfo.setGameRoleID(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));
            roleInfo.setGameBalance(submitInfo.get(JunSConstants.SUBMIT_BALANCE));
            roleInfo.setVipLevel(submitInfo.get(JunSConstants.SUBMIT_VIP)); //设置当前用户vip等级，必须为数字整型字符串,请勿传"vip1"等类似字符串
            roleInfo.setGameUserLevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));//设置游戏角色等级
            roleInfo.setPartyName(submitInfo.get(JunSConstants.SUBMIT_PARTY_NAME));//设置帮派名称
            roleInfo.setRoleCreateTime(submitInfo.get(JunSConstants.SUBMIT_CREATE_TIME)); //UC，当乐与1881，TT渠道必传，值为10位数时间戳
            roleInfo.setPartyId("1100"); //360渠道参数，设置帮派id，必须为整型字符串
            roleInfo.setGameRoleGender("男");//360渠道参数
            roleInfo.setGameRolePower(submitInfo.get(JunSConstants.SUBMIT_POWER)); //360,TT语音渠道参数，设置角色战力，必须为整型字符串
            roleInfo.setPartyRoleId("11"); //360渠道参数，设置角色在帮派中的id
            roleInfo.setPartyRoleName("帮主"); //360渠道参数，设置角色在帮派中的名称
            roleInfo.setProfessionId("38"); //360渠道参数，设置角色职业id，必须为整型字符串
            roleInfo.setProfession("法师"); //360渠道参数，设置角色职业名称
            roleInfo.setFriendlist("无"); //360渠道参数，设置好友关系列表，格式请参考：http://open.quicksdk.net/help/detail/aid/190
            //创建角色
            //进入游戏及角色升级
            User.getInstance().setGameRoleInfo(mainAct, roleInfo, false);
        }



    }

    @Override
    public void logout(Activity mainAct) {
        com.quicksdk.User.getInstance().logout(mainAct);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        float money = Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        GameRoleInfo roleInfo = new GameRoleInfo();
        roleInfo.setServerID(payParams.get(JunSConstants.PAY_SERVER_ID));//数字字符串
        roleInfo.setServerName(payParams.get(JunSConstants.PAY_SERVER_NAME));
        roleInfo.setGameRoleName(payParams.get(JunSConstants.PAY_ROLE_NAME));
        roleInfo.setGameRoleID(payParams.get(JunSConstants.PAY_ROLE_ID));
        roleInfo.setGameUserLevel(payParams.get(JunSConstants.PAY_ROLE_LEVEL));
        roleInfo.setVipLevel(payParams.get(JunSConstants.PAY_ROLE_VIP));
        roleInfo.setRoleCreateTime(roleInfo.getRoleCreateTime()+"");
        roleInfo.setGameBalance(payParams.get(JunSConstants.PAY_ROLE_BALANCE));
        roleInfo.setPartyName("无");
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCpOrderID(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        orderInfo.setGoodsName(payParams.get(JunSConstants.PAY_ORDER_NAME));//商品名称，不带数量
        orderInfo.setCount((int)money*Integer.parseInt(payParams.get(JunSConstants.PAY_RATE)));//游戏币数量
        orderInfo.setAmount(money);
        try {
            JSONObject datajson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            orderInfo.setGoodsID(datajson.getString("productId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        orderInfo.setGoodsDesc(payParams.get(JunSConstants.PAY_ORDER_NAME));
        orderInfo.setPrice(money);
        orderInfo.setExtrasParams(payParams.get(JunSConstants.PAY_M_ORDER_ID));
        logger.print("orderInfo"+orderInfo.toString());
        Payment.getInstance().pay(activity, orderInfo, roleInfo);
    }

    @Override
    public void exitGame(final Activity activity) {
        //通过isShowExitDialog判断渠道sdk是否有退出框
        if(QuickSDK.getInstance().isShowExitDialog()){
            Sdk.getInstance().exit(activity);
        }else{
            // 游戏调用自身的退出对话框，点击确定后，调用quick的exit接口
            new AlertDialog.Builder(activity).setTitle("退出").setMessage("是否退出游戏?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Sdk.getInstance().exit(activity);
                }
            }).setNegativeButton("取消", null).show();
        }
    }

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        com.quicksdk.Sdk.getInstance().onStart(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        com.quicksdk.Sdk.getInstance().onRestart(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        com.quicksdk.Sdk.getInstance().onPause(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        com.quicksdk.Sdk.getInstance().onResume(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        com.quicksdk.Sdk.getInstance().onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        com.quicksdk.Sdk.getInstance().onDestroy(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        super.onNewIntent(mainAct, data);
        com.quicksdk.Sdk.getInstance().onNewIntent(data);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(mainAct, requestCode, resultCode, data);
        com.quicksdk.Sdk.getInstance().onActivityResult(mainAct, requestCode, resultCode, data);
    }
}
