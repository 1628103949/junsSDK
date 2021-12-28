package com.juns.sdk.core.own.pay;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.RelativeLayout;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.sdk.event.OnActivityResult;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;

/**
 * User: Ranger
 * Date: 01/03/2018
 * Time: 9:21 PM
 */
public class PayDialog extends BaseDialog<PayDialog> {

    private Activity mActivity;
    private RelativeLayout contentRl;
    private SdkWebViewHolder sdkWebViewHolder;
    private String payUrl;
    private RelativeLayout closeBtn;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private PayCallback payCallback;

    public PayDialog(Activity act, String url, PayDialog.PayCallback callback) {
        super(act, false);
        this.payUrl = url;
        this.mActivity = act;
        this.payCallback = callback;
    }

    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_pay_dialog", mActivity), null);
        contentRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("content_rl", mActivity));
        closeBtn = (RelativeLayout) containerView.findViewById(ResUtil.getID("close_rl", mActivity));
        return containerView;
    }

    @Override
    public void setUiBeforeShow() {
        initWebView();
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (payCallback != null) {
                    payCallback.onFinish();
                }
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PayDialog.this.dismiss();
            }
        });
    }

    private void initWebView() {
        sdkWebViewHolder = new SdkWebViewHolder(mActivity, false);
        //添加webView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);
        //处理通用界面开放的接口
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new WebInterface(), JunSConstants.JUNS_WEB_OBJ);
        sdkWebViewHolder.loadUrl(payUrl);
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void handleNoticeActivityResult(OnActivityResult onActivityResult) {
        if (sdkWebViewHolder != null) {
            sdkWebViewHolder.handleOnActivityResult(onActivityResult.getRequestCode(),
                    onActivityResult.getResultCode(),
                    onActivityResult.getData());
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (sdkWebViewHolder != null) {
            sdkWebViewHolder.destroy();
            sdkWebViewHolder = null;
        }
    }

    public interface PayCallback {
        void onFinish();
    }

    private class WebInterface {

        /**
         * 关闭
         */
        @JavascriptInterface
        public void closePay() {
            //关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    PayDialog.this.dismiss();
                }
            });
        }

    }

}
