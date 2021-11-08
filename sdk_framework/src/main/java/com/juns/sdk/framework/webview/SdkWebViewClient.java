package com.juns.sdk.framework.webview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.juns.sdk.framework.safe.JunSEncrypt;
import com.juns.sdk.framework.view.common.ViewUtils;

import java.util.List;

/**
 * User: Ranger
 * Date: 09/01/2018
 * Time: 2:25 PM
 * Desc: to write
 */

public class SdkWebViewClient extends WebViewClient {

    private Context mContext;
    private WebViewCallback mWevViewCallback;

    public SdkWebViewClient(Context ctx) {
        this.mContext = ctx;
    }

    public SdkWebViewClient(Context ctx, WebViewCallback callback) {
        this.mContext = ctx;
        this.mWevViewCallback = callback;
    }

    private static boolean checkApkExist(Context ctx, Intent intent) {
        if (ctx == null || intent == null) {
            return false;
        }
        List<ResolveInfo> list = ctx.getPackageManager()
                .queryIntentActivities(intent, 0);
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (mWevViewCallback != null) {
            mWevViewCallback.shouldOverrideUrlLoading(view, url);
        }

        //微信H5支付处理
        if (url.startsWith(JunSEncrypt.decryptInfo("M2GegRdWFVw/OjuhMV1xkd9Dfj5MZ21F0vckuCTyYMg="))) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            if (checkApkExist(mContext, intent)) {
                //拉起支付
                mContext.startActivity(intent);
            } else {
                //提示安装微信
                ViewUtils.sdkShowTips(mContext, "需安装微信");
            }
            return true;
        }

        //打开支付宝客户端

        if (url.startsWith(JunSEncrypt.decryptInfo("OOKTvvFnriY3TQUrY3+XOw==")) || (url.startsWith("intent") && url.contains(JunSEncrypt.decryptInfo("OOKTvvFnriY3TQUrY3+XOw==")))) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            if (checkApkExist(mContext, intent)) {
                //拉起支付
                mContext.startActivity(intent);
            } else {
                //提示安装支付宝客户端
                ViewUtils.sdkShowTips(mContext, "需安装支付宝");
            }
            return true;
        }

        //过滤taptap://，为了测试
        if (url.startsWith("taptap://")) {
            return true;
        }

        if (url.startsWith("mailto:")) {
            mContext.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
            return true;
        }

        if (url.startsWith("tel:")) {
            mContext.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (mWevViewCallback != null) {
            mWevViewCallback.onReceivedError(view, description, failingUrl);
        }
    }

    @TargetApi(android.os.Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
        // Redirect to deprecated method, so you can use it in all SDK versions
        onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
    }

    @Override
    public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
        if (mWevViewCallback != null) {
            mWevViewCallback.onPageStarted(view, url);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (mWevViewCallback != null) {
            mWevViewCallback.onPageFinished(view, url);
        }
    }



//    @Override
//    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
//        //接受所有网站的证书
//        //防止游戏链接https出现证书确认界面
//        sslErrorHandler.proceed();
//    }

    public interface WebViewCallback {

        public void shouldOverrideUrlLoading(WebView webView, String url);

        public void onPageStarted(WebView webView, String url);

        public void onPageFinished(WebView webView, String url);

        public void onReceivedError(WebView view, String description, String url);

    }

}
