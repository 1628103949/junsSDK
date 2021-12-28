package com.juns.sdk.core.own.account.login;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.http.params.UserRegParam;
import com.juns.sdk.core.own.account.JunSAccount;
import com.juns.sdk.core.own.account.login.base.LoginBaseView;
import com.juns.sdk.core.own.account.user.UserRandomUtils;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.EditText.ClearEditText;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

/**
 * Data：28/02/2019-10:45 AM
 * Author: ranger
 */
public class AccountRegView extends LoginBaseView<AccountRegView> {

    private ImageButton regBtn;
    private RelativeLayout phoneRegBtn, accountLoginBtn;
    private ClearEditText nameEt, pwdEt;
    private RelativeLayout closeRl;
    private CheckBox agreeCheckBox;
    private TextView agreeTv;
    private RegSuccessDialog regSuccessDialog;

    private String currentName, currentPwd;

    private AccountPopView accountPopView;

    private boolean isAutoRandom = true;

    //默认同意协议
    private boolean isAgreement = true;

    public AccountRegView(LoginDialog loginDialog, Activity activity) {
        super(loginDialog, activity);
    }

    @Override
    protected View createContentView() {
        View containView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_reg_view", mContext), null);
        regBtn = (ImageButton) containView.findViewById(ResUtil.getID("reg_btn", mContext));
        phoneRegBtn = (RelativeLayout) containView.findViewById(ResUtil.getID("phone_reg_btn", mContext));
        accountLoginBtn = (RelativeLayout) containView.findViewById(ResUtil.getID("account_login_btn", mContext));
        nameEt = (ClearEditText) containView.findViewById(ResUtil.getID("user_et", mContext));
        pwdEt = (ClearEditText) containView.findViewById(ResUtil.getID("pwd_et", mContext));
        closeRl = (RelativeLayout) containView.findViewById(ResUtil.getID("close_rl", mContext));
        agreeCheckBox = (CheckBox) containView.findViewById(ResUtil.getID("agree_checkbox", mContext));
        agreeTv = (TextView) containView.findViewById(ResUtil.getID("agree_tv", mContext));
        return containView;
    }

    @Override
    protected void setUiBeforeShow() {
        nameEt.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        nameEt.getText().clear();
        pwdEt.getText().clear();
        closeRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDialog();
            }
        });

        //设置密码可视
        pwdEt.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        pwdEt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        agreeCheckBox.setChecked(isAgreement);
        agreeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isCheck) {
                if (isCheck) {
                    isAgreement = true;
                } else {
                    isAgreement = false;
                }
            }
        });
        agreeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //userAgreement
                showAgreementDialog(new AgreementDialog.AgreementCallback() {
                    @Override
                    public void onRefuse() {
                        isAgreement = false;
                        agreeCheckBox.setChecked(isAgreement);
                    }

                    @Override
                    public void onAccept() {
                        isAgreement = true;
                        agreeCheckBox.setChecked(isAgreement);
                    }
                });
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录操作
                currentName = nameEt.getText().toString().trim();
                currentPwd = pwdEt.getText().toString().trim();
                if (TextUtils.isEmpty(currentName)) {
                    onViewTips("请输入您的用户名！");
                    return;
                }
                if (TextUtils.isEmpty(currentPwd)) {
                    onViewTips("请输入您的密码！");
                    return;
                }
                checkAgreementStatus();
            }
        });

        accountLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginDialog.showAccountLogin();
            }
        });

        phoneRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginDialog.showPhoneReg();
            }
        });
        if (TextUtils.isEmpty(SDKData.getRandomUserName())) {
            SDKData.setRandomUserName(UserRandomUtils.randomUserName());
        }
        if (TextUtils.isEmpty(SDKData.getRandomUserPwd())) {
            SDKData.setRandomUserPwd(UserRandomUtils.randomPwd());
        }

        nameEt.setText(SDKData.getRandomUserName());
        nameEt.setSelection(nameEt.getText().length());

        pwdEt.setText(SDKData.getRandomUserPwd());
        pwdEt.setSelection(pwdEt.getText().length());

        isAutoRandom = true;
    }

    @Override
    public AccountRegView destroyView() {
        if (accountPopView != null) {
            accountPopView.dismiss();
        }
        accountPopView = null;
        return super.destroyView();
    }

    private void checkAgreementStatus() {
        if (!isAgreement) {
            //弹出协议
            AgreementDialog.AgreementCallback callback = new AgreementDialog.AgreementCallback() {
                @Override
                public void onRefuse() {
                    isAgreement = false;
                    agreeCheckBox.setChecked(isAgreement);
                }

                @Override
                public void onAccept() {
                    isAgreement = true;
                    agreeCheckBox.setChecked(isAgreement);
                    doRegister();
                }
            };
            showAgreementDialog(callback);
        } else {
            //直接登录
            doRegister();
        }
    }

    private void doRegister() {
        onViewStartLoad();
        UserRegParam userRegParam = new UserRegParam(currentName, currentPwd);
        x.http().post(userRegParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(final JunSResponse result) {
                onViewFinishLoad();
                if (!TextUtils.isEmpty(currentName) || !TextUtils.isEmpty(SDKData.getRandomUserName())) {
                    if (currentName.equals(SDKData.getRandomUserName())) {
                        SDKData.setRandomUserName("");
                    }
                }

                if (!TextUtils.isEmpty(currentPwd) || !TextUtils.isEmpty(SDKData.getRandomUserPwd())) {
                    if (currentPwd.equals(SDKData.getRandomUserPwd())) {
                        SDKData.setRandomUserPwd("");
                    }
                }
                showSuccessDialog(new RegSuccessDialog.SuccessCallback() {
                    @Override
                    public void onFinish() {
                        mLoginDialog.getJunSAccount().dealLoginSuccResult(JunSAccount.ACCOUNT_REG, result, currentPwd);
                    }
                });
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                onViewFinishLoad();
                if (isAutoRandom) {
                    //自动注册失败，提示手动输入帐号与密码
                    if (ex instanceof JunSServerException) {
                        ViewUtils.sdkShowTips(mActivity, "注册失败, 请手动输入您的帐号！");
                        nameEt.getText().clear();
                        pwdEt.getText().clear();
                        isAutoRandom = false;
                    } else {
                        ViewUtils.sdkShowTips(mActivity, "网络发生异常，请重试！");
                    }
                } else {
                    mLoginDialog.getJunSAccount().dealLoginFailResult(JunSAccount.ACCOUNT_REG, ex);
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

    private void showSuccessDialog(RegSuccessDialog.SuccessCallback callback) {
        if (regSuccessDialog != null) {
            if (regSuccessDialog.isShowing()) {
                regSuccessDialog.dismiss();
            }
        }
        regSuccessDialog = null;
        regSuccessDialog = new RegSuccessDialog(mActivity, currentName, currentPwd, callback);
        regSuccessDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

}
