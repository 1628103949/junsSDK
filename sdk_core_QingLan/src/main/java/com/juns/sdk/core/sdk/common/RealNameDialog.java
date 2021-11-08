package com.juns.sdk.core.sdk.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.http.params.UserRealNameParam;
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
        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        if (cancelable) {
            setCancelable(true);
            closeRl.setVisibility(View.VISIBLE);
        } else {
            setCancelable(false);
            closeRl.setVisibility(View.GONE);
        }
        closeRl.setOnClickListener(this);
        commitBtn.setOnClickListener(this);

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
