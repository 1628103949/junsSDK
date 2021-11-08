package com.juns.sdk.core.own.fw;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;

/**
 * Data：07/03/2019-10:09 AM
 * Author: ranger
 */
public class HideTipsDialog extends BaseDialog<HideTipsDialog> {

    private LinearLayout contentLl;
    private Button cancelBtn, hideBtn;

    private HideCallback mHideCallback;

    public HideTipsDialog(Context ctx, HideCallback callback) {
        super(ctx, false);
        this.mHideCallback = callback;
    }

    @Override
    public View onCreateView() {
        View container = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_float_tips_dialog", mContext), null);
        contentLl = (LinearLayout) container.findViewById(ResUtil.getID("content_ly", mContext));
        cancelBtn = (Button) container.findViewById(ResUtil.getID("cancel_btn", mContext));
        hideBtn = (Button) container.findViewById(ResUtil.getID("hide_btn", mContext));

        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mHideCallback != null) {
                    mHideCallback.onCancel();
                }
            }
        });

        return container;
    }

    @Override
    public void setUiBeforeShow() {

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHideCallback != null) {
                    mHideCallback.onCancel();
                }
                HideTipsDialog.this.dismiss();
            }
        });

        hideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHideCallback != null) {
                    mHideCallback.onHide();
                }
                HideTipsDialog.this.dismiss();
            }
        });

    }

    public interface HideCallback {
        void onCancel();

        void onHide();
    }
}

