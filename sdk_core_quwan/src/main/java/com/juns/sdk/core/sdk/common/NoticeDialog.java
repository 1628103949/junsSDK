package com.juns.sdk.core.sdk.common;

import android.app.Activity;
import android.content.DialogInterface;
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
import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.core.sdk.event.OnActivityResult;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;

/**
 * Data：2019/3/20-3:53 PM
 * Author: ranger
 */
public class NoticeDialog extends BaseDialog<NoticeDialog> {

    private Activity mActivity;

    private RelativeLayout headerContentRl;
    private ImageButton backIBtn, closeIBtn;
    private TextView titleTv;
    private RelativeLayout contentRl;

    private SdkWebViewHolder sdkWebViewHolder;

    private String loadUrl;

    private boolean cancelable = true;

    private SdkWebViewHolder.BackGameCallback webBackGameCallback;
    private String webViewErrorTips = null;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private NoticeCallback noticeCallback;

    public NoticeDialog(Activity act, String url, NoticeCallback callback) {
        super(act, false);
        this.loadUrl = url;
        this.mActivity = act;
        this.noticeCallback = callback;
        Bus.getDefault().register(NoticeDialog.this);
    }

    public NoticeDialog(Activity act, String url, boolean cancel, NoticeCallback callback) {
        super(act, false);
        this.loadUrl = url;
        this.mActivity = act;
        this.cancelable = cancel;
        this.noticeCallback = callback;
        Bus.getDefault().register(NoticeDialog.this);
    }

    public void setWebBackGameCallback(SdkWebViewHolder.BackGameCallback callback, String tips) {
        this.webBackGameCallback = callback;
        this.webViewErrorTips = tips;
    }

    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_notice_dialog", mActivity), null);
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
        setCancelable(cancelable);
        backIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //假如WebView可返回，则返回，否则关闭页面
                if (sdkWebViewHolder != null && sdkWebViewHolder.getSdkWebView() != null && sdkWebViewHolder.getSdkWebView().canGoBack()) {
                    sdkWebViewHolder.getSdkWebView().goBack();
                } else {
                    NoticeDialog.this.dismiss();
                }
            }
        });
        closeIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭退出
                NoticeDialog.this.dismiss();
            }
        });
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (noticeCallback != null) {
                    noticeCallback.onFinish();
                }
            }
        });
    }

    private void initWebView() {
        sdkWebViewHolder = new SdkWebViewHolder(mActivity, false);
        //添加webView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);
        //处理通用界面开放的接口
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new NoticeDialog.WebInterface(), JunSConstants.JUNS_WEB_OBJ);
        if (webBackGameCallback != null) {
            sdkWebViewHolder.setBackGameCallback(webBackGameCallback, webViewErrorTips);
        } else {
            sdkWebViewHolder.setBackGameCallback(new SdkWebViewHolder.BackGameCallback() {
                @Override
                public void onBack() {
                    //关闭退出
                    NoticeDialog.this.dismiss();
                }
            });
        }
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
        Bus.getDefault().unregister(NoticeDialog.this);
    }

    public interface NoticeCallback {
        void onFinish();
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
         * 退出游戏
         */
        @JavascriptInterface
        public void exitGame() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Bus.getDefault().post(EvExit.getSucc());
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
                    headerContentRl.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(title)) {
                        titleTv.setText(title);
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
                    NoticeDialog.this.dismiss();
                }
            });
        }

        /**
         * 设置是否可返回取消
         *
         * @param cancelable 是否可取消，true可以，false不可以
         */
        @JavascriptInterface
        public void setCancelable(final boolean cancelable) {
            //设置是否可返回键关闭
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    NoticeDialog.this.setCancelable(cancelable);
                }
            });
        }

    }

}
