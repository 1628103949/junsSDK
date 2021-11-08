package com.juns.sdk.core.own.fw.account;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.http.params.PhoneBindCodeParam;
import com.juns.sdk.core.http.params.PhoneBindVerifyParam;
import com.juns.sdk.core.http.params.PhoneResetCodeParam;
import com.juns.sdk.core.http.params.PhoneResetVerifyParam;
import com.juns.sdk.core.http.params.UserRealNameParam;
import com.juns.sdk.core.own.account.user.UserUtils;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.utils.SizeUtils;
import com.juns.sdk.framework.view.EditText.ClearEditText;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：04/03/2019-12:23 PM
 * Author: ranger
 */
public class AccountDialog extends BaseDialog<AccountDialog> {
    //通用
    private RelativeLayout backRl, closeRl;
    private TextView titleTv;
    private View containerView;
    private Activity mActivity;
    //帐号
    private LinearLayout mainContentLl;
    private TextView mainNameTv, mainPhoneTv, realNameTv;
    private RelativeLayout resetPwdRl, bindPhoneRl, realNameRl;
    private ImageView bindForwardImg, realNameForwardImg;
    //密码
    private RelativeLayout resetContentRl;
    private Button resetBtn, resetCodeBtn;
    private ImageView resetPhoneImg, resetCodeImg;
    private ClearEditText resetPhoneEt, resetCodeEt;
    //手机号
    private RelativeLayout bindContentRl;
    private Button bindBtn, bindCodeBtn;
    private ImageView bindPhoneImg, bindCodeImg;
    private ClearEditText bindPhoneEt, bindCodeEt;
    //实名
    private RelativeLayout realnameContentRl;
    private Button realnameBtn;
    private ClearEditText realnameNameEt, realnameIdEt;

