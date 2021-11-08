package com.juns.sdk.framework.view.common;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;

/**
 * User: Ranger
 * Date: 06/03/2018
 * Time: 3:50 PM
 */
public class TipsDialog extends BaseDialog<TipsDialog> {

    private LinearLayout contentLl;
    private TextView titleTv, contentTv;
    private Button cancelBtn, confirmBtn, confirmBtn2;
    private LinearLayout type1Ll, type2Ll;

    private String tipsTitle;
    private String tipsContent;
    private String cancelContent;
    private String confirmContent;

    private TipsCallback mTipsCallback;
    private boolean cancelable = false;
    private boolean isConfirmStatus = false;
    private TipsConfirmCallback mTipsConfirmCallback;

    public TipsDialog(Context ctx, boolean cancelable, String tipsContent, TipsCallback callback) {
        super(ctx, false);
        this.cancelable = cancelable;
        this.mTipsCallback = callback;
        this.tipsContent = tipsContent;
    }

    public TipsDialog(Context ctx, boolean cancelable, String tipsContent, String cancelContent, String confirmContent, TipsCallback callback) {
        super(ctx, false);
        this.cancelable = cancelable;
        this.tipsContent = tipsContent;
        this.cancelContent = cancelContent;
        this.confirmContent = confirmContent;
        this.mTipsCallback = callback;
    }

    public TipsDialog(Context ctx, String tipsContent, TipsConfirmCallback callback) {
        super(ctx, false);
        //单一确定弹窗，不可取消
        this.cancelable = false;
        this.tipsContent = tipsContent;
        this.mTipsConfirmCallback = callback;
        isConfirmStatus = true;
    }

    @Override
    public View onCreateView() {
        View container = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_fw_common_tips_layout", mContext), null);
        contentLl = (LinearLayout) container.findViewById(ResUtil.getID("tn_common_tl_content_rl", mContext));
        titleTv = (TextView) container.findViewById(ResUtil.getID("tn_common_tl_title_tv", mContext));
        contentTv = (TextView) container.findViewById(ResUtil.getID("tn_common_tl_content_tv", mContext));
        cancelBtn = (Button) container.findViewById(ResUtil.getID("tn_common_tl_cancel", mContext));
        confirmBtn = (Button) container.findViewById(ResUtil.getID("tn_common_tl_confirm", mContext));
        type1Ll = (LinearLayout) container.findViewById(ResUtil.getID("tn_common_tl_type1_ll", mContext));
        type2Ll = (LinearLayout) container.findViewById(ResUtil.getID("tn_common_tl_type2_ll", mContext));
        confirmBtn2 = (Button) container.findViewById(ResUtil.getID("tn_common_tl_confirm2", mContext));

        setCancelable(cancelable);
        setCanceledOnTouchOutside(false);

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mTipsCallback != null) {
                    mTipsCallback.onCancel();
                }
            }
        });

        return container;
    }

    @Override
    public void setUiBeforeShow() {

        if (isConfirmStatus) {
            type1Ll.setVisibility(View.GONE);
            type2Ll.setVisibility(View.VISIBLE);
        } else {
            type1Ll.setVisibility(View.VISIBLE);
            type2Ll.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(tipsTitle)) {
            titleTv.setText(tipsTitle);
        }

        if (isConfirmStatus) {
            contentTv.setText(tipsContent);
        } else {
            if (!TextUtils.isEmpty(tipsContent)) {
                contentTv.setText(tipsContent);
            }
        }

        if (!TextUtils.isEmpty(cancelContent)) {
            cancelBtn.setText(cancelContent);
        }

        if (!TextUtils.isEmpty(confirmContent)) {
            confirmBtn.setText(confirmContent);
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mTipsCallback != null) {
                    mTipsCallback.onCancel();
                }
            }
        });

        confirmBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mTipsConfirmCallback != null) {
                    mTipsConfirmCallback.onConfirm();
                }
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mTipsCallback != null) {
                    mTipsCallback.onConfirm();
                }
            }
        });

    }

    public interface TipsCallback {
        void onCancel();

        void onConfirm();
    }

    public interface TipsConfirmCallback {
        void onConfirm();
    }
}
