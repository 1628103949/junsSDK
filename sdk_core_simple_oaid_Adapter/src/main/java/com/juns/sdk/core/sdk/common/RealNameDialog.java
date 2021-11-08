package com.juns.sdk.core.sdk.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

import org.json.JSONException;
import org.json.JSONObject;

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

        }
        else if(view.getId() == showBtn.getId()){
            JunsNotiDialog.showNoti(SDKCore.getMainAct(),
                    "如果您是未成年人，请在家长监督下填写自己真实的身份证信息，根据国家新闻出版署《关于防止未成年人沉迷网络游戏的通知》和《关于进一步严格管理切实防止未成年人沉迷网络游戏的通知》，对您有以下限制：\n" +
                    "游戏登陆:\n" +
                    "1.将不以任何形式为未成年人提供网络游戏服务。\n" +
                    "游戏充值:\n" +
                    "1.将不以任何形式为未成年人提供游戏充值服务。\n",true,240);
        }
    }

    private void doVerify(final String name, String idCard) {
        UserRealNameParam realNameParam = new UserRealNameParam(name, idCard);
        x.http().post(realNameParam, new com.juns.sdk.framework.xutils.common.Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse result) {
                try {
                    JSONObject dataJson = new JSONObject(result.data);
                    SDKData.setUserRealName(name);
                    if(dataJson.has("isadult")){
                        int isadult = dataJson.getInt("isadult");
                        if (isadult == 0){
                            JunsNotiDialog.showNoti(SDKCore.getMainAct(),"未成年登陆提示：" +
                                    "\n您已被识别为未成年人，根据国家新闻出版署《关于防止未成年人沉迷网络游戏的通知》和《关于进一步严格管理切实防止未成年人沉迷网络游戏的通知》，该游戏将不以任何形式为未成年人提供游戏服务。",false,190);
                            SDKData.setSdkUserIsVerify(true);
                            ViewUtils.sdkShowTips(mContext, "您已经认证成功！");
                            dismiss();
                            //close
                            if (mCallback != null) {
                                mCallback.onSuccess();
                            }
                        }else if(isadult == 1){
                            SDKData.setSdkUserIsVerify(true);
                            ViewUtils.sdkShowTips(mContext, "您已经认证成功！");
                            dismiss();
                            //close
                            if (mCallback != null) {
                                mCallback.onSuccess();
                            }
                        }
                    }else {
                        SDKData.setSdkUserIsVerify(true);
                        ViewUtils.sdkShowTips(mContext, "您已经认证成功！");
                        dismiss();
                        //close
                        if (mCallback != null) {
                            mCallback.onSuccess();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Log.e("guoinfo","doVerify:"+result.toString());
//                SDKData.setSdkUserIsVerify(true);
//                ViewUtils.sdkShowTips(mContext, "您已经认证成功！");
//                dismiss();
//                //close
//                if (mCallback != null) {
//                    mCallback.onSuccess();
//                }
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
