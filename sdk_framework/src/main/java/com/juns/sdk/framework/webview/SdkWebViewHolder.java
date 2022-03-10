package com.juns.sdk.framework.webview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.loading.AVLoadingIndicatorView;

import java.util.ArrayList;

/**
 * User: Ranger
 * Date: 09/01/2018
 * Time: 2:53 PM
 */

public class SdkWebViewHolder {
    private View holderView;

    private RelativeLayout webContentRl;

    private RelativeLayout loadingRl;
    private AVLoadingIndicatorView avLoadingIndicatorView;

    private RelativeLayout netErrorRl;
    private Button refreshBtn;
    private TextView tipTv;

    private SdkWebView mSdkWebView;

    private Context mContext;

    private SdkWebChromeClient mSdkWebChromeClient;
    private SdkWebViewClient mSdkWebViewClient;

    private StatusCallback statusCallback;

    private boolean isTran = false;

    private ArrayList<String> allowHosts;

    private BackGameCallback backGameCallback;

    public SdkWebViewHolder(Context ctx) {
        this.mContext = ctx;
        initView();
        initWebView();
    }

    public SdkWebViewHolder(Context ctx, boolean isTran) {
        this.mContext = ctx;
        this.isTran = isTran;
        initView();
        initWebView();
    }

    private void initView() {
        //initial view
        holderView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_fw_common_webview_holder_layout", mContext), null);
        webContentRl = (RelativeLayout) holderView.findViewById(ResUtil.getID("tn_common_wh_content_rl", mContext));
        loadingRl = (RelativeLayout) holderView.findViewById(ResUtil.getID("tn_common_wh_loading_rl", mContext));
        avLoadingIndicatorView = (AVLoadingIndicatorView) holderView.findViewById(ResUtil.getID("tn_user_wh_loading_avi", mContext));
        netErrorRl = (RelativeLayout) holderView.findViewById(ResUtil.getID("tn_common_wh_net_error_content_rl", mContext));
        refreshBtn = (Button) holderView.findViewById(ResUtil.getID("tn_common_wh_refresh_btn", mContext));
        tipTv = (TextView) holderView.findViewById(ResUtil.getID("tn_common_wh_net_error_tips_tv", mContext));

        if (isTran) {
            //透明，设置白色背景
            webContentRl.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        //reset view
        netErrorRl.setVisibility(View.GONE);
        avLoadingIndicatorView.show();
        loadingRl.setVisibility(View.GONE);
        webContentRl.setVisibility(View.VISIBLE);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSdkWebView != null) {
                    mSdkWebView.reload();
                    webContentRl.setVisibility(View.VISIBLE);
                    netErrorRl.setVisibility(View.GONE);
                    loadingRl.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setBackGameCallback(BackGameCallback callback) {
        setBackGameCallback(callback, null);
    }

    public void setBackGameCallback(BackGameCallback callback, String tips) {
        this.backGameCallback = callback;
        netErrorRl.setBackgroundColor(Color.parseColor("#99000000"));
        tipTv.setTextColor(Color.parseColor("#FFFFFF"));
        if (TextUtils.isEmpty(tips)) {
            refreshBtn.setText(ResUtil.getStringID("juns_fw_common_back", mContext));
        } else {
            refreshBtn.setText(tips);
        }
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backGameCallback.onBack();
            }
        });
    }

    private void initWebView() {
        mSdkWebView = new SdkWebView(mContext);

        mSdkWebChromeClient = new SdkWebChromeClient(mContext, new SdkWebChromeClient.ChromeCallBack() {

            @Override
            public void onLoading(WebView view, int newProgress) {
                if (newProgress == 100) {
                    loadingRl.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceiveIcon(WebView view, Bitmap icon) {
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
            }

            @Override
            public void onRequestFocus(WebView view) {
                if (statusCallback != null) {
                    statusCallback.onRequestFocus();
                }
            }
        });

        mSdkWebViewClient = new SdkWebViewClient(mContext, new SdkWebViewClient.WebViewCallback() {
            @Override
            public void shouldOverrideUrlLoading(WebView webView, String url) {
                webView.setVisibility(View.VISIBLE);
                netErrorRl.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView webView, String url) {
                if (statusCallback != null) {
                    statusCallback.onPageStartLoad();
                }

                loadingRl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                if (statusCallback != null) {
                    statusCallback.onPageFinish();
                }
                loadingRl.setVisibility(View.GONE);
            }


            @Override
            public void onReceivedError(WebView view, String description, String url) {
                //去掉检测方式某个引用404使整个页面无法访问
                //这里处理一般web界面
               // Log.e("onReceivedError: " , description + "url"+url);

                if (url.contains("http") || url.contains("https") || url.contains("www")) {
                    netErrorRl.setVisibility(View.VISIBLE);
                    view.setVisibility(View.GONE);
                }
            }
        });

        mSdkWebView.setSdkWebChromeClient(mSdkWebChromeClient);
        mSdkWebView.setSdkWebViewClient(mSdkWebViewClient);

        //add webView to content view
        RelativeLayout.LayoutParams wVLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        webContentRl.addView(mSdkWebView, wVLP);
    }

    /**
     * 1、dialog情况
     * 2、Activity情况
     */
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSdkWebChromeClient != null) {
            mSdkWebChromeClient.handleOnActivityResult(requestCode, resultCode, data);
        }
    }

    public void loadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            mSdkWebView.loadUrl(url);
            //开启cookie
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(mSdkWebView, true);
            } else {
                CookieManager.getInstance().setAcceptCookie(true);
            }
        }
    }

    public void destroy() {
        if (mSdkWebView != null) {
            webContentRl.removeView(mSdkWebView);
            mSdkWebView.removeAllViews();
            mSdkWebView.destroy();
            mSdkWebView = null;
        }
    }

    public View getHolderView() {
        return holderView;
    }

    public SdkWebView getSdkWebView() {
        return mSdkWebView;
    }

    public void setStatusCallback(StatusCallback callback) {
        statusCallback = callback;
    }

    public void setAllowHosts(ArrayList<String> hosts) {
        allowHosts = hosts;
    }

    private boolean isUrlInAllow(String url) {
        try {
            if (allowHosts == null || allowHosts.isEmpty() || TextUtils.isEmpty(url)) {
                return true;
            }
            for (String host : allowHosts) {
                if (url.contains(host)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface StatusCallback {
        void onPageStartLoad();

        void onPageFinish();

        void onRequestFocus();
    }

    public interface BackGameCallback {
        void onBack();
    }
}
