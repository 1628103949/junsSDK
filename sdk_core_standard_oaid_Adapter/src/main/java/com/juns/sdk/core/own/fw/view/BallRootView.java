package com.juns.sdk.core.own.fw.view;

import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.juns.sdk.core.own.fw.FWUtils;
import com.juns.sdk.core.own.fw.view.listener.BallClickListener;
import com.juns.sdk.core.own.fw.view.listener.BallTouchListener;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.utils.ScreenUtils;
import com.juns.sdk.framework.utils.SizeUtils;

public class BallRootView extends FrameLayout {

    //上下边距
    public static final int MARGIN_EDGE_V = 0;
    public static final int BALL_TO_LEFT = 1;
    public static final int BALL_TO_TOP = 2;
    public static final int BALL_TO_RIGHT = 3;
    public static final int BALL_TO_BOTTOM = 4;
    public static final int BALL_NORMAL = 0;
    private MoveAnimator mMoveAnimator;
    private HideAnimator mHideAnimator;
    private Activity mActivity;
    //记录down/up位置
    private float xUpScreen;
    private float yUpScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private float mOriginalRawX;
    private float mOriginalRawY;
    private float mOriginalX;
    private float mOriginalY;
    private BallTouchListener ballTouchListener;
    private BallClickListener ballClickListener;
    private int mScreenWidth;
    private int mScreenHeight;

    private View redTipsView;