    public static String mUserName, mUserPhone, mUserRealName;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private CountDownTimer resetCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(final long millisUntilFinished) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    resetCodeBtn.setText(String.valueOf(millisUntilFinished / 1000) + "秒后重发");
                }
            });
        }

        @Override
        public void onFinish() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    resetCodeBtn.setClickable(true);
                    resetCodeBtn.setTextColor(Color.parseColor("#00BAFF"));
                    try {
                        resetCodeBtn.setBackgroundResource(ResUtil.getDrawableID("juns_fw_btn_bg_tran", mContext));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    resetCodeBtn.setText(ResUtil.getStringID("juns_get_phone_code", mContext));
                    resetCodeBtn.setTextSize(9);
                }
            });
        }
    };

    private CountDownTimer bindCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(final long millisUntilFinished) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    bindCodeBtn.setText(String.valueOf(millisUntilFinished / 1000) + "秒后重发");
                }
            });
        }

        @Override
        public void onFinish() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    bindCodeBtn.setClickable(true);
                    bindCodeBtn.setTextColor(Color.parseColor("#00BAFF"));
                    try {
                        bindCodeBtn.setBackgroundResource(ResUtil.getDrawableID("juns_fw_btn_bg_tran", mContext));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bindCodeBtn.setText(ResUtil.getStringID("juns_get_phone_code", mContext));
                    bindCodeBtn.setTextSize(9);
                }
            });
        }
    };

    public AccountDialog(Activity act, String userName, String userPhone, String userRealName) {
        super(act, false);
        this.mActivity = act;
        if (!TextUtils.isEmpty(mUserName)) {

        }else{
            this.mUserName = userName;
        }
        if (!TextUtils.isEmpty(mUserPhone)) {

        }else{
            this.mUserPhone = userPhone;
        }
        if (!TextUtils.isEmpty(mUserRealName)) {

        }else{
            this.mUserRealName = userRealName;
        }
    }

    /**
     * 手机号码，中间4位星号替换
     *
     * @param phone 手机号
     * @return 星号替换的手机号
     */
    private static String phoneNoHide(String phone) {
        // 括号表示组，被替换的部分$n表示第n组的内容
        // 正则表达式中，替换字符串，括号的意思是分组，在replace()方法中，
        // 参数二中可以使用$n(n为数字)来依次引用模式串中用括号定义的字串。
        // "(\d{3})\d{4}(\d{4})", "$1****$2"的这个意思就是用括号，
        // 分为(前3个数字)中间4个数字(最后4个数字)替换为(第一组数值，保持不变$1)(中间为*)(第二组数值，保持不变$2)
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_float_account_dialog", mContext), null);
        backRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("back_rl", mActivity));
        closeRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("close_rl", mActivity));
        titleTv = (TextView) containerView.findViewById(ResUtil.getID("title_tv", mActivity));

        mainContentLl = (LinearLayout) containerView.findViewById(ResUtil.getID("main_content_rl", mActivity));
        mainNameTv = (TextView) containerView.findViewById(ResUtil.getID("main_name_tv", mActivity));
        mainPhoneTv = (TextView) containerView.findViewById(ResUtil.getID("main_phone_tv", mActivity));
        realNameTv = (TextView) containerView.findViewById(ResUtil.getID("main_realname_tv", mActivity));
        resetPwdRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("main_reset_rl", mActivity));
        bindPhoneRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("main_phone_rl", mActivity));
        realNameRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("main_realname_rl", mActivity));
        bindForwardImg = (ImageView) containerView.findViewById(ResUtil.getID("phone_forward", mActivity));
        realNameForwardImg = (ImageView) containerView.findViewById(ResUtil.getID("realname_forward", mActivity));

        resetContentRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("reset_pwd_content_rl", mActivity));
        resetBtn = (Button) containerView.findViewById(ResUtil.getID("reset_pwd_btn", mActivity));
        resetCodeBtn = (Button) containerView.findViewById(ResUtil.getID("reset_code_btn", mActivity));
        resetPhoneImg = (ImageView) containerView.findViewById(ResUtil.getID("reset_phone_img", mActivity));
        resetCodeImg = (ImageView) containerView.findViewById(ResUtil.getID("reset_code_img", mActivity));
        resetPhoneEt = (ClearEditText) containerView.findViewById(ResUtil.getID("reset_phone_et", mActivity));
        resetCodeEt = (ClearEditText) containerView.findViewById(ResUtil.getID("reset_code_et", mActivity));

        bindContentRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("bind_phone_content_rl", mActivity));
        bindBtn = (Button) containerView.findViewById(ResUtil.getID("bind_phone_btn", mActivity));
        bindCodeBtn = (Button) containerView.findViewById(ResUtil.getID("bind_code_btn", mActivity));
        bindPhoneImg = (ImageView) containerView.findViewById(ResUtil.getID("bind_phone_img", mActivity));
        bindCodeImg = (ImageView) containerView.findViewById(ResUtil.getID("bind_code_img", mActivity));
        bindPhoneEt = (ClearEditText) containerView.findViewById(ResUtil.getID("bind_phone_et", mActivity));
        bindCodeEt = (ClearEditText) containerView.findViewById(ResUtil.getID("bind_code_et", mActivity));

        realnameContentRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("real_name_content_rl", mActivity));
        realnameBtn = (Button) containerView.findViewById(ResUtil.getID("real_name_btn", mActivity));
        realnameNameEt = (ClearEditText) containerView.findViewById(ResUtil.getID("user_et", mActivity));
        realnameIdEt = (ClearEditText) containerView.findViewById(ResUtil.getID("id_et", mActivity));

        return containerView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        closeRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountDialog.this.dismiss();
            }
        });
        initMainView();
    }

    private void initMainView() {
        backRl.setVisibility(View.GONE);
        titleTv.setText(ResUtil.getStringID("juns_account", mActivity));
        mainContentLl.setVisibility(View.VISIBLE);
        resetContentRl.setVisibility(View.GONE);
        bindContentRl.setVisibility(View.GONE);
        realnameContentRl.setVisibility(View.GONE);
        mainNameTv.setText(mUserName);

        resetPwdRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //提示先绑定手机
                if (TextUtils.isEmpty(mUserPhone)) {
                    ViewUtils.sdkShowTips(mActivity, "请先绑定手机号码！");
                } else {
                    //重置密码
                    initResetView();
                }
            }
        });


        if (!TextUtils.isEmpty(mUserPhone)) {
            mainPhoneTv.setText(mUserPhone);
            RelativeLayout.LayoutParams phoneLP = (RelativeLayout.LayoutParams) mainPhoneTv.getLayoutParams();
            phoneLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            phoneLP.setMargins(0, 0, SizeUtils.dp2px(12), 0);
            bindForwardImg.setVisibility(View.GONE);
            bindPhoneRl.setClickable(false);
        } else {
            //绑定手机
            bindPhoneRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //绑定手机
                    initBindView();
                }
            });
            bindForwardImg.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mUserRealName)) {
            realNameTv.setText(mUserRealName);
            RelativeLayout.LayoutParams realnameLP = (RelativeLayout.LayoutParams) realNameTv.getLayoutParams();
            realnameLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            realnameLP.setMargins(0, 0, SizeUtils.dp2px(12), 0);
            realNameForwardImg.setVisibility(View.GONE);
            realNameRl.setClickable(false);
        } else {
            //实名认证
            realNameRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //实名认证
                    initRealnameView();
                }
            });
            realNameForwardImg.setVisibility(View.VISIBLE);
        }
    }

    private void initResetView() {
        backRl.setVisibility(View.VISIBLE);
        backRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMainView();
            }
        });
        titleTv.setText(ResUtil.getStringID("juns_reset_pwd", mActivity));
        resetContentRl.setVisibility(View.VISIBLE);
        mainContentLl.setVisibility(View.GONE);
        bindContentRl.setVisibility(View.GONE);
        realnameContentRl.setVisibility(View.GONE);

