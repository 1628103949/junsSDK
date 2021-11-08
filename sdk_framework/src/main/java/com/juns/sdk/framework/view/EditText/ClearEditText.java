package com.juns.sdk.framework.view.EditText;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.juns.sdk.framework.common.ResUtil;

public class ClearEditText extends EditText {

    private Drawable clearDrawable;
    private Context mContext;

    public ClearEditText(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        clearDrawable = mContext.getResources().getDrawable(ResUtil.getDrawableID("juns_fw_clear_button", mContext));
        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setDrawable();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setDrawable();
            }
        });
        setDrawable();
    }

    private void setDrawable() {
//        if (length() > 0) {
//            setCompoundDrawablesWithIntrinsicBounds(null, null, clearDrawable, null);
//        } else {
//            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (clearDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
//            int viewX = (int) event.getX();
//            int viewY = (int) event.getY();
//            Rect rect = new Rect();
//            getLocalVisibleRect(rect);
//            rect.left = rect.right - clearDrawable.getIntrinsicWidth();
//            if (rect.contains(viewX, viewY))
//                setText("");
//        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}