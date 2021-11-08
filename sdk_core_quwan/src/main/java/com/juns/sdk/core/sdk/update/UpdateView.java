package com.juns.sdk.core.sdk.update;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.common.TUitls;
import com.juns.sdk.framework.utils.AppUtils;
import com.juns.sdk.framework.view.common.TipsDialog;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BaseDialog;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.common.task.PriorityExecutor;
import com.juns.sdk.framework.xutils.http.RequestParams;
import com.juns.sdk.framework.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * UserManager: Ranger
 * Date: 09/04/2018
 * Time: 2:20 PM
 */
public class UpdateView extends BaseDialog<UpdateView> {

    private final static int MAX_DOWNLOAD_THREAD = 2; // 有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD, true);
    private boolean useCache = true;

    private UpdateViewCallback mUpdateViewCallback;
    private boolean isForce = true;
    private String updateTips;
    private String mDownloadUrl;

    private LinearLayout contentRl;
    private TextView contentTv;
    private RelativeLayout normalRl, loadingRl;
    private Button confirmBtn, cancelBtn;
    private ProgressBar mProgressBar;
    private TextView speedTv, processTv;
    private Callback.Cancelable httpCancelable;

    private String currentApkPath = null;

    public UpdateView(Context ctx, boolean force, @NonNull String downUrl, @NonNull String tips, @NonNull UpdateViewCallback updateViewCallback) {
        super(ctx, false);
        this.mUpdateViewCallback = updateViewCallback;
        this.isForce = force;
        this.updateTips = tips;
        this.mDownloadUrl = downUrl;
    }

    @Override
    public View onCreateView() {
        View containerView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_update_dialog", mContext), null);
        contentRl = (LinearLayout) containerView.findViewById(ResUtil.getID("content_rl", mContext));
        contentTv = (TextView) containerView.findViewById(ResUtil.getID("content_tv", mContext));
        normalRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("normal_rl", mContext));
        loadingRl = (RelativeLayout) containerView.findViewById(ResUtil.getID("loading_rl", mContext));
        confirmBtn = (Button) containerView.findViewById(ResUtil.getID("confirm_btn", mContext));
        cancelBtn = (Button) containerView.findViewById(ResUtil.getID("cancel_btn", mContext));
        mProgressBar = (ProgressBar) containerView.findViewById(ResUtil.getID("loading_pb", mContext));
        speedTv = (TextView) containerView.findViewById(ResUtil.getID("speed_tv", mContext));
        processTv = (TextView) containerView.findViewById(ResUtil.getID("process_tv", mContext));

