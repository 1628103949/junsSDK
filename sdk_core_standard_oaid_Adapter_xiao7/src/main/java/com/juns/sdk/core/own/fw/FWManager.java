package com.juns.sdk.core.own.fw;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juns.sdk.core.own.fw.view.BallMenuView;
import com.juns.sdk.core.own.fw.view.BallRootView;
import com.juns.sdk.core.own.fw.view.BallView;
import com.juns.sdk.core.own.fw.view.listener.BallClickListener;
import com.juns.sdk.core.own.fw.view.listener.BallTouchListener;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.utils.SizeUtils;
import com.juns.sdk.framework.view.dialog.Attention.ShakeVertical;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.annotation.BusReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：2019/3/28-5:32 PM
 * Author: ranger
 */
public class FWManager implements BallClickListener, BallTouchListener {

    private final int hideContentWidth = 130;
    private final int hideContentHeight = 80;
    private final int hideContentMarginBottom = 45;

    private int currentOrientation = -1;

    private Activity mActivity;
    private FrameLayout mContainer;

    private BallView ballView;
    private FrameLayout.LayoutParams ballLP;

    private View hideView;
    private RelativeLayout hideContentRl;
    private ImageView hideCloseImg;
    private TextView hideCloseTv;
    private FrameLayout.LayoutParams hideLP;

    private BallMenuView ballMenuView;
    private RelativeLayout ballMenuFather;

    private float hideDownX;
    private float hideDownY;

    private HideTipsDialog mHideTipsDialog;

    public FWManager(Activity act) {
        currentOrientation = act.getResources().getConfiguration().orientation;
        this.mActivity = act;
    }

    private void initial() {
        mContainer = getRootContainer();
        initBallView();
        initHideView();
    }

    private void initBallView() {
        ballView = new BallView(mActivity);
        ballLP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        int lastX = SDKData.getFWLastX();
        int lastY = SDKData.getFWLastY();
        int drawWidth = FWUtils.getWindowWidth(mActivity) - SizeUtils.dp2px(45);
        int drawHeight = FWUtils.getWindowHeight(mActivity) - SizeUtils.dp2px(45);
        //只左右
        //默认左侧居中
        if (lastX == -1 && lastY == -1) {
            //没数据，默认靠左居中
            ballLP.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            ballView.setCurrentStatus(BallRootView.BALL_TO_LEFT);
        } else if (lastX <= drawWidth / 2) {
            //靠左
            ballLP.gravity = Gravity.LEFT;
            if (lastY < 0 || lastY > drawHeight) {
                ballLP.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            } else {
                ballLP.topMargin = lastY;
            }
            ballView.setCurrentStatus(BallRootView.BALL_TO_LEFT);
        } else if (lastX >= drawWidth / 2) {
            //靠右
            ballLP.gravity = Gravity.RIGHT;
            if (lastY < 0 || lastY > drawHeight) {
                ballLP.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            } else {
                ballLP.topMargin = lastY;
            }
            ballView.setCurrentStatus(BallRootView.BALL_TO_RIGHT);
        }

        ballView.setBallTouchListener(this);
        ballView.setBallClickListener(this);
        ballView.setLayoutParams(ballLP);
    }

    private void initHideView() {
        hideLP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        hideLP.gravity = Gravity.BOTTOM;
        hideLP.width = FrameLayout.LayoutParams.MATCH_PARENT;
        hideView = LayoutInflater.from(mActivity).inflate(ResUtil.getLayoutID("juns_float_hide_view", mActivity), null);
        hideContentRl = (RelativeLayout) hideView.findViewById(ResUtil.getID("hide_content_rl", mActivity));
        hideCloseImg = (ImageView) hideView.findViewById(ResUtil.getID("hide_img", mActivity));
        hideCloseTv = (TextView) hideView.findViewById(ResUtil.getID("hide_tv", mActivity));
        hideView.setLayoutParams(hideLP);
    }

