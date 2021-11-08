package com.juns.channel;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.Attention.Tada;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class AntiAddictionTips extends BaseDialog<AntiAddictionTips> {

    private TextView contentTv;
    private RelativeLayout closeRl;
    private boolean cancelable = false;
    private Callback callback;
    private Button exitBtn;

    private String content;
    private SpannableStringBuilder spannableBuilder;

    private AntiAddictionTips(Activity act, String content) {
        super(act, false);
        this.content = content;
    }

    private AntiAddictionTips(Activity act, String content, boolean cancel, Callback cb) {
        super(act, false);
        this.cancelable = cancel;
        this.content = content;
        this.callback = cb;
    }

    private AntiAddictionTips(Activity act, SpannableStringBuilder spannableBuilder) {
        super(act, false);
        this.spannableBuilder = spannableBuilder;
    }

    public static void show(Activity act, String content, boolean cancel, Callback cb) {
        AntiAddictionTips tipsDialog = new AntiAddictionTips(act, content, cancel, cb);
        tipsDialog.showAnim(new Tada()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    public static void show(Activity act, String content) {
        AntiAddictionTips tipsDialog = new AntiAddictionTips(act, content);
        tipsDialog.showAnim(new Tada()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    public static void show(Activity act, SpannableStringBuilder spannableBuilder) {
        AntiAddictionTips tipsDialog = new AntiAddictionTips(act, spannableBuilder);
        tipsDialog.showAnim(new Tada()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("ysdk_anti_addiction_tips", mContext), null);
        contentTv = (TextView) contentView.findViewById(ResUtil.getID("tips_tv", mContext));
        closeRl = (RelativeLayout) contentView.findViewById(ResUtil.getID("close_rl", mContext));
        exitBtn = (Button) contentView.findViewById(ResUtil.getID("exit_btn", mContext));
        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        if (cancelable) {
            setCanceledOnTouchOutside(true);
            setCancelable(true);
            closeRl.setVisibility(View.VISIBLE);
            exitBtn.setVisibility(View.GONE);
        } else {
            setCanceledOnTouchOutside(false);
            setCancelable(false);
            closeRl.setVisibility(View.GONE);
            exitBtn.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(content)) {
            contentTv.setText(spannableBuilder);
        } else {
            contentTv.setText(content);
        }
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (callback != null) {
                    callback.onFinish();
                }
            }
        });
        closeRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bus.getDefault().post(EvExit.getSucc());
            }
        });
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }

    public interface Callback {
        void onFinish();
    }

}
