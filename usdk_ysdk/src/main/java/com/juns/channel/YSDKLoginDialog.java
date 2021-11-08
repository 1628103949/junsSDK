package com.juns.channel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.module.user.UserLoginRet;

/**
 * User: Ranger
 * Date: 01/11/2018-4:10 PM
 */

class YSDKLoginDialog extends BaseDialog<YSDKLoginDialog> {

    private ImageView qqImg, wxImg;
    private LoginViewCallback loginViewCallback;

    public YSDKLoginDialog(Context ctx, LoginViewCallback callback) {
        super(ctx);
        loginViewCallback = callback;
    }

    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("fy_tencent_login", mContext), null);
        qqImg = (ImageView) containerView.findViewById(ResUtil.getID("fy_login_qq", mContext));
        wxImg = (ImageView) containerView.findViewById(ResUtil.getID("fy_login_weixin", mContext));
        return containerView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        qqImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YSDKLoginDialog.this.dismiss();
                if (loginViewCallback != null) {
                    loginViewCallback.onQQ();
                }
            }
        });

        wxImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YSDKLoginDialog.this.dismiss();
                if (loginViewCallback != null) {
                    loginViewCallback.onWX();
                }
            }
        });
    }

    interface LoginViewCallback {
        void onQQ();

        void onWX();
    }
}
