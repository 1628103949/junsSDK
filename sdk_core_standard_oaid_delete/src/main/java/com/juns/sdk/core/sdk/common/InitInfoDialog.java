package com.juns.sdk.core.sdk.common;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.common.ToastUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class InitInfoDialog extends BaseDialog<InitInfoDialog> {

    private Button continueBtn, exitBtn;
    private TextView firstLine,secondLine;
    private InitInfoCallBack callback;

    private InitInfoDialog(Activity act, InitInfoCallBack callback) {
        super(act, false);
        this.callback = callback;
    }

    public static void show(Activity act, InitInfoCallBack exitCallback) {
        InitInfoDialog tipsDialog = new InitInfoDialog(act, exitCallback);
        tipsDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_init_info", mContext), null);
        firstLine = (TextView) contentView.findViewById(ResUtil.getID("init_info_first", mContext)) ;
        secondLine = (TextView) contentView.findViewById(ResUtil.getID("init_info_second", mContext));
        String str = contentView.getResources().getString(ResUtil.getStringID("juns_init_info_first",mContext));
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(str);
        TextViewSpan1 span1 = new TextViewSpan1();
        stringBuilder.setSpan(span1,76,82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextViewSpan2 span2 = new TextViewSpan2();
        stringBuilder.setSpan(span2,83,89, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        firstLine.setText(stringBuilder);
        //一定要记得设置这个方法  不是不起作用
        secondLine.setText("1.为向您提供游戏服务，我们将依据本游戏的隐私政策收集、使用、存储必要的信息。\n" +
                "2.基于您的明示授权，我们可能会申请开启您的设备权限，您有权拒绝或取消授权。\n" +
                "3.您可以访问、更正、删除您的个人信息，还可以撤回授权同意、注销账号、投诉举报以及调整其他隐私设置。\n" +
                "4.我们已采取符合业界标准的安全防护措施保护您的个人信息。\n" +
                "5.如您是未成年人，请您和您的监护人仔细阅读本隐私政策，并在征得您的监护人授权同意的前提下使用我们的服务或向我们提供个人信息。\n" +
                "如您同意本游戏的用户协议及隐私政策，请您点击“同意”开始使用我们的产品和服务，我们将尽全力保护您的个人信息安全。");
        firstLine.setMovementMethod(LinkMovementMethod.getInstance());
        exitBtn = (Button) contentView.findViewById(ResUtil.getID("init_disagree", mContext));
        continueBtn = (Button) contentView.findViewById(ResUtil.getID("init_agree", mContext));
        return contentView;
    }

    private class TextViewSpan1 extends ClickableSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(Color.RED);
        }
        @Override
        public void onClick(View widget) {
            new InitAgreementDialog((Activity) mContext,"用户协议","https://junshanggame.com/uagree/user_xiaomi.html")
            .showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
            //点击事件
        }
    }

    private class TextViewSpan2 extends ClickableSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(Color.RED);
        }
        @Override
        public void onClick(View widget) {
            new InitAgreementDialog((Activity) mContext,"隐私政策","https://junshanggame.com/uagree/privacy_xiaomi.html")
                    .showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
            //点击事件
            //ToastUtil.showToast("这是测试点击2");
        }
    }


    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InitInfoDialog.this.dismiss();
                if (callback != null) {
                    callback.toContinue();
                }
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InitInfoDialog.this.dismiss();
                InitInfoConfirmDialog.show((Activity) mContext, callback);
            }
        });
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }

}
