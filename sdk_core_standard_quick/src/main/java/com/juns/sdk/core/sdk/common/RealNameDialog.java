package com.juns.sdk.core.sdk.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.http.params.UserRealNameParam;
import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.EditText.ClearEditText;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xutils.x;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class RealNameDialog extends BaseDialog<RealNameDialog> implements View.OnClickListener {

    private RelativeLayout closeRl;
    private ImageButton commitBtn;
    private TextView showBtn;
    private Callback mCallback;
    private boolean cancelable = false;
    private ClearEditText nameEt, idCardEt;

    private String uId;
    private String uName;

    private RealNameDialog(Activity act, boolean cancel, Callback callback) {
        super(act, false);
        cancelable = cancel;
        this.mCallback = callback;
    }

    public static void show(Activity act, boolean cancel, Callback callback) {
        RealNameDialog dialog = new RealNameDialog(act, cancel, callback);
        dialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_realname_dialog", mContext), null);
        closeRl = (RelativeLayout) contentView.findViewById(ResUtil.getID("close_rl", mContext));
        commitBtn = (ImageButton) contentView.findViewById(ResUtil.getID("commit_btn", mContext));
        nameEt = (ClearEditText) contentView.findViewById(ResUtil.getID("name_et", mContext));
        idCardEt = (ClearEditText) contentView.findViewById(ResUtil.getID("id_card_et", mContext));
        showBtn = (TextView) contentView.findViewById(ResUtil.getID("show_realname_info", mContext));
        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        if (cancelable) {
            setCancelable(true);
            closeRl.setVisibility(View.VISIBLE);
        } else {
            showBtn.setVisibility(View.VISIBLE);
            setCancelable(false);
            closeRl.setVisibility(View.GONE);
        }
        closeRl.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        showBtn.setOnClickListener(this);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mCallback != null) {
                    mCallback.onCancel();
                }
            }
        });

        idCardEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == closeRl.getId()) {
            RealNameDialog.this.dismiss();
            //close
            if (mCallback != null) {
                mCallback.onCancel();
            }
        } else if (view.getId() == commitBtn.getId()) {
            //认证
            String name = nameEt.getText().toString().trim();
            String idCard = idCardEt.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ViewUtils.sdkShowTips(mContext, "请输入姓名！");
                return;
            }
            if (TextUtils.isEmpty(idCard)) {
                ViewUtils.sdkShowTips(mContext, "请输入身份证号码！");
                return;
            }
            doVerify(name, idCard);

        }else if(view.getId() == showBtn.getId()){
            JunsNotiDialog.showNoti(SDKCore.getMainAct(),"实名限制\n" +
                    "如果您是未成年人，请在家长监督下填写自己真实的身份证信息，根据国家新闻出版署《关于防止未成年人沉迷网络游戏的通知》，对您有以下限制：\n" +
                    "游戏登陆\n" +
                    "1.每日22时至次日8时，该游戏将不以任何形式为未成年人提供游戏服务。\n" +
                    "2.法定节假日每日累计不得超过3小时，其他时间每日累计不得超过1.5小时。\n" +
                    "游戏充值\n" +
                    "1.8岁的用户无法进行游戏充值服务。\n" +
                    "2.8周岁以上未满16周岁的用户，单次充值金额不得超过50元人民币，每月充值金额累计不得超过200元人民币。\n" +
                    "3.16周岁以上未满18周岁的用户，单日充值上限100元，每月上限400元。",true,240);
        }
    }

    private void doVerify(String name, String idCard) {
        UserRealNameParam realNameParam = new UserRealNameParam(name, idCard);
        x.http().post(realNameParam, new com.juns.sdk.framework.xutils.common.Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse result) {
                SDKData.setSdkUserIsVerify(true);
                ViewUtils.sdkShowTips(mContext, "您已经认证成功！");
                dismiss();
                //close
                if (mCallback != null) {
                    mCallback.onSuccess();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex instanceof JunSServerException) {
                    ViewUtils.sdkShowTips(mContext, ((JunSServerException) ex).getServerMsg());
                } else {
                    ViewUtils.sdkShowTips(mContext, "网络异常，请重试！");
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public interface Callback {

        void onSuccess();

        void onCancel();

    }
}
