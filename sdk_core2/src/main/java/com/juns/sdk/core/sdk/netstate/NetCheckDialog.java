package com.juns.sdk.core.sdk.netstate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.utils.NetworkUtils;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;
import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.core.sdk.event.OnPause;
import com.juns.sdk.core.sdk.event.GOnResume;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class NetCheckDialog extends BaseDialog<NetCheckDialog> {

    private Button settingBtn, exitBtn;
    private NetStateCallback netStateCallback;
    private BroadcastReceiver netStateReceiver;
    private boolean isNetAvailable = false;
    private boolean isPausing = false;

    private NetCheckDialog(Activity act, NetStateCallback callback) {
        super(act, false);
        this.netStateCallback = callback;
    }

    public static void show(Activity act, NetStateCallback netStateCallback) {
        NetCheckDialog netCheckDialog = new NetCheckDialog(act, netStateCallback);
        netCheckDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_network_check", mContext), null);
        exitBtn = (Button) contentView.findViewById(ResUtil.getID("exit_btn", mContext));
        settingBtn = (Button) contentView.findViewById(ResUtil.getID("setting_network_btn", mContext));
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (netStateCallback != null) {
                    netStateCallback.onFail();
                }
            }
        });
        Bus.getDefault().register(NetCheckDialog.this);
        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtils.openWirelessSettings();
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetCheckDialog.this.dismiss();
                //退出游戏
                Bus.getDefault().post(EvExit.getSucc());
            }
        });
        registerNetStateReceiver();
    }

    private void registerNetStateReceiver() {
        unRegisterNetStateReceiver();
        netStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
                        if (NetworkUtils.isConnected()) {
                            isNetAvailable = true;
                            if (netStateCallback != null && !isPausing) {
                                NetCheckDialog.this.dismiss();
                                netStateCallback.onSuccess();
                            }
                        } else {
                            isNetAvailable = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (netStateCallback != null) {
                        NetCheckDialog.this.dismiss();
                        netStateCallback.onFail();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.getApplicationContext().registerReceiver(netStateReceiver, filter);
    }

    private void unRegisterNetStateReceiver() {
        if (netStateReceiver != null) {
            try {
                mContext.getApplicationContext().unregisterReceiver(netStateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void handleOnResume(GOnResume onResume) {
        isPausing = false;
        if (isNetAvailable) {
            if (netStateCallback != null) {
                NetCheckDialog.this.dismiss();
                netStateCallback.onSuccess();
            }
        }
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void handleOnPause(OnPause onPause) {
        isPausing = true;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Bus.getDefault().unregister(NetCheckDialog.this);
        unRegisterNetStateReceiver();
    }

    public interface NetStateCallback {
        void onSuccess();

        void onFail();
    }
}
