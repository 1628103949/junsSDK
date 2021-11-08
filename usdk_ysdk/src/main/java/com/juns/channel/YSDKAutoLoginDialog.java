package com.juns.channel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.params.UserLoginParam;
import com.juns.sdk.core.own.account.JunSAccount;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

/**
 * Data：04/03/2019-12:23 PM
 * Author: ranger
 */
public class YSDKAutoLoginDialog extends BaseDialog<YSDKAutoLoginDialog> {

    private static final int AUTO_LOGIN_TIME = 2000;
    private Button switchAccountBtn;
    private TextView userNameTv;
    private ImageView loadingImg;
    private AutoCallback autoCallback;
    private String currentUName = null;
    private String currentUPwd = null;
    private JunSAccount junSAccount = null;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private long startReqTime = 0L;
    private boolean isSwitchEnter = false;

    public YSDKAutoLoginDialog(Context ctx,AutoCallback callback) {
        super(ctx, false);
        this.autoCallback = callback;
    }

    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_auto_login_dialog", mContext), null);
        switchAccountBtn = (Button) containerView.findViewById(ResUtil.getID("switch_account_btn", mContext));
        userNameTv = (TextView) containerView.findViewById(ResUtil.getID("user_name_tv", mContext));
        loadingImg = (ImageView) containerView.findViewById(ResUtil.getID("loading_img", mContext));
        return containerView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        switchAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoCallback != null) {
                    autoCallback.onSwitchAccount();
                    isSwitchEnter = true;
                    YSDKAutoLoginDialog.this.dismiss();
                }
            }
        });
        userNameTv.setText("");

        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(1000);//设置动画持续周期
        rotate.setRepeatCount(-1);//设置重复次数
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10);//执行前的等待时间
        loadingImg.setAnimation(rotate);
        //processAutoLogin();
    }

//    private void processAutoLogin() {
//        startReqTime = System.currentTimeMillis();
//        UserLoginParam userLoginParam = new UserLoginParam(currentUName, currentUPwd);
//        x.http().post(userLoginParam, new Callback.CommonCallback<JunSResponse>() {
//            @Override
//            public void onSuccess(final JunSResponse result) {
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (isSwitchEnter) {
//                            return;
//                        }
//                        junSAccount.dealLoginSuccResult(JunSAccount.AUTO_LOGIN, result, currentUPwd);
//                    }
//                }, AUTO_LOGIN_TIME - System.currentTimeMillis() + startReqTime);
//            }
//
//            @Override
//            public void onError(final Throwable ex, boolean isOnCallback) {
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (isSwitchEnter) {
//                            return;
//                        }
//                        junSAccount.dealLoginFailResult(JunSAccount.AUTO_LOGIN, ex);
//                    }
//                }, AUTO_LOGIN_TIME - System.currentTimeMillis() + startReqTime);
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
//
//    }

    @Override
    public void dismiss() {
        loadingImg.clearAnimation();
        super.dismiss();
    }

    public interface AutoCallback {
        void onSwitchAccount();
    }
}