//        resetPhoneEt.setRawInputType(Configuration.KEYBOARD_QWERTY);
        resetPhoneEt.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        resetPhoneEt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        resetPhoneEt.setText(mUserName);

//        resetPhoneEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    resetPhoneImg.setImageResource(ResUtil.getDrawableID("juns_phone_select", mContext));
//                } else {
//                    resetPhoneImg.setImageResource(ResUtil.getDrawableID("juns_phone", mContext));
//                }
//            }
//        });

        resetCodeEt.setRawInputType(Configuration.KEYBOARD_QWERTY);
//        resetCodeImg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    resetCodeImg.setImageResource(ResUtil.getDrawableID("juns_safe_select", mContext));
//                } else {
//                    resetCodeImg.setImageResource(ResUtil.getDrawableID("juns_safe", mContext));
//                }
//            }
//        });

        resetCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNum = resetPhoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNum)) {
                    ViewUtils.sdkShowTips(mContext, "请输入您的账号！");
                    return;
                }
                resetPwdGetCode(phoneNum);
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = resetPhoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    ViewUtils.sdkShowTips(mContext, "请输入您的账号！");
                    return;
                }
                String safeCode = resetCodeEt.getText().toString().trim();
                if (TextUtils.isEmpty(safeCode)) {
                    ViewUtils.sdkShowTips(mContext, "请输入短信验证码！");
                    return;
                }
                resetPwdVerify(phone, safeCode);
            }
        });

    }

    private void resetPwdGetCode(String phone) {
        PhoneResetCodeParam resetCodeParam = new PhoneResetCodeParam(phone);
        x.http().post(resetCodeParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse result) {
                ViewUtils.sdkShowTips(mContext, result.msg);
                resetCountDownTimer.start();
                resetCodeBtn.setBackgroundColor(Color.TRANSPARENT);
                resetCodeBtn.setClickable(false);
                resetCodeBtn.setTextColor(Color.parseColor("#333333"));
                resetCodeBtn.setTextSize(9);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex instanceof JunSServerException) {
                    ViewUtils.sdkShowTips(mContext, ((JunSServerException) ex).getServerMsg());
                } else {
                    ViewUtils.sdkShowTips(mContext, "网络异常，发送失败，请重试！");
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

    private void resetPwdVerify(String phone, String code) {
        PhoneResetVerifyParam phoneResetVerifyParam = new PhoneResetVerifyParam(phone, code);
        x.http().post(phoneResetVerifyParam, new Callback.CommonCallback<JunSResponse>() {

            @Override
            public void onSuccess(JunSResponse result) {
                try {
                    JSONObject dataJson = new JSONObject(result.data);
                    String newPwd = dataJson.getString("upwd");
                    UserUtils.updateSDKUserPwd(SDKData.getSdkUserName(), SDKData.getSdkUserPhone(), newPwd);
                    ViewUtils.sdkShowTips(mContext, "密码重置成功，新密码已通过短信下发！");
                    initMainView();
                } catch (JSONException e) {
                    e.printStackTrace();
                    ViewUtils.sdkShowTips(mContext, "发生异常，重置失败，请重试！");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex instanceof JunSServerException) {
                    ViewUtils.sdkShowTips(mContext, ((JunSServerException) ex).getServerMsg());
                } else {
                    ViewUtils.sdkShowTips(mContext, "网络异常，发送失败，请重试！");
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

    private void initBindView() {
        backRl.setVisibility(View.VISIBLE);
        backRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMainView();
            }
        });
        titleTv.setText(ResUtil.getStringID("juns_bind_phone", mActivity));
        bindContentRl.setVisibility(View.VISIBLE);
        resetContentRl.setVisibility(View.GONE);
        mainContentLl.setVisibility(View.GONE);
        realnameContentRl.setVisibility(View.GONE);


        bindPhoneEt.setRawInputType(Configuration.KEYBOARD_QWERTY);
        bindPhoneEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        bindPhoneEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    bindPhoneImg.setImageResource(ResUtil.getDrawableID("juns_phone_select", mContext));
//                } else {
//                    bindPhoneImg.setImageResource(ResUtil.getDrawableID("juns_phone", mContext));
//                }
//            }
//        });

        bindCodeEt.setRawInputType(Configuration.KEYBOARD_QWERTY);
//        bindCodeImg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    bindCodeImg.setImageResource(ResUtil.getDrawableID("juns_safe_select", mContext));
//                } else {
//                    bindCodeImg.setImageResource(ResUtil.getDrawableID("juns_safe", mContext));
//                }
//            }
//        });

        bindCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNum = bindPhoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNum)) {
                    ViewUtils.sdkShowTips(mContext, "请输入您的手机号码！");
                    return;
                }
                bindPhoneGetCode(phoneNum);
            }
        });

        bindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = bindPhoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    ViewUtils.sdkShowTips(mContext, "请输入您的手机号码！");
                    return;
                }
                String safeCode = bindCodeEt.getText().toString().trim();
                if (TextUtils.isEmpty(safeCode)) {
                    ViewUtils.sdkShowTips(mContext, "请输入短信验证码！");
                    return;
                }
                bindPhoneVerify(phone, safeCode);
            }
        });

    }

    private void bindPhoneGetCode(String phone) {
        PhoneBindCodeParam phoneBindCodeParam = new PhoneBindCodeParam(phone);
        x.http().post(phoneBindCodeParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse result) {
                ViewUtils.sdkShowTips(mContext, result.msg);
                bindCountDownTimer.start();
                bindCodeBtn.setBackgroundColor(Color.TRANSPARENT);
                bindCodeBtn.setClickable(false);
                bindCodeBtn.setTextColor(Color.parseColor("#333333"));
                bindCodeBtn.setTextSize(9);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex instanceof JunSServerException) {
                    ViewUtils.sdkShowTips(mContext, ((JunSServerException) ex).getServerMsg());
                } else {
                    ViewUtils.sdkShowTips(mContext, "网络异常，发送失败，请重试！");
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

    private void bindPhoneVerify(final String phone, String code) {
        PhoneBindVerifyParam phoneBindVerifyParam = new PhoneBindVerifyParam(phone, code);
        x.http().post(phoneBindVerifyParam, new Callback.CommonCallback<JunSResponse>() {

            @Override
            public void onSuccess(JunSResponse result) {
                ViewUtils.sdkShowTips(mContext, "绑定成功！");
                mUserPhone = phoneNoHide(phone);
                initMainView();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex instanceof JunSServerException) {
                    ViewUtils.sdkShowTips(mContext, ((JunSServerException) ex).getServerMsg());
                } else {
                    ViewUtils.sdkShowTips(mContext, "网络异常，发送失败，请重试！");
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

    private void initRealnameView() {
        backRl.setVisibility(View.VISIBLE);
        backRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMainView();
            }
        });
        titleTv.setText(ResUtil.getStringID("juns_certification", mActivity));
        realnameContentRl.setVisibility(View.VISIBLE);
        resetContentRl.setVisibility(View.GONE);
        mainContentLl.setVisibility(View.GONE);
        bindContentRl.setVisibility(View.GONE);

        realnameIdEt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        realnameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //认证
                String name = realnameNameEt.getText().toString().trim();
                String idCard = realnameIdEt.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ViewUtils.sdkShowTips(mContext, "请输入姓名！");
                    return;
                }
                if (TextUtils.isEmpty(idCard)) {
                    ViewUtils.sdkShowTips(mContext, "请输入身份证号码！");
                    return;
                }
                doRealnameVerify(name, idCard);
            }
        });
    }

    private void doRealnameVerify(final String name, String idCard) {
        UserRealNameParam realNameParam = new UserRealNameParam(name, idCard);
        x.http().post(realNameParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse result) {
                SDKData.setUserRealName(name);
                SDKData.setSdkUserIsVerify(true);
                ViewUtils.sdkShowTips(mContext, "您已经认证成功！");
                String formatName = name.substring(0, 1) + "**";
                mUserRealName = formatName;
                initMainView();
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


}