    private FrameLayout getRootContainer() {
        if (mActivity == null) {
            return null;
        }
        try {
            FrameLayout contentView = (FrameLayout) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
            try {
                if (mContainer != null && ViewCompat.isAttachedToWindow(mContainer)) {
                    contentView.removeView(mContainer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mContainer = null;
            FrameLayout newContainer = new FrameLayout(mActivity);
            newContainer.setBackgroundColor(Color.parseColor("#00FFFFFF"));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            contentView.addView(newContainer, lp);
            return newContainer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @BusReceiver(mode = Bus.EventMode.Main)
    public void handleTNBallEv(JSBallEv tnBallEv) {
        switch (tnBallEv.getAction()) {
            case JSBallEv.TO_SHOW:
                show();
                break;
            case JSBallEv.TO_HIDE:
                hide();
                break;
            default:
                break;
        }
    }

    public void handleOnResume() {
        show();
    }

    public void handleOnPause() {
        hide();
    }


    private void show() {

        hide();
        if (SDKCore.isSdkLogined() && SDKData.getFwSwitchStatus() && SDKData.getFwGameSwitchStatus()) {
            if (mActivity != null) {
                initial();
                mContainer.addView(ballView); 
                dealRed();
                ballView.toGoingHide2();
            }
        }
    }

    private void dealRed() {
        if (!TextUtils.isEmpty(SDKData.getFloatWindowData())) {
            try {
                JSONArray jsonArray = new JSONArray(SDKData.getFloatWindowData());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject itemJson = jsonArray.getJSONObject(i);
                    if (itemJson.has("red")) {
                        boolean isRed = itemJson.getBoolean("red");
                        ballView.showRedTips(isRed);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void hide() {
        if (ballView != null && ViewCompat.isAttachedToWindow(ballView) && mContainer != null) {
            SDKData.setFwLastX((int) ballView.getX());
            SDKData.setFwLastY((int) ballView.getY());
            mContainer.removeView(ballView);
        }
        if (ballView != null) {
            ballView.destroy();
        }
        ballView = null;
        ballLP = null;

        if (hideView != null && ViewCompat.isAttachedToWindow(hideView) && mContainer != null) {
            mContainer.removeView(hideView);
        }
        hideView = null;
        ballLP = null;

        if (ballMenuFather != null && ViewCompat.isAttachedToWindow(ballMenuFather) && mContainer != null) {
            mContainer.removeView(ballMenuFather);
        }
        ballMenuFather = null;
        ballMenuView = null;
    }

    public void destroy() {
        if (ballView != null && ViewCompat.isAttachedToWindow(ballView) && mContainer != null) {
            mContainer.removeView(ballView);
        }
        if (ballView != null) {
            ballView.destroy();
        }
        ballView = null;
        ballLP = null;

        if (hideView != null && ViewCompat.isAttachedToWindow(hideView) && mContainer != null) {
            mContainer.removeView(hideView);
        }
        hideLP = null;
        hideView = null;

        if (mHideTipsDialog != null) {
            if (mHideTipsDialog.isShowing()) {
                mHideTipsDialog.dismiss();

            }
            mHideTipsDialog = null;
        }
        mContainer = null;
        mActivity = null;
    }

    @Override
    public void onClick() {
//        try {
//            Intent intent = new Intent();
////            intent.putExtra(JunSWebActivity.URL, SDKData.getFloatWindowUrl());
//            intent.putExtra(JunSWebActivity.URL_PARAMS, JunSPlatformUtils.getWebParams());
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            intent.setClass(mActivity, JunSWebActivity.class);
//            mActivity.startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        showMenuView();
    }

    private void showMenuView() {
        try {
            if (ballMenuFather != null && ViewCompat.isAttachedToWindow(mContainer)) {
                mContainer.removeView(ballMenuFather);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ballMenuFather = null;
        ballMenuFather = new RelativeLayout(mActivity);
        ballMenuFather.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(ballMenuFather);

        //show ball menu item
        RelativeLayout.LayoutParams menuLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        menuLp.topMargin = (int) (ballView.getY() - SizeUtils.dp2px(2.5F));
        menuLp.topMargin = (int) ballView.getY();
        if (ballView.getCurrentStatus() == BallView.BALL_TO_LEFT) {
            //左边
            menuLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            menuLp.leftMargin = SizeUtils.dp2px(55)+FWUtils.getScreenLeft(mActivity);
        } else {
            //右边
            menuLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            menuLp.rightMargin = SizeUtils.dp2px(55)+FWUtils.getScreenRight(mActivity);
        }
        ballMenuView = new BallMenuView(mActivity).buildItemView();
        ballMenuFather.addView(ballMenuView, menuLp);

        ballMenuFather.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mContainer.removeView(ballMenuFather);
                        ballView.toGoingHide();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }
                return true;
            }
        });
    }

    @Override
    public void onDown(MotionEvent event) {
        hideDownX = event.getRawX();
        hideDownY = event.getRawY();
    }

    @Override
    public void onMove(MotionEvent event) {
        dealHideViewMove(event.getRawX(), event.getRawY());
    }

    @Override
    public void onUp(MotionEvent event) {
        dealHideViewUp(event.getRawX(), event.getRawY());
    }

    private void dealHideViewMove(float screenX, float screenY) {
        if (hideLP == null || hideView == null || mActivity == null || mContainer == null) {
            return;
        }
        boolean toShow = false;
        //计算阀值，确保不是点击事件
        if (Math.abs(hideDownX - screenX) > 15 || Math.abs(hideDownY - screenY) > 15) {
            toShow = true;
        }

        if (toShow) {
            if (ViewCompat.isAttachedToWindow(hideView)) {
                if (isHideActive(screenX, screenY)) {
                    //激活隐藏状态
                    hideCloseImg.setImageResource(ResUtil.getDrawableID("juns_hide_closing", mActivity));
                    hideCloseTv.setTextColor(Color.parseColor("#EC6B62"));
                } else {
                    //不激活隐藏
                    hideCloseImg.setImageResource(ResUtil.getDrawableID("juns_hide_close", mActivity));
                    hideCloseTv.setTextColor(Color.WHITE);
                }
            } else {
                RelativeLayout.LayoutParams contentLP = (RelativeLayout.LayoutParams) hideContentRl.getLayoutParams();
                contentLP.width = SizeUtils.dp2px(hideContentWidth);
                contentLP.height = SizeUtils.dp2px(hideContentHeight);
                contentLP.setMargins(0, 0, 0, SizeUtils.dp2px(hideContentMarginBottom));
                mContainer.addView(hideView);
            }
        }
    }

    private void dealHideViewUp(float screenX, float screenY) {
        if (isHideActive(screenX, screenY)) {
            //显示隐藏视图弹窗
            if (mHideTipsDialog == null) {
                mHideTipsDialog = new HideTipsDialog(mActivity, new HideTipsDialog.HideCallback() {
                    @Override
                    public void onCancel() {
                        //显示悬浮球
                        show();
                    }

                    @Override
                    public void onHide() {
                        //写入配置，重新登录后修改配置
                        SDKData.setFwSwitchStatus(false);
                        SDKData.setFwLastX(-1);
                        SDKData.setFwLastY(-1);
                    }
                })
                        .showAnim(new ShakeVertical())
                        .dismissAnim(new ZoomOutExit())
                        .dimEnabled(true);
            }
            if (mHideTipsDialog.isShowing()) {
                mHideTipsDialog.dismiss();
            }
            mHideTipsDialog.show();
            //隐藏悬浮球
            hide();
        }
        //移除隐藏视图
        if (hideView != null && ViewCompat.isAttachedToWindow(hideView)) {
            mContainer.removeView(hideView);
        }
    }

    private boolean isHideActive(float screenX, float screenY) {
        boolean isActive = false;
        int viewWidth = SizeUtils.dp2px(hideContentWidth);
        int viewHeight = SizeUtils.dp2px(hideContentHeight);
        int viewMarginBottom = SizeUtils.dp2px(hideContentMarginBottom);

        final int[] location = new int[2];
        hideContentRl.getLocationOnScreen(location);
        int viewStartX = location[0];
        int viewStartY = location[1];
        if (viewStartX == 0 || viewStartY == 0) {
            return false;
        }
        int viewEndX = viewStartX + viewWidth;
        int viewEndY = viewStartY + viewHeight;

        if (screenX > viewStartX && screenX < viewEndX && screenY > viewStartY && screenY < viewEndY) {
            isActive = true;
        }
        return isActive;
    }

}
