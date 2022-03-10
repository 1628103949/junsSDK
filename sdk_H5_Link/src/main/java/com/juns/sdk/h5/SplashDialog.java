package com.juns.sdk.h5;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.juns.sdk.framework.common.TUitls;


/**
 * Data：28/09/2018-10:49 AM
 * Author: ranger
 */
public class SplashDialog extends Dialog {

    private SplashCallback mSplashCallback;

    private LinearLayout container;
    private ImageView splashImg;
    private Context mContext;

    public SplashDialog(Context ctx, SplashCallback splashCallback) {
        super(ctx);
        mContext = ctx;
        this.mSplashCallback = splashCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().setTheme(android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        container = new LinearLayout(mContext);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mSplashCallback != null) {
                    mSplashCallback.onFinish();
                }
            }
        });
        setContentView(container, new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Bitmap splashBitmap;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            splashBitmap = TUitls.getImageFromAssetsFile(mContext, "juns/juns_splash_port.png");
        } else {
            //横屏
            splashBitmap = TUitls.getImageFromAssetsFile(mContext, "juns/juns_splash_land.png");
        }
        splashImg = new ImageView(mContext);
        splashImg.setImageBitmap(splashBitmap);
        splashImg.setScaleType(ImageView.ScaleType.FIT_XY);
        container.addView(splashImg, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        container.setBackground(new BitmapDrawable(mContext.getResources(), splashBitmap));
    }

    public interface SplashCallback {
        void onFinish();
    }
}