        setCanceledOnTouchOutside(false);
        if (isForce) {
            setCancelable(false);
        } else {
            setCancelable(true);
        }

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mUpdateViewCallback != null) {
                    mUpdateViewCallback.onFinish();
                    if (httpCancelable != null) {
                        httpCancelable.cancel();
                    }
                }
            }
        });
        return containerView;
    }

    @Override
    public void setUiBeforeShow() {

        normalRl.setVisibility(View.VISIBLE);
        loadingRl.setVisibility(View.GONE);

        if (isForce) {
            cancelBtn.setVisibility(View.GONE);
        } else {
            cancelBtn.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(updateTips)) {
            contentTv.setText(updateTips);
        }

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDownload();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //juns_fw_clear_button
                UpdateView.this.dismiss();
            }
        });
    }

    private void checkDownload() {
        normalRl.setVisibility(View.VISIBLE);
        loadingRl.setVisibility(View.GONE);

        //检查网络类型，wifi状态进行下载，非wifi情况，提示确认弹窗
        String netType = TUitls.getNetWorkTypeName();
        if (!netType.equals("WIFI")) {
            //非WIFI情况，弹出确认下载弹窗
            if (isForce) {
                ViewUtils.showTipsConfirm(mContext, mContext.getString(ResUtil.getStringID("juns_not_wifi_type_download_tips", mContext)), new TipsDialog.TipsConfirmCallback() {
                    @Override
                    public void onConfirm() {
                        //开始下载
                        doDownload(useCache);
                    }
                });
            } else {
                ViewUtils.showTipsDialog(mContext, true, mContext.getString(ResUtil.getStringID("juns_not_wifi_type_download_tips", mContext)), new TipsDialog.TipsCallback() {
                    @Override
                    public void onCancel() {
                        UpdateView.this.dismiss();
                    }

                    @Override
                    public void onConfirm() {
                        //开始下载
                        doDownload(useCache);
                    }
                });
            }
        } else {
            doDownload(useCache);
        }
    }

    private void doDownload(boolean cache) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentApkPath = mContext.getCacheDir() + File.separator + "juns_update_files" + File.separator + TNUpdate.getFileNameFormUrl(mDownloadUrl);
        } else {
            currentApkPath = mContext.getExternalCacheDir() + File.separator + "juns_update_files" + File.separator + TNUpdate.getFileNameFormUrl(mDownloadUrl);
        }
        RequestParams params = new RequestParams(mDownloadUrl);
        //是否断线续传
        params.setAutoResume(cache);
        //设置是否根据头信息自动命名文件
        params.setAutoRename(false);
        params.setSaveFilePath(currentApkPath);
        params.setExecutor(executor);
        params.setCancelFast(true);
        httpCancelable = x.http().get(params, new DownloadCallback());
    }

    public interface UpdateViewCallback {
        void onFinish();
    }

    private class DownloadCallback implements
            Callback.CommonCallback<File>,
            Callback.ProgressCallback<File> {

        @Override
        public void onSuccess(final File result) {
            //更改视图状态
            normalRl.setVisibility(View.VISIBLE);
            loadingRl.setVisibility(View.GONE);
            //下载完成后，对应版本与地址
            SDKData.setUpdateApkPath(result.getAbsolutePath());
            SDKData.setUpdateApkVersion(AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
            //下载完成后，提示安装
            if (isForce) {
                ViewUtils.showTipsConfirm(mContext, mContext.getString(ResUtil.getStringID("juns_update_to_install", mContext)), new TipsDialog.TipsConfirmCallback() {
                    @Override
                    public void onConfirm() {
                        //安装
                        try {
                            InstallUtils.install(mContext, result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                ViewUtils.showTipsDialog(mContext, true, mContext.getString(ResUtil.getStringID("juns_update_to_install", mContext)), new TipsDialog.TipsCallback() {
                    @Override
                    public void onCancel() {
                        UpdateView.this.dismiss();
                    }

                    @Override
                    public void onConfirm() {
                        //安装
                        try {
                            InstallUtils.install(mContext, result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            //下载错误，提示重试
            if (isForce) {
                ViewUtils.showTipsConfirm(mContext, mContext.getString(ResUtil.getStringID("juns_update_to_retry", mContext)), new TipsDialog.TipsConfirmCallback() {
                    @Override
                    public void onConfirm() {
                        useCache = false;
                        checkDownload();
                    }
                });
            } else {
                ViewUtils.showTipsDialog(mContext, true, mContext.getString(ResUtil.getStringID("juns_update_to_retry", mContext)), new TipsDialog.TipsCallback() {
                    @Override
                    public void onCancel() {
                        UpdateView.this.dismiss();
                    }

                    @Override
                    public void onConfirm() {
                        //下载更新包
                        useCache = false;
                        checkDownload();
                    }
                });
            }
        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }

        @Override
        public void onWaiting() {

        }

        @Override
        public void onStarted() {
            //改变视图状态
            normalRl.setVisibility(View.GONE);
            loadingRl.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoading(long total, long current, boolean isDownloading) {
            //更改视图状态
            double process = ((current * 1.0 / total) * 100);
            mProgressBar.setProgress((int) process);
            String currentDownload = Formatter.formatFileSize(mContext, current) + "/" + Formatter.formatFileSize(mContext, total);
            processTv.setText(currentDownload);
        }
    }

}