    private int currentStatus = BALL_NORMAL;
    private boolean isHide = false;
    private CountDownTimer countTimer = new CountDownTimer(2000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (mHideAnimator != null) {
                mHideAnimator.start();
            }
        }
    };

    public BallRootView(Activity act) {
        this(act, null);
    }

    public BallRootView(Activity act, AttributeSet attrs) {
        this(act, attrs, 0);
        View contentView = inflate(act, ResUtil.getLayoutID("juns_float_ball_view", act), this);
        redTipsView = contentView.findViewById(ResUtil.getID("red_view", mActivity));
    }

    public BallRootView(Activity act, AttributeSet attrs, int defStyleAttr) {
        super(act, attrs, defStyleAttr);
        this.mActivity = act;
        init();
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int status) {
        currentStatus = status;
        RelativeLayout.LayoutParams redLp = (RelativeLayout.LayoutParams) redTipsView.getLayoutParams();
        redLp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        redLp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (status == BALL_TO_LEFT) {
            redLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            redTipsView.setLayoutParams(redLp);
        } else {
            redLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            redTipsView.setLayoutParams(redLp);
        }

    }

    public void showRedTips(boolean isShow) {
        if (isShow) {
            redTipsView.setVisibility(View.VISIBLE);
        } else {
            redTipsView.setVisibility(View.GONE);
        }
    }

    public void setBallClickListener(BallClickListener clickListener) {
        this.ballClickListener = clickListener;
    }

    public void setBallTouchListener(BallTouchListener touchListener) {
        this.ballTouchListener = touchListener;
    }

    private void init() {
        mMoveAnimator = new MoveAnimator();
        mHideAnimator = new HideAnimator();
        setClickable(true);
        updateSize();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeOriginalTouchParams(event);
                updateSize();
                mMoveAnimator.stop();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xUpScreen = xDownInScreen;
                yUpScreen = yDownInScreen;
                toNormal();
                countTimer.cancel();
                if (ballTouchListener != null) {
                    ballTouchListener.onDown(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                xUpScreen = event.getRawX();
                yUpScreen = event.getRawY();
                updateViewPosition(event);
                if (ballTouchListener != null) {
                    ballTouchListener.onMove(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isOnClickEvent()) {
                    dealClickEvent();
                } else {
                    moveToEdge();
                    if (ballTouchListener != null) {
                        ballTouchListener.onUp(event);
                    }
                }
                break;
        }
        return false;
    }

    private void dealClickEvent() {
        if (!isHide) {
            if (ballClickListener != null) {
                ballClickListener.onClick();
            }
        }
    }

    /**
     * 是否为点击事件
     */
    private boolean isOnClickEvent() {
        int scaledTouchSlop = ViewConfiguration.get(mActivity).getScaledTouchSlop();
        return Math.abs(xDownInScreen - xUpScreen) <= scaledTouchSlop
                && Math.abs(yDownInScreen - yUpScreen) <= scaledTouchSlop;
    }

    private void updateViewPosition(MotionEvent event) {
        // 限制不可超出屏幕宽度
        float desX = mOriginalX + event.getRawX() - mOriginalRawX;
        if (desX <= 0) {
            desX = 0;
        }
        if (desX > mScreenWidth - getWidth()) {
            desX = mScreenWidth - getWidth();
        }
        setX(desX);

        // 限制不可超出屏幕高度
        float desY = mOriginalY + event.getRawY() - mOriginalRawY;
        if (desY <= 0) {
            desY = 0;
        }
        if (desY > mScreenHeight - getHeight() - SizeUtils.dp2px(MARGIN_EDGE_V)) {
            desY = mScreenHeight - getHeight() - SizeUtils.dp2px(MARGIN_EDGE_V);
        }
        setY(desY);
    }

    private void changeOriginalTouchParams(MotionEvent event) {
        mOriginalX = getX();
        mOriginalY = getY();
        mOriginalRawX = event.getRawX();
        mOriginalRawY = event.getRawY();
    }

    private void updateSize() {
        mScreenWidth = FWUtils.getWindowWidth(mActivity);
        mScreenHeight = FWUtils.getWindowHeight(mActivity);
    }

    private void moveToEdge() {
        float viewX = getX();
        float viewY = getY();
        float marginLeft = viewX;
        float marginTop = viewY;
        float marginRight = mScreenWidth - viewX - getWidth();
        float marginBottom = mScreenHeight - viewY - getHeight();

        int x = SizeUtils.dp2px(50);

        int viewMatchWidth = mScreenWidth - getWidth();
        int viewHeightWidth = mScreenHeight - getHeight();

        RelativeLayout.LayoutParams redLp = (RelativeLayout.LayoutParams) redTipsView.getLayoutParams();
        redLp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        redLp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);

        if (ScreenUtils.isPortrait()) {
            //仅左右
            if (marginLeft < marginRight) {
                //靠左
                mMoveAnimator.start(0, viewY);
                redLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                redTipsView.setLayoutParams(redLp);
                currentStatus = BALL_TO_LEFT;
            } else {
                //靠右
                mMoveAnimator.start(viewMatchWidth, viewY);
                redLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                redTipsView.setLayoutParams(redLp);
                currentStatus = BALL_TO_RIGHT;
            }

//
//            }
        } else {
            //仅左右
            if (marginLeft < marginRight) {
                //靠左
                mMoveAnimator.start(0, viewY);
                redLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                redTipsView.setLayoutParams(redLp);
                currentStatus = BALL_TO_LEFT;
            } else {
                //靠右
                mMoveAnimator.start(viewMatchWidth, viewY);
                redLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                redTipsView.setLayoutParams(redLp);
                currentStatus = BALL_TO_RIGHT;
            }


        }
    }

    private void move(float deltaX, float deltaY) {
        setX(getX() + deltaX);
        setY(getY() + deltaY);
    }

    public void toGoingHide() {
        moveToEdge();
//        if (countTimer != null) {
//            countTimer.start();
//        }
    }

    public void toGoingHide2() {
        //moveToEdge();
        if (countTimer != null) {
            countTimer.start();
        }
    }

    public void destroy() {
        if (mMoveAnimator != null) {
            mMoveAnimator.destroy();
            mMoveAnimator = null;
        }
        if (mHideAnimator != null) {
            mHideAnimator.destroy();
            mHideAnimator = null;
        }
        mActivity = null;
        if (countTimer != null) {
            countTimer.cancel();
            countTimer = null;
        }
    }

    private void toHideStatus() {
        int halfHeight = getHeight() / 2;
        int halfWidth = getWidth() / 2;
        switch (currentStatus) {
            case BALL_TO_LEFT:
                setX(-halfWidth+FWUtils.getScreenLeft(mActivity));
                setY(getY());
                break;
            case BALL_TO_RIGHT:
//                setX(halfWidth-FWUtils.getScreenRight(mActivity));
//                setY(getY());
                move(halfWidth-FWUtils.getScreenRight(mActivity), 0);
                break;
            case BALL_TO_TOP:
                move(0, -halfHeight);
                break;
            case BALL_TO_BOTTOM:
                move(0, halfHeight);
                break;
            default:
                //默认左边
                setX(-halfWidth+FWUtils.getScreenLeft(mActivity));
                setY(getY());
                break;
        }
        setAlpha(0.5f);
    }

    private void toNormal() {
        if (!isHide) {
            return;
        }
        int halfHeight = getHeight() / 2;
        int halfWidth = getWidth() / 2;
        switch (currentStatus) {
            case BALL_TO_LEFT:
                move(halfWidth, 0);
                break;
            case BALL_TO_RIGHT:
                move(-halfWidth, 0);
                break;
            case BALL_TO_TOP:
                move(0, halfHeight);
                break;
            case BALL_TO_BOTTOM:
                move(0, -halfHeight);
                break;
            default:
                break;
        }
        setAlpha(1f);
        isHide = false;
    }

    private class MoveAnimator implements Runnable {
        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() == null || getRootView().getParent() == null) {
                return;
            }
            float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 1000f);
            float deltaX = (destinationX - getX()) * progress;
            float deltaY = (destinationY - getY()) * progress;
            move(deltaX, deltaY);
            if (progress < 1) {
                handler.post(this);
            } else {
                mHideAnimator.start();
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }

        private void destroy() {
            if (handler != null) {
                handler.removeCallbacks(this);
                handler = null;
            }

//            //左，上，下
//            if (marginLeft < marginTop) {
//                if (marginLeft < marginBottom) {
//                    //靠左
//                    mMoveAnimator.start(0, viewY);
//                    redLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                    redTipsView.setLayoutParams(redLp);
//                    currentStatus = BALL_TO_LEFT;
//                } else {
//                    //靠下
//                    mMoveAnimator.start(viewX, viewHeightWidth);
//                    redLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                    redTipsView.setLayoutParams(redLp);
//                    currentStatus = BALL_TO_BOTTOM;
//                }
//            } else {
//                if (marginTop < marginBottom) {
//                    //靠上
//                    mMoveAnimator.start(viewX, 0);
//                    redLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                    redTipsView.setLayoutParams(redLp);
//                    currentStatus = BALL_TO_TOP;
//                } else {
//                    //靠下
//                    mMoveAnimator.start(viewX, viewHeightWidth);
//                    redLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                    redTipsView.setLayoutParams(redLp);
//                    currentStatus = BALL_TO_BOTTOM;
//                }
//            }
        }
    }

    private class HideAnimator implements Runnable {
        private Handler handler = new Handler(Looper.getMainLooper());

        void start() {
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() == null || getRootView().getParent() == null) {
                return;
            }
            if (isHide) {
                return;
            }
            isHide = true;

            toHideStatus();
        }

        private void destroy() {
            if (handler != null) {
                handler.removeCallbacks(this);
                handler = null;
            }

        }
    }

}
