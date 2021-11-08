package com.juns.sdk.framework.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * User: Ranger
 * Date: 09/01/2018
 * Time: 3:41 PM
 */

public class SdkWebView extends WebView {
    private static final String SDK_DEFAULT_UA = "JunS/SDK/Android";

    private Context mContext;

    public SdkWebView(Context ctx) {
        super(ctx);
        this.mContext = ctx;
        initWebViewSettings();
    }

    public SdkWebView(Context ctx, AttributeSet attr) {
        super(ctx, attr);
        this.mContext = ctx;
        initWebViewSettings();
    }

    private void initWebViewSettings() {
        //1、缓存模式设置默认
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        //不支持缩放
        webSetting.setSupportZoom(false);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setDisplayZoomControls(false);

        webSetting.setUseWideViewPort(true);
        //webSetting.setSupportMultipleWindows(true);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);

        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);

        //开启application caches 功能
        webSetting.setAppCacheEnabled(true);
        //设置缓存最大容量
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        //启用地理定位
        webSetting.setGeolocationEnabled(true);
        //适配5.0不允许http和https混合使用情况
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //设置UA
        String userAgentData = webSetting.getUserAgentString();
        //SDK页面规定使用此值
        webSetting.setUserAgentString(userAgentData + SDK_DEFAULT_UA);

        webSetting.setUseWideViewPort(true);  //将图片调整到适合webview的大小
        webSetting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //设置加载进来的页面自适应手机屏幕
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);

        //文件下载功能
        setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    //调用系统下载功能
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    ((Activity) mContext).startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        requestFocusFromTouch();

        //设置没有滚动条
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        //WebView背景设置为透明
        setBackgroundColor(0);
    }

    public void setSdkWebViewClient(SdkWebViewClient webViewClient) {
        if (webViewClient != null) {
            this.setWebViewClient(webViewClient);
        }
    }

    public void setSdkWebChromeClient(SdkWebChromeClient sdkWebChromeClient) {
        if (sdkWebChromeClient != null) {
            this.setWebChromeClient(sdkWebChromeClient);
        }
    }

    public void setSdkUserAgent(String userAgent) {
        if (!TextUtils.isEmpty(userAgent)) {
            String userAgentData = this.getSettings().getUserAgentString();
            userAgentData = userAgentData.replace(SDK_DEFAULT_UA, userAgent);
            this.getSettings().setUserAgentString(userAgentData);
        }
    }

}
