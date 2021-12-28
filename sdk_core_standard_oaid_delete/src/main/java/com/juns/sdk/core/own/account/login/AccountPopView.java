package com.juns.sdk.core.own.account.login;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.juns.sdk.core.own.account.user.User;
import com.juns.sdk.core.own.account.user.UserUtils;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.ResUtil;

import java.util.LinkedList;

/**
 * Data：01/03/2019-9:49 AM
 * Author: ranger
 */
public class AccountPopView {
    private Context mContext;
    private PopupWindow mPopupWindow;
    private ListView mListView;
    private PopListener mPopListener;
    private int windowWidth;
    private LinkedList<User> userList;
    private MyAdapter myAdapter;

    public AccountPopView(Context ctx, int width, LinkedList<User> users, PopListener popListener) {
        this.mContext = ctx;
        this.userList = users;
        init(width, popListener);
    }

    private void init(int windowWidth, PopListener popListener) {
        this.windowWidth = windowWidth;
        this.mPopListener = popListener;

        //init view
        mListView = new ListView(mContext);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setCacheColorHint(Color.WHITE);
        mListView.setFocusable(false);
        mListView.setDivider(new ColorDrawable(Color.parseColor("#CCCCCC")));
        mListView.setDividerHeight(1);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setBackgroundResource(ResUtil.getDrawableID("juns_pop_bg", mContext));

        //处理item点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mPopListener != null) {
                    mPopListener.onItemClick(userList.get(position));
                }
                //dismiss window
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
            }
        });

        //处理item长按事件
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //其效果等同点击选择
                if (mPopListener != null) {
                    mPopListener.onItemClick(userList.get(position));
                }
                //dismiss window
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
                return true;
            }
        });

        mPopupWindow = new PopupWindow(mListView, windowWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //解决某些机型点击外部弹窗无法消失
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.setFocusable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        //set Data
        myAdapter = new MyAdapter();
        mListView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mPopListener != null) {
                    mPopListener.onDismiss();
                }
            }
        });
    }

    public void showAccountView(View bottomView) {
        if (mPopupWindow != null) {
            if (userList == null || userList.size() == 0 || mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            } else {
                mPopupWindow.showAsDropDown(bottomView);
            }
        }
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        mPopupWindow = null;
        mContext = null;
        mListView = null;
        myAdapter = null;
        mPopListener = null;
        userList = null;

    }

    public interface PopListener {
        void onItemClick(User user);

        void onDismiss();
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(ResUtil.getLayoutID("juns_pop_item_view", mContext), null);
                viewHolder = new ViewHolder();
                viewHolder.nameTv = (TextView) convertView.findViewById(ResUtil.getID("name_tv", mContext));
                viewHolder.delImg = (ImageView) convertView.findViewById(ResUtil.getID("del_img", mContext));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //优先显示手机号
            if (TextUtils.isEmpty(userList.get(position).getUserPhone())) {
                viewHolder.nameTv.setText(userList.get(position).getUserName());
            } else {
                viewHolder.nameTv.setText(userList.get(position).getUserPhone());
            }

            viewHolder.delImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserUtils.deleteAnRecord(userList.get(position));
                    //处理最新登录帐号
                    User lastLoginUser = UserUtils.getLastUser();
                    if (lastLoginUser != null && !userList.isEmpty()) {
                        if (lastLoginUser.getUserId().equals(userList.get(position).getUserId())) {
                            SDKData.setUserLastLogin("");
                        }
                    }
                    userList.remove(position);
                    notifyDataSetChanged();
                    if (userList.isEmpty()) {
                        dismiss();
                    }
                }
            });
            return convertView;
        }

        private class ViewHolder {
            private TextView nameTv;
            private ImageView delImg;
        }
    }

}
