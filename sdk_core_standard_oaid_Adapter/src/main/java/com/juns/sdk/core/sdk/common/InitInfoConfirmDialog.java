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

import com.juns.sdk.core.own.event.JSExitEv;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class InitInfoConfirmDialog extends BaseDialog<InitInfoConfirmDialog> {

    private Button continueBtn, exitBtn;
    private TextView firstLine;
    private InitInfoCallBack callback;

    private InitInfoConfirmDialog(Activity act, InitInfoCallBack callback) {
        super(act, false);
        this.callback = callback;
    }

    public static void show(Activity act, InitInfoCallBack exitCallback) {
        InitInfoConfirmDialog tipsDialog = new InitInfoConfirmDialog(act, exitCallback);
        tipsDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_init_info_confirm", mContext), null);
        firstLine = (TextView) contentView.findViewById(ResUtil.getID("init_info_confirm_first", mContext)) ;
        String str = contentView.getResources().getString(ResUtil.getStringID("juns_init_info_confirm_first",mContext));
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(str);
        TextViewSpan1 span1 = new TextViewSpan1();
        stringBuilder.setSpan(span1,6,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextViewSpan2 span2 = new TextViewSpan2();
        stringBuilder.setSpan(span2,13,19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        firstLine.setText(stringBuilder);
        //一定要记得设置这个方法  不是不起作用
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
            //ToastUtil.showToast("这是测试点击1");
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
                InitInfoConfirmDialog.this.dismiss();
                InitInfoDialog.show((Activity) mContext,callback);

            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InitInfoConfirmDialog.this.dismiss();
                Bus.getDefault().post(EvExit.getSucc());
//                SDKCore.getMainAct().finish();
//                InitInfoConfirmDialog.this.dismiss();
//                if (callback != null) {
//                    callback.toExit();
//                }
            }
        });
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }

    public interface Callback {
        void toContinue();

    }
}
