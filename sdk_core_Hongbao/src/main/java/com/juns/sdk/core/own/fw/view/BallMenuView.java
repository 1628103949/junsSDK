package com.juns.sdk.core.own.fw.view;

import android.app.Activity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.juns.sdk.core.own.account.JunSAccount;
import com.juns.sdk.core.own.common.JunSWebDialog;
import com.juns.sdk.core.own.event.JSLogoutEv;
import com.juns.sdk.core.own.fw.account.AccountDialog;
import com.juns.sdk.core.own.fw.service.ServiceDialog;
import com.juns.sdk.core.sdk.JunSUtils;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.NoticeDialog;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：2020-05-22-11:54
 * Author: ranger
 */
public class BallMenuView extends FrameLayout {

    private Activity mActivity;
    private LinearLayout menuLl;
    private ServiceDialog serviceDialog;
    private AccountDialog accountDialog;

    private JunSWebDialog junSWebDialog;

    public BallMenuView(Activity act) {
        this(act, null);
    }

    public BallMenuView(Activity act, AttributeSet attrs) {
        this(act, attrs, 0);
        View contentView = inflate(act, ResUtil.getLayoutID("juns_float_menu", act), this);
        this.mActivity = act;
        menuLl = (LinearLayout) contentView.findViewById(ResUtil.getID("menu_ll", mActivity));
    }

    public BallMenuView(Activity act, AttributeSet attrs, int defStyleAttr) {
        super(act, attrs, defStyleAttr);
    }

    public BallMenuView buildItemView() {
        if (!TextUtils.isEmpty(SDKData.getFloatWindowData())) {
            try {
                JSONArray jsonArray = new JSONArray(SDKData.getFloatWindowData());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject itemJson = jsonArray.getJSONObject(i);
                    parseMenuItem(itemJson);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        BallItemView logoutView = new BallItemView(mActivity);
        logoutView.buildLogoutView(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //处理切换帐号逻辑
                //回调登出
                JunSAccount.setIsLogout(true);
                Bus.getDefault().post(new JSLogoutEv());
            }
        });
        menuLl.addView(logoutView);
        return this;
    }

    private void parseMenuItem(final JSONObject itemObj) {
        try {
            String keyName = itemObj.getString("key");
            final String title = itemObj.getString("title");
            final String iconUrl = itemObj.getString("icon");
            boolean isRed = false;
            BallItemView itemView;
            switch (keyName) {
                case "kf":
                    //处理客服
                    isRed = itemObj.optBoolean("red");
                    itemView = new BallItemView(mActivity);
                    itemView.buildView(title, iconUrl, isRed, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //处理客服点击事件
                            try {
                                String hearUrl = itemObj.getString("header");
                                String gzhValue = itemObj.getString("gzaccount");
                                String gzhStr = itemObj.getString("gz");
                                String telInfo = itemObj.getString("info");
                                String telStr = itemObj.getString("tel");
                                if (serviceDialog != null && serviceDialog.isShowing()) {
                                    serviceDialog.dismiss();
                                }
                                serviceDialog = null;
                                serviceDialog = new ServiceDialog(mActivity, title, telInfo, telStr, gzhStr, gzhValue, hearUrl);
                                serviceDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;

                case "account":
                    //处理帐号
                    isRed = itemObj.optBoolean("red");
                    itemView = new BallItemView(mActivity);
                    itemView.buildView(title, iconUrl, isRed, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //处理帐号点击事件
                            try {
                                String userName = itemObj.getString("uname");
                                String userPhone = itemObj.getString("phone");
                                String userRealName = itemObj.getString("name");
                                if (accountDialog != null && accountDialog.isShowing()) {
                                    accountDialog.dismiss();
                                }
                                accountDialog = null;
                                accountDialog = new AccountDialog(mActivity, userName, userPhone, userRealName);
                                accountDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;

                case "libaoo":
                    //处理礼包
                    itemView = new BallItemView(mActivity);
                    isRed = itemObj.optBoolean("red");
                    itemView.buildView(title, iconUrl, isRed, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //处理礼包点击事件
                            //直接打开url附带url
                            try {
                                if (itemObj.has("url")) {
                                    String itemUrl = itemObj.getString("url");
                                    showJunsWebDialog(title, JunSUtils.buildCommonWebUrl(itemUrl, true));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case "hongbao":
                    //处理红包
                    isRed = itemObj.optBoolean("red");
                    itemView = new BallItemView(mActivity);
                    itemView.buildView(title, iconUrl, isRed, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //处理红包点击事件
                            //直接打开url附带url
                            try {
                                if (itemObj.has("url")) {
                                    String itemUrl = itemObj.getString("url");
                                    SDKData.setRedUrl(itemUrl);
                                    NoticeDialog hongbaoDialog = new NoticeDialog(mActivity, JunSUtils.buildCommonWebUrl(itemUrl, true), new NoticeDialog.NoticeCallback() {
                                        @Override
                                        public void onFinish() {
                                            
                                        }
                                    });
                                    hongbaoDialog.show();
                                    //showJunsWebDialog(title, JunSUtils.buildCommonWebUrl(itemUrl, true));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                default:
                    //处理其他，直接打开Web页面item
                    itemView = new BallItemView(mActivity);
                    isRed = itemObj.optBoolean("red");
                    itemView.buildView(title, iconUrl, isRed, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //处理公告，代金劵item
                            //直接打开url附带url
                            try {
                                if (itemObj.has("url")) {
                                    String itemUrl = itemObj.getString("url");
                                    showJunsWebDialog(title, JunSUtils.buildCommonWebUrl(itemUrl, true));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
            }
            menuLl.addView(itemView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showJunsWebDialog(String title, String url) {
        if (junSWebDialog != null) {
            if (junSWebDialog.isShowing()) {
                junSWebDialog.dismiss();
            }
            junSWebDialog = null;
        }
        junSWebDialog = new JunSWebDialog(mActivity, title, url);
        junSWebDialog.showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit()).
                dimEnabled(true)
                .show();
    }

}
