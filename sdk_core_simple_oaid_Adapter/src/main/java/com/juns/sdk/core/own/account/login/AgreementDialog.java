package com.juns.sdk.core.own.account.login;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.juns.sdk.core.sdk.SDKData;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class AgreementDialog extends BaseDialog<AgreementDialog> implements View.OnClickListener {

    private RelativeLayout contentRl, closeRl;
    private Button refuseBtn, acceptBtn;
    private AgreementCallback agreementCallback;

    private SdkWebViewHolder sdkWebViewHolder;


    public AgreementDialog(Activity act, AgreementCallback callback) {
        super(act, false);
        this.agreementCallback = callback;
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_agree_dialog", mContext), null);
        contentRl = (RelativeLayout) contentView.findViewById(ResUtil.getID("agree_ll", mContext));
        closeRl = (RelativeLayout) contentView.findViewById(ResUtil.getID("close_rl", mContext));
        refuseBtn = (Button) contentView.findViewById(ResUtil.getID("refuse_btn", mContext));
        acceptBtn = (Button) contentView.findViewById(ResUtil.getID("accept_btn", mContext));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sdkWebViewHolder = new SdkWebViewHolder(mContext);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);

        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        closeRl.setOnClickListener(this);
        refuseBtn.setOnClickListener(this);
        acceptBtn.setOnClickListener(this);
        //load url
        sdkWebViewHolder.loadUrl(SDKData.getUserAgreement());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == closeRl.getId()) {
            AgreementDialog.this.dismiss();
            //close
            if (agreementCallback != null) {
                agreementCallback.onRefuse();
            }
        } else if (view.getId() == refuseBtn.getId()) {
            AgreementDialog.this.dismiss();
            //refuse
            if (agreementCallback != null) {
                agreementCallback.onRefuse();
            }
        } else if (view.getId() == acceptBtn.getId()) {
            AgreementDialog.this.dismiss();
            //accept
            if (agreementCallback != null) {
                agreementCallback.onAccept();
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        sdkWebViewHolder.destroy();
    }

    public interface AgreementCallback {
        void onRefuse();

        void onAccept();
    }
}
