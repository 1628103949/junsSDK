package com.juns.sdk.core.own.account.login;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.juns.sdk.core.own.account.JunSAccount;
import com.juns.sdk.core.own.account.login.base.LoginBaseView;
import com.juns.sdk.core.own.account.user.User;
import com.juns.sdk.core.own.account.user.UserUtils;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;

import java.util.LinkedList;

/**
 * Data：10/01/2019-9:58 AM
 * Author: ranger
 */
public class LoginDialog extends BaseDialog<LoginDialog> {
    private Activity mActivity;
    private JunSAccount junSAccount;

    private RelativeLayout contentRl;
    private LoginBaseView accountLoginView, accountRegView, phoneRegView, findPwdView;
    private LoginBaseView currentView;

    public LoginDialog(Activity act, JunSAccount account) {
        super(act, false, false);
        this.junSAccount = account;
        this.mActivity = act;
    }

    @Override
    public View onCreateView() {
        View contentView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_login_main_dialog", mContext), null);
        contentRl = (RelativeLayout) contentView.findViewById(ResUtil.getID("content_rl", mContext));
        return contentView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        //检测是否已有帐号
        LinkedList<User> userList = UserUtils.getUserRecord();
        if (!userList.isEmpty()) {
            //已有帐号，显示帐号登录
            accountLoginView = new AccountLoginView(LoginDialog.this, mActivity).build();
            currentView = accountLoginView;
        } else {
            //没有帐号，显示帐号注册
            accountRegView = new AccountRegView(LoginDialog.this, mActivity).build();
            currentView = accountRegView;
        }
        contentRl.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        contentRl.addView(currentView.getFatherView(), lp);

        //处理取消登录/切换帐号逻辑
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                junSAccount.onLoginViewClose();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (accountLoginView != null) {
            accountLoginView.destroyView();
        }
        if (accountRegView != null) {
            accountRegView.destroyView();
        }
        if (phoneRegView != null) {
            phoneRegView.destroyView();
        }
        if (findPwdView != null) {
            findPwdView.destroyView();
        }
    }

    void showAccountLogin() {
        if (accountLoginView == null) {
            accountLoginView = new AccountLoginView(LoginDialog.this, mActivity).build();
        }
        currentView = accountLoginView;
        contentRl.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        contentRl.addView(currentView.getFatherView(), lp);
    }

    void showAccountReg() {
        if (accountRegView == null) {
            accountRegView = new AccountRegView(LoginDialog.this, mActivity).build();
        }
        currentView = accountRegView;
        contentRl.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        contentRl.addView(currentView.getFatherView(), lp);
    }

    void showPhoneReg() {
        if (phoneRegView == null) {
            phoneRegView = new PhoneRegView(LoginDialog.this, mActivity).build();
        }
        currentView = phoneRegView;
        contentRl.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        contentRl.addView(currentView.getFatherView(), lp);
    }

    void showFindPwd() {
        if (findPwdView == null) {
            findPwdView = new FindPwdView(LoginDialog.this, mActivity).build();
        }
        currentView = findPwdView;
        contentRl.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        contentRl.addView(currentView.getFatherView(), lp);
    }

    public JunSAccount getJunSAccount() {
        return junSAccount;
    }
}
