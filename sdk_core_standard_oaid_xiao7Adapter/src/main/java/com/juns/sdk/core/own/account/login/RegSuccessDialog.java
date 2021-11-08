package com.juns.sdk.core.own.account.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BaseDialog;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Data：04/03/2019-12:23 PM
 * Author: ranger
 */
public class RegSuccessDialog extends BaseDialog<RegSuccessDialog> {

    private ImageButton enterGameBtn;
    private TextView userNameTv, userPwdTv;
    private SuccessCallback successCallback;
    private String currentUName, currentUPwd;
    private View containerView;

    public RegSuccessDialog(Context ctx, String uName, String uPwd, SuccessCallback callback) {
        super(ctx, false);
        this.currentUName = uName;
        this.currentUPwd = uPwd;
        this.successCallback = callback;
    }

    @Override
    public View onCreateView() {
        containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_reg_success_dialog", mContext), null);
        enterGameBtn = (ImageButton) containerView.findViewById(ResUtil.getID("enter_game_btn", mContext));
        userNameTv = (TextView) containerView.findViewById(ResUtil.getID("user_name_tv", mContext));
        userPwdTv = (TextView) containerView.findViewById(ResUtil.getID("user_pwd_tv", mContext));
        return containerView;
    }

    @Override
    public void setUiBeforeShow() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        enterGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (successCallback != null) {
                    saveView(mContext, getmLlTop(), currentUName + "_帐号截图.jpg");
                    successCallback.onFinish();
                    RegSuccessDialog.this.dismiss();
                }
            }
        });
        userNameTv.setText(currentUName);
        userPwdTv.setText(currentUPwd);
    }

    /**
     * 获取和保存当前View的截图
     */
    private void saveView(Context context, View view, String FileName) {
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        // 有存储才保存
        if (sdcardExist) {
            // 1.获取屏幕
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache(true);

            Bitmap screenShot = Bitmap.createBitmap(view.getDrawingCache());

            view.setDrawingCacheEnabled(false);
            view.destroyDrawingCache();

            // 2.保存Bitmap，默认放到系统相册路径中去
            String savePath = Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator;

            try {
                File path = new File(savePath);
                // 文件
                String filepath = savePath + File.separator + FileName;
                File file = new File(filepath);
                if (!path.exists()) {
                    path.mkdirs();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileOutputStream fos = null;
                fos = new FileOutputStream(file);

                if (null != fos) {
                    screenShot.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                }

                //发送广播更新图库
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                context.sendBroadcast(intent);
                ViewUtils.sdkShowTips(mContext, "帐号信息已截图并保存至手机相册！");

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("数据有误，帐号截图保存失败：" + FileName);
            }
        } else {
            System.out.println("没有存储卡，帐号截图没法保存：" + FileName);
        }

    }

    public interface SuccessCallback {
        void onFinish();
    }

}
