package com.juns.channel;

import android.app.Activity;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;
import com.tencent.ysdk.api.YSDKApi;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class YSDKNotiDialog extends BaseDialog<YSDKNotiDialog> {

    private Button logoutBtn, exitBtn;
    static TextView limitTv;
    private View lineBtn;
    boolean tip;
    private YSDKNotiDialog(Activity act,boolean tip) {
        super(act, false);
        this.tip = tip;
    }
    private static boolean showState = false;
    public static void showNoti(Activity act,String content,boolean tip) {
        if(!showState){
            YSDKNotiDialog tipsDialog = new YSDKNotiDialog(act,tip);
            tipsDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
            limitTv.setText(content);
            showState = true;
        }

    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_noti_dialog", mContext), null);
        logoutBtn = (Button) contentView.findViewById(ResUtil.getID("logout_btn", mContext));
        exitBtn = (Button) contentView.findViewById(ResUtil.getID("exit_btn", mContext));
        limitTv = (TextView) contentView.findViewById(ResUtil.getID("limit_tv", mContext));
        limitTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        lineBtn = (View) contentView.findViewById(ResUtil.getID("bt_line", mContext));
        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        if(tip){
            logoutBtn.setVisibility(View.GONE);
            lineBtn.setVisibility(View.GONE);
            exitBtn.setText("确定");
            //exitBtn.setTextColor(ResUtil.getDrawableID("juns_in_blue", mContext));
            exitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    YSDKNotiDialog.this.dismiss();
                }
            });
        }else{
            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    YSDKNotiDialog.this.dismiss();
                    YSDKApi.logout();
                    Bus.getDefault().post(new OLogoutEv());
                }
            });

            exitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    YSDKNotiDialog.this.dismiss();
                    Bus.getDefault().post(OExitEv.getSucc());
                }
            });
        }

    }


    @Override
    public void dismiss() {
        super.dismiss();
        showState = false;
    }


}