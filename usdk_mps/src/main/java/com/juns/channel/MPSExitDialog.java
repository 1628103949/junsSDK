package com.juns.channel;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class MPSExitDialog extends BaseDialog<MPSExitDialog> {

    private Button continueBtn, exitBtn;
    private MPSExitDialog.ExitCallback exitCallback;

    private MPSExitDialog(Activity act, MPSExitDialog.ExitCallback callback) {
        super(act, false);
        this.exitCallback = callback;
    }

    public static void showExit(Activity act, MPSExitDialog.ExitCallback exitCallback) {
        MPSExitDialog tipsDialog = new MPSExitDialog(act, exitCallback);
        tipsDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_exit_dialog", mContext), null);
        exitBtn = (Button) contentView.findViewById(ResUtil.getID("exit_btn", mContext));
        continueBtn = (Button) contentView.findViewById(ResUtil.getID("continue_btn", mContext));
        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MPSExitDialog.this.dismiss();
                if (exitCallback != null) {
                    exitCallback.toContinue();
                }
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MPSExitDialog.this.dismiss();
                if (exitCallback != null) {
                    exitCallback.toExit();
                }
            }
        });
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }

    public interface ExitCallback {
        void toContinue();

        void toExit();
    }
}