package com.juns.sdk.core.own.account.login.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.juns.sdk.core.own.account.login.AgreementDialog;
import com.juns.sdk.core.own.account.login.LoginDialog;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.view.loading.AVLoadingIndicatorView;

/**
 * Data：28/02/2019-10:50 AM
 * Author: ranger
 */
public abstract class LoginBaseView<T> {

    protected Context mContext;
    protected LoginDialog mLoginDialog;
    protected Activity mActivity;
    private View mContentView;
    private View fatherView;
    private RelativeLayout loadingRl, containerRl;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private AgreementDialog agreementDialog;

    public LoginBaseView(LoginDialog loginDialog, Activity activity) {
        this.mLoginDialog = loginDialog;
        this.mContext = loginDialog.getContext();
        this.mActivity = activity;
    }

    public T build() {
        fatherView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_login_base_layout", mContext), null);
        containerRl = (RelativeLayout) fatherView.findViewById(ResUtil.getID("content_rl", mContext));
        loadingRl = (RelativeLayout) fatherView.findViewById(ResUtil.getID("loading_rl", mContext));
        avLoadingIndicatorView = (AVLoadingIndicatorView) fatherView.findViewById(ResUtil.getID("loading_avi", mContext));
        avLoadingIndicatorView.setIndicatorColor(Color.parseColor("#8a8a8a"));
        avLoadingIndicatorView.show();
        loadingRl.setVisibility(View.GONE);
        loadingRl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mContentView = createContentView();
        RelativeLayout.LayoutParams contentLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerRl.addView(mContentView, contentLP);
        setUiBeforeShow();
        return (T) this;
    }

    /**
     * 返回主容器
     */
    public View getFatherView() {
        return fatherView;
    }

    public T destroyView() {
        loadingRl.setVisibility(View.GONE);
        mContentView = null;
        fatherView = null;
        mContext = null;
        return (T) this;
    }

    /**
     * refresh ui to original status.
     */
    public T refreshUI() {
        loadingRl.setVisibility(View.GONE);
        setUiBeforeShow();
        return (T) this;
    }

    /**
     * create content view.
     *
     * @return contentView
     */
    protected abstract View createContentView();

    /**
     * set business, before ui show.
     */
    protected abstract void setUiBeforeShow();

    /**
     * view on start load.
     */
    public void onViewStartLoad() {
        loadingRl.setVisibility(View.VISIBLE);
        avLoadingIndicatorView.show();
    }


    /**
     * view on finish load.
     */
    public void onViewFinishLoad() {
        avLoadingIndicatorView.hide();
        loadingRl.setVisibility(View.GONE);
    }

    public void onViewTips(String msg) {
        ViewUtils.sdkShowTips(mContext, msg);
    }

    protected void closeDialog() {
        onViewFinishLoad();
        mLoginDialog.getJunSAccount().onLoginViewClose();
    }

    protected void showAgreementDialog(AgreementDialog.AgreementCallback callback) {
        if (agreementDialog != null) {
            if (agreementDialog.isShowing()) {
                agreementDialog.dismiss();
            }
        }
        agreementDialog = null;
        agreementDialog = new AgreementDialog(mActivity, callback);
        agreementDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

}
