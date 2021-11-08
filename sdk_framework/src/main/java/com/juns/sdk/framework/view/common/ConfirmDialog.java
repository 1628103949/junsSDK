package com.juns.sdk.framework.view.common;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;


/**
 * User: Ranger
 * Date: 06/03/2018
 * Time: 3:50 PM
 */
public class ConfirmDialog extends BaseDialog<ConfirmDialog> {

    private RelativeLayout contentRl;
    private TextView titleTv, contentTv;
    private Button cancelBtn, confirmBtn;

    private String confirmContent;
    private String titleContent;
    private SpannableStringBuilder confirmSpannableBuilder;
    private ConfirmCallback confirmCallback;
    private boolean cancelable = false;

    public ConfirmDialog(Context ctx, String confirmContent, boolean cancelable, ConfirmCallback callback) {
        super(ctx, false);
        this.confirmContent = confirmContent;
        this.cancelable = cancelable;
        this.confirmCallback = callback;
    }

    public ConfirmDialog(Context ctx, String title, String confirmContent, boolean cancelable, ConfirmCallback callback) {
        super(ctx, false);
        this.confirmContent = confirmContent;
        this.cancelable = cancelable;
        this.confirmCallback = callback;
        this.titleContent = title;
    }

    public ConfirmDialog(Context ctx, SpannableStringBuilder spannableStringBuilder, boolean cancelable, ConfirmCallback callback) {
        super(ctx, false);
        this.confirmSpannableBuilder = spannableStringBuilder;
        this.cancelable = cancelable;
        this.confirmCallback = callback;
    }


    @Override
    public View onCreateView() {
        View container = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_fw_common_confirm_layout", mContext), null);
        titleTv = (TextView) container.findViewById(ResUtil.getID("tn_common_title_tv", mContext));
        contentRl = (RelativeLayout) container.findViewById(ResUtil.getID("tn_common_cl_content_ly", mContext));
        contentTv = (TextView) container.findViewById(ResUtil.getID("tn_common_cl_tv", mContext));
        cancelBtn = (Button) container.findViewById(ResUtil.getID("tn_common_cl_cancel_btn", mContext));
        confirmBtn = (Button) container.findViewById(ResUtil.getID("tn_common_cl_confirm_btn", mContext));

        setCancelable(cancelable);
        setCanceledOnTouchOutside(false);

        if (!cancelable) {
            cancelBtn.setVisibility(View.GONE);
        } else {
            cancelBtn.setVisibility(View.VISIBLE);
        }

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (confirmCallback != null) {
                    confirmCallback.onCancel();
                }
            }
        });

        return container;
    }

    @Override
    public void setUiBeforeShow() {

        if (!TextUtils.isEmpty(confirmContent)) {
            contentTv.setText(confirmContent);
        }

        if (!TextUtils.isEmpty(titleContent)) {
            titleTv.setText(titleContent);
        }

        if (confirmSpannableBuilder != null) {
            contentTv.setText(confirmSpannableBuilder);
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (confirmCallback != null) {
                    confirmCallback.onCancel();
                }
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (confirmCallback != null) {
                    confirmCallback.onConfirm();
                }
            }
        });

    }

    public interface ConfirmCallback {
        void onCancel();

        void onConfirm();
    }
}
