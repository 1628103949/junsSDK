package com.juns.sdk.core.own.common;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.sdk.event.OnActivityResult;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;

/**
 * Data：07/03/2019-10:39 AM
 * Author: ranger
 */
public class JunSWebDialog extends BaseDialog<JunSWebDialog> {
    private Activity mActivity;

    private RelativeLayout headerContentRl;
    private ImageButton backIBtn, closeIBtn;
    private TextView titleTv;
    private RelativeLayout contentRl;

    private SdkWebViewHolder sdkWebViewHolder;
    private String loadUrl;

    private String mTitle;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public JunSWebDialog(Activity act, String title, String url) {
        super(act, false);
        this.loadUrl = url;
        this.mActivity = act;
        this.mTitle = title;
        Bus.getDefault().register(JunSWebDialog.this);
    }


    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_web_dialog", mActivity), null);
        headerContentRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("header_rl", mActivity));
        backIBtn = (ImageButton) containerView.findViewById(ResUtil.getID("back_btn", mActivity));
        titleTv = (TextView) containerView.findViewById(ResUtil.getID("title_tv", mActivity));
        closeIBtn = (ImageButton) containerView.findViewById(ResUtil.getID("close_btn", mActivity));
        contentRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("content_rl", mActivity));
        return containerView;
    }

    @Override
    public void setUiBeforeShow() {
        initWebView();
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        backIBtn.setVisibility(View.GONE);
        backIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //假如WebView可返回，则返回，否则关闭页面
                if (sdkWebViewHolder != null && sdkWebViewHolder.getSdkWebView() != null && sdkWebViewHolder.getSdkWebView().canGoBack()) {
                    sdkWebViewHolder.getSdkWebView().goBack();
                } else {
                    JunSWebDialog.this.dismiss();
                }
            }
        });
        closeIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭退出
                JunSWebDialog.this.dismiss();
            }
        });

        if (!TextUtils.isEmpty(mTitle)) {
            titleTv.setText(mTitle);
        }
    }

    private void initWebView() {
        sdkWebViewHolder = new SdkWebViewHolder(mActivity, false);
        //添加webView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);
        //处理通用界面开放的接口
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new JunSWebDialog.WebInterface(), JunSConstants.JUNS_WEB_OBJ);
        sdkWebViewHolder.setBackGameCallback(new SdkWebViewHolder.BackGameCallback() {
            @Override
            public void onBack() {
                //关闭退出
                JunSWebDialog.this.dismiss();
            }
        });
        sdkWebViewHolder.loadUrl(loadUrl);
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
        Bus.getDefault().unregister(JunSWebDialog.this);
    }

    private class WebInterface {
        //默认样式
        //1. 无Title导航栏
        //2. 全屏透明
        //3. 可返回键取消

        /**
         * 隐藏title，全屏透明
         */
        @JavascriptInterface
        public void hideTitle() {
            //透明，全屏
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    headerContentRl.setVisibility(View.GONE);
                }
            });
        }

        /**
         * 显示title
         *
         * @param title 显示的标题
         */
        @JavascriptInterface
        public void showTitle(final String title) {
            //背景白色，带有title
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(title)) {
                        titleTv.setText(title);
                        headerContentRl.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        /**
         * 关闭
         */
        @JavascriptInterface
        public void close() {
            //关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    JunSWebDialog.this.dismiss();
                }
            });
        }

    }

}
