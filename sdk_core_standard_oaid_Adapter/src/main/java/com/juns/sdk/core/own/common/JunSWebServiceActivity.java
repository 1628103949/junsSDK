package com.juns.sdk.core.own.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.webview.SdkWebViewHolder;
import com.juns.sdk.framework.xbus.Bus;

/**
 * Data：07/03/2019-10:39 AM
 * Author: ranger
 */
public class JunSWebServiceActivity extends Activity {

    private RelativeLayout headerContentRl;
    private ImageButton backIBtn, closeIBtn;
    private TextView titleTv;
    private RelativeLayout contentRl;

    private SdkWebViewHolder sdkWebViewHolder;
    private String loadUrl;

    private String mTitle;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public void initDataUrl(String title, String url) {
        this.loadUrl = url;
        this.mTitle = title;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResUtil.getLayoutID("juns_web_service", this));
        //获取到Intent
        Intent intent = getIntent();
        //从Intent获取到Bundle
        Bundle bundle= intent.getExtras();
        if (bundle != null) {
//            String m = bundle.getString("title");
//            String n = bundle.getString("url");
            initDataUrl(bundle.getString("title"),bundle.getString("url"));
        }
        headerContentRl = (RelativeLayout) findViewById(ResUtil.getID("header_rl", this));
        backIBtn = (ImageButton) findViewById(ResUtil.getID("back_btn", this));
        titleTv = (TextView) findViewById(ResUtil.getID("title_tv", this));
        closeIBtn = (ImageButton) findViewById(ResUtil.getID("close_btn", this));
        contentRl = (RelativeLayout) findViewById(ResUtil.getID("content_rl", this));
        initWebView();
        backIBtn.setVisibility(View.GONE);
        backIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //假如WebView可返回，则返回，否则关闭页面
                if (sdkWebViewHolder != null && sdkWebViewHolder.getSdkWebView() != null && sdkWebViewHolder.getSdkWebView().canGoBack()) {
                    sdkWebViewHolder.getSdkWebView().goBack();
                } else {
                    JunSWebServiceActivity.this.dismiss();
                }
            }
        });
        closeIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭退出
                JunSWebServiceActivity.this.dismiss();
            }
        });

        if (!TextUtils.isEmpty(mTitle)) {
            titleTv.setText(mTitle);
        }
    }



    private void initWebView() {
        sdkWebViewHolder = new SdkWebViewHolder(this, false);
        //添加webView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRl.addView(sdkWebViewHolder.getHolderView(), lp);
        //处理通用界面开放的接口
        sdkWebViewHolder.getSdkWebView().addJavascriptInterface(new JunSWebServiceActivity.WebInterface(), JunSConstants.JUNS_WEB_OBJ);
        sdkWebViewHolder.setBackGameCallback(new SdkWebViewHolder.BackGameCallback() {
            @Override
            public void onBack() {
                //关闭退出
                JunSWebServiceActivity.this.dismiss();
            }
        });
        sdkWebViewHolder.loadUrl(loadUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (sdkWebViewHolder != null) {
            sdkWebViewHolder.handleOnActivityResult(requestCode,
                    resultCode,
                    data);
        }
    }

    public void dismiss() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (sdkWebViewHolder != null) {
            sdkWebViewHolder.destroy();
            sdkWebViewHolder = null;
        }
        Bus.getDefault().unregister(JunSWebServiceActivity.this);
        finish();
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
                    JunSWebServiceActivity.this.dismiss();
                }
            });
        }

    }

}
