package com.juns.sdk.framework.view.dialog;

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.juns.sdk.framework.view.dialog.animation.BaseAnimatorSet;


/**
 * User: Ranger
 * Date: 26/05/2017
 * Time: 3:16 PM
 */
public abstract class BaseDialog<T extends BaseDialog<T>> extends Dialog {
    /**
     * mTag(日志)
     */
    protected String mTag;
    /**
     * mContext(上下文)
     */
    protected Context mContext;
    /**
     * (DisplayMetrics)设备密度
     */
    protected DisplayMetrics mDisplayMetrics;
    /**
     * enable dismiss outside dialog(设置点击对话框以外区域,是否dismiss)
     */
    protected boolean mCancel;
    /**
     * dialog width scale(宽度比例)
     */
    protected float mWidthScale;
    /**
     * dialog height scale(高度比例)
     */
    protected float mHeightScale;
    /**
     * top container(最上层容器)
     */
    protected LinearLayout mLlTop;
    /**
     * container to control dialog height(用于控制对话框高度)
     */
    protected LinearLayout mLlControlHeight;
    /**
     * the child of mLlControlHeight you create.(创建出来的mLlControlHeight的直接子View)
     */
    protected View mOnCreateView;

    /**
     * mShowAnim(对话框显示动画)
     */
    private BaseAnimatorSet mShowAnim;
    /**
     * mDismissAnim(对话框消失动画)
     */
    private BaseAnimatorSet mDismissAnim;
    /**
     * is mShowAnim running(显示动画是否正在执行)
     */
    private boolean mIsShowAnim;
    /**
     * is DismissAnim running(消失动画是否正在执行)
     */
    private boolean mIsDismissAnim;
    /**
     * show Dialog as PopupWindow(像PopupWindow一样展示Dialog)
     */
    private boolean mIsPopupStyle;
    /**
     * automatic dimiss dialog after given delay(在给定时间后,自动消失)
     */
    private boolean mAutoDismiss = false;
    /**
     * delay (milliseconds) to dimiss dialog(对话框消失延时时间,毫秒值)
     */
    private long mAutoDismissDelay = 1500;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean isEditView = false;

    /**
     * method execute order:
     * show:constrouctor---show---oncreate---onStart---onAttachToWindow
     * dismiss:dismiss---onDetachedFromWindow---onStop
     */
    public BaseDialog(Context context) {
        super(context);
        mContext = context;
        setDialogTheme();
        mTag = getClass().getSimpleName();
        setCanceledOnTouchOutside(true);
        Log.d(mTag, "constructor");
    }

    public BaseDialog(Context context, boolean isPopupStyle) {
        this(context);
        mIsPopupStyle = isPopupStyle;
    }

    public BaseDialog(Context context, boolean isPopupStyle, boolean editView) {
        this(context);
        mIsPopupStyle = isPopupStyle;
        this.isEditView = editView;
    }

