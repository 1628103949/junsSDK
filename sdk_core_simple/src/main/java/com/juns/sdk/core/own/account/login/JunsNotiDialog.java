package com.juns.sdk.core.own.account.login;

import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.juns.sdk.core.own.event.JSExitEv;
import com.juns.sdk.core.own.event.JSLogoutEv;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class JunsNotiDialog extends BaseDialog<JunsNotiDialog> {

    private Button logoutBtn, exitBtn;
    private View lineBtn;
    static TextView limitTv;
    boolean tip;
    private JunsNotiDialog(Activity act, boolean tip) {
        super(act, false);
        this.tip = tip;
    }

    private static boolean showState = false;

    public static void showNoti(Activity act,String content,boolean tip) {
        if(!showState){
            JunsNotiDialog tipsDialog = new JunsNotiDialog(act,tip);
            tipsDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
            limitTv.setText(content);
            showState = true;
        }

    }
    public static void showNoti(Activity act,String content,boolean tip,int height) {
        if(!showState){
            JunsNotiDialog tipsDialog = new JunsNotiDialog(act,tip);
            tipsDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
            limitTv.setText(content);
            //limitTv.setHeight(height);
            final float scale = act.getResources().getDisplayMetrics().density;
            limitTv.getLayoutParams().height = (int) (height * scale + 0.5f);
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
            exitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JunsNotiDialog.this.dismiss();
                }
            });
        }else{
            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JunsNotiDialog.this.dismiss();
                    Bus.getDefault().post(new JSLogoutEv());
                }
            });

            exitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JunsNotiDialog.this.dismiss();
                    Bus.getDefault().post(JSExitEv.getSucc());
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