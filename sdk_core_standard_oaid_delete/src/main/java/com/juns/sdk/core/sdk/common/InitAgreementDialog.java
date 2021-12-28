package com.juns.sdk.core.sdk.common;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.webview.SdkWebViewHolder;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class InitAgreementDialog extends BaseDialog<InitAgreementDialog> implements View.OnClickListener {

    private RelativeLayout contentRl, closeRl;
    private Button refuseBtn, acceptBtn;
    private TextView tvTitle;
    private SdkWebViewHolder sdkWebViewHolder;
    private String title,url;

    public InitAgreementDialog(Activity act, String title ,String url) {
        super(act, false);
        this.title = title;
        this.url = url;
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_init_agree_dialog", mContext), null);
        contentRl = (RelativeLayout) contentView.findViewById(ResUtil.getID("init_agree_ll", mContext));
        tvTitle = (TextView) contentView.findViewById(ResUtil.getID("init_info_agree_title", mContext));
        acceptBtn = (Button) contentView.findViewById(ResUtil.getID("btn_init_agree_confirm", mContext));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sdkWebViewHolder = new SdkWebViewHolder(mContext);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);

        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        acceptBtn.setOnClickListener(this);
        tvTitle.setText(title);
        //load url
        sdkWebViewHolder.loadUrl(url);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == acceptBtn.getId()) {
            InitAgreementDialog.this.dismiss();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        sdkWebViewHolder.destroy();
    }

}