    /**
     * set dialog theme(设置对话框主题)
     */
    private void setDialogTheme() {
        // android:windowNoTitle
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // android:windowBackground
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //默认透明背景
        getWindow().clearFlags(LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * inflate layout for dialog ui and return (填充对话框所需要的布局并返回)
     * <pre>
     * public View onCreateView() {
     *      View inflate = View.inflate(mContext, R.layout.dialog_share, null);
     *      return inflate;
     * }
     * </pre>
     */
    public abstract View onCreateView();

    public void onViewCreated(View inflate) {
    }

    /**
     * set Ui data or logic opreation before attatched window(在对话框显示之前,设置界面数据或者逻辑)
     */
    public abstract void setUiBeforeShow();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(mTag, "onCreate");
        mDisplayMetrics = mContext.getResources().getDisplayMetrics();
        mLlTop = new LinearLayout(mContext);
        mLlTop.setGravity(Gravity.CENTER);

        mLlControlHeight = new LinearLayout(mContext);
        mLlControlHeight.setGravity(Gravity.CENTER);

        mOnCreateView = onCreateView();
        LinearLayout.LayoutParams createViewLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        createViewLP.gravity = Gravity.CENTER;
        mLlControlHeight.addView(mOnCreateView, createViewLP);

        LinearLayout.LayoutParams heightLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        heightLP.gravity = Gravity.CENTER;
        mLlTop.addView(mLlControlHeight, heightLP);

        onViewCreated(mOnCreateView);

        if (mIsPopupStyle) {
            FrameLayout.LayoutParams contentLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentLP.gravity = Gravity.CENTER;
            setContentView(mLlTop, contentLP);
            //20180502，设置WRAP_CONTENT
            getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//            getWindow().setGravity(Gravity.CENTER);
        } else {
            FrameLayout.LayoutParams contentLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            contentLP.gravity = Gravity.CENTER;
            setContentView(mLlTop, contentLP);
            //20180502，设置MATCH_PARENT
            getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            getWindow().setGravity(Gravity.CENTER);
        }

        mLlTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCancel) {
                    dismiss();
                }
            }
        });

        mOnCreateView.setClickable(true);
    }

    public LinearLayout getmLlTop() {
        return mLlTop;
    }

    /**
     * get actual created view(获取实际创建的View)
     */
    public View getCreateView() {
        return mOnCreateView;
    }

    /**
     * when dailog attached to window,set dialog width and height and show anim
     * (当dailog依附在window上,设置对话框宽高以及显示动画)
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(mTag, "onAttachedToWindow");
        setUiBeforeShow();

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        mLlControlHeight.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        if (mShowAnim != null) {
            mShowAnim.listener(new BaseAnimatorSet.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsShowAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mIsShowAnim = false;
                    delayDismiss();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    mIsShowAnim = false;
                }
            }).playOn(mLlControlHeight);
        } else {
//            BaseAnimatorSet.reset(mLlControlHeight);
//            delayDismiss();
        }
    }


    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        this.mCancel = cancel;
        super.setCanceledOnTouchOutside(cancel);
    }

    @Override
    public void show() {
        Log.d(mTag, "show");
        if (!isEditView) {
            // Dialog 在初始化时会生成新的 Window，先禁止 Dialog Window 获取焦点，等 Dialog 显示后对 Dialog Window 的 DecorView 设置 setSystemUiVisibility ，接着再获取焦点。 这样表面上看起来就没有退出沉浸模式。
            // Set the dialog to not focusable (makes navigation ignore us adding the window)
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }

        //Show the dialog!
        super.show();

        if (!isEditView) {
            //Set the dialog to immersive
            fullScreenImmersive(getWindow().getDecorView());

            //Clear the not focusable flag from the window
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    /**
     * 全屏显示，隐藏虚拟按钮
     */
    private void fullScreenImmersive(View view) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                view.setSystemUiVisibility(((Activity) mContext).getWindow().getDecorView().getSystemUiVisibility());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(mTag, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(mTag, "onStop");
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(mTag, "onDetachedFromWindow");
    }

    @Override
    public void dismiss() {
        Log.d(mTag, "dismiss");
        if (mDismissAnim != null) {
            mDismissAnim.listener(new BaseAnimatorSet.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsDismissAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mIsDismissAnim = false;
                    superDismiss();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    mIsDismissAnim = false;
                    superDismiss();
                }
            }).playOn(mLlControlHeight);
        } else {
            superDismiss();
        }
    }

    /**
     * dismiss without anim(无动画dismiss)
     */
    public void superDismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * dialog anim by styles(动画弹出对话框,style动画资源)
     */
    public void show(int animStyle) {
        Window window = getWindow();
        window.setWindowAnimations(animStyle);
        show();
    }

    /**
     * show at location only valid for mIsPopupStyle true(指定位置显示,只对isPopupStyle为true有效)
     */
    public void showAtLocation(int gravity, int x, int y) {
        if (mIsPopupStyle) {
            Window window = getWindow();
            LayoutParams params = window.getAttributes();
            window.setGravity(gravity);
            params.x = x;
            params.y = y;
        }

        show();
    }

    /**
     * show at location only valid for mIsPopupStyle true(指定位置显示,只对isPopupStyle为true有效)
     */
    public void showAtLocation(int x, int y) {
        int gravity = Gravity.LEFT | Gravity.TOP;//Left Top (坐标原点为右上角)
        showAtLocation(gravity, x, y);
    }

    /**
     * set window dim or not(设置背景是否昏暗)
     */
    public T dimEnabled(boolean isDimEnabled) {
        if (isDimEnabled) {
            getWindow().addFlags(LayoutParams.FLAG_DIM_BEHIND);
        } else {
            getWindow().clearFlags(LayoutParams.FLAG_DIM_BEHIND);
        }
        return (T) this;
    }

    /**
     * set show anim(设置显示的动画)
     */
    public T showAnim(BaseAnimatorSet showAnim) {
        mShowAnim = showAnim;
        return (T) this;
    }

    /**
     * set dismiss anim(设置隐藏的动画)
     */
    public T dismissAnim(BaseAnimatorSet dismissAnim) {
        mDismissAnim = dismissAnim;
        return (T) this;
    }

    /**
     * automatic dimiss dialog after given delay(在给定时间后,自动消失)
     */
    public T autoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
        return (T) this;
    }

    /**
     * set delay (milliseconds) to dismiss dialog(对话框消失延时时间,毫秒值)
     */
    public T autoDismissDelay(long autoDismissDelay) {
        mAutoDismissDelay = autoDismissDelay;
        return (T) this;
    }

    private void delayDismiss() {
        if (mAutoDismiss && mAutoDismissDelay > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, mAutoDismissDelay);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsDismissAnim || mIsShowAnim || mAutoDismiss) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (mIsDismissAnim || mIsShowAnim || mAutoDismiss) {
            return;
        }
        super.onBackPressed();
    }

}
