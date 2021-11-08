package com.juns.sdk.core.own.fw.service;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.circleimageview.CircleImageView;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.xutils.image.ImageOptions;
import com.juns.sdk.framework.xutils.x;

/**
 * Data：04/03/2019-12:23 PM
 * Author: ranger
 */
public class ServiceDialog extends BaseDialog<ServiceDialog> {

    private Button gzhBtn;
    private TextView titleTv, telTv;
    private CircleImageView circleImageView;
    private RelativeLayout closeBtn;
    private RelativeLayout telRl;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private String mTitle, mTelInfo, mTel, mGzhStr, mGzhValue, mLogoUrl;

    public ServiceDialog(Context ctx, String title, String telInfo, String tel, String gzhStr, String gzhValue, String logoUrl) {
        super(ctx, false);
        this.mTitle = title;
        this.mGzhStr = gzhStr;
        this.mGzhValue = gzhValue;
        this.mLogoUrl = logoUrl;
        this.mTel = tel;
        this.mTelInfo = telInfo;
    }

    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_service_dialog", mContext), null);
        gzhBtn = (Button) containerView.findViewById(ResUtil.getID("gzh_btn", mContext));
        titleTv = (TextView) containerView.findViewById(ResUtil.getID("title_tv", mContext));
        telTv = (TextView) containerView.findViewById(ResUtil.getID("tel_tv", mContext));
        circleImageView = (CircleImageView) containerView.findViewById(ResUtil.getID("service_icon_img", mContext));
        closeBtn = (RelativeLayout) containerView.findViewById(ResUtil.getID("close_rl", mContext));
        telRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("tel_rl", mContext));
        return containerView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(true);
//        if (!TextUtils.isEmpty(mTitle)) {
//            titleTv.setText(mTitle);
//        }
        if (!TextUtils.isEmpty(mGzhStr)) {
            gzhBtn.setText(mGzhStr);
        }
        if (!TextUtils.isEmpty(mTelInfo)) {
            telTv.setText(mTelInfo);

            telRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //拨打电话
                    callPhone(mTel);
                }
            });
        }
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceDialog.this.dismiss();
            }
        });
        if (!TextUtils.isEmpty(mGzhValue)) {
            gzhBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //获取剪贴板管理器：
                    ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("weixin_gzh", mGzhValue);
                    // 将ClipData内容放到系统剪贴板里
                    cm.setPrimaryClip(mClipData);
                    ViewUtils.sdkShowTips(mContext, "公众号已复制成功，正在为您打开微信！");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openWechatApi();
                        }
                    }, 500);
                }
            });
        }

        loadImage(mLogoUrl);
    }

    private void loadImage(String logoUrl) {
        ImageOptions imageOptions = new ImageOptions.Builder().
                setLoadingDrawableId(ResUtil.getDrawableID("juns_service", mContext))
                .setFailureDrawableId(ResUtil.getDrawableID("juns_service", mContext))
                .build();
        x.image().bind(circleImageView, logoUrl, imageOptions);
    }

    /**
     * 跳转到微信
     */
    private void openWechatApi() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ViewUtils.sdkShowTips(mContext, "检查到您手机没有安装微信，请安装后使用该功能!");
        }
    }

    /**
     * 拨打电话（直接拨打电话）
     *
     * @param phoneNum 电话号码
     */
    public void callPhone(String phoneNum) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            Uri data = Uri.parse("tel:" + phoneNum);
            intent.setData(data);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
