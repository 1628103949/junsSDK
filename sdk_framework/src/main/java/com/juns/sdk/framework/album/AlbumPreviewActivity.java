package com.juns.sdk.framework.album;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.view.common.ViewUtils;

import java.util.ArrayList;

public class AlbumPreviewActivity extends Activity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    /**
     * 选择的照片文件夹
     */
    public final static String EXTRA_DATA = "extra_data";


    /**
     * 所有被选中的图片
     */
    public final static String EXTRA_ALL_PICK_DATA = "extra_pick_data";
    /**
     * 当前被选中的照片
     */
    public final static String EXTRA_CURRENT_PIC = "extra_current_pic";
    /**
     * 剩余的可选择照片
     */
    public final static String EXTRA_LAST_PIC = "extra_last_pic";
    /**
     * 总的照片
     */
    public final static String EXTRA_TOTAL_PIC = "extra_total_pic";
    static ArrayList<String> pickList;
    static ArrayList<SingleImageModel> allImageList;
    private ViewPager viewPager;
    private TextView tv_choose_pic;
    private ImageView iv_choose_state;
    private Button btn_choose_finish;
    private MyViewPagerAdapter adapter;
    /**
     * 当前选中的图片
     */
    private int currentPic;
    private int lastPic;
    private int totalPics;
    private boolean isFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResUtil.getLayoutID("juns_fw_album_activity_pick_big_images", this));
        initFindView();
        initData();
    }

    protected void initFindView() {
        viewPager = (ViewPager) findViewById(ResUtil.getID("vp_content", this));
        tv_choose_pic = (TextView) findViewById(ResUtil.getID("tv_choose_pic", this));
        iv_choose_state = (ImageView) findViewById(ResUtil.getID("iv_choose_state", this));
        tv_choose_pic.setOnClickListener(this);
        iv_choose_state.setOnClickListener(this);


        findViewById(ResUtil.getID("iv_back", this)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_choose_finish = (Button) findViewById(ResUtil.getID("btn_choose_finish", this));
        btn_choose_finish.setText("完成");
        btn_choose_finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isFinish = true;
                finish();
            }
        });

        if (lastPic < totalPics) {
            btn_choose_finish.setTextColor(getResources().getColor(ResUtil.getColorID("tn_album_color.white", this)));
            btn_choose_finish.setText(String.format(getString(ResUtil.getStringID("tn_fw_album_choose_pic_finish_with_num", this)), totalPics - lastPic, totalPics));
        }
    }

    protected void initData() {
        allImageList = (ArrayList<SingleImageModel>) getIntent().getSerializableExtra(EXTRA_DATA);
        pickList = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_ALL_PICK_DATA);
        if (pickList == null) {
            pickList = new ArrayList<String>();
        }
        currentPic = getIntent().getIntExtra(EXTRA_CURRENT_PIC, 0);

        lastPic = getIntent().getIntExtra(EXTRA_LAST_PIC, 0);
        totalPics = getIntent().getIntExtra(EXTRA_TOTAL_PIC, 9);

        setTitle((currentPic + 1) + "/" + getImagesCount());
        //如果该图片被选中
        if (getChooseStateFromList(currentPic)) {
            iv_choose_state.setBackgroundResource(ResUtil.getDrawableID("juns_fw_album_image_choose", this));
        } else {
            iv_choose_state.setBackgroundResource(ResUtil.getDrawableID("juns_fw_album_image_not_chose", this));
        }

        adapter = new MyViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(currentPic);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //如果该图片被选中
        if (getChooseStateFromList(position)) {
            iv_choose_state.setBackgroundResource(ResUtil.getDrawableID("juns_fw_album_image_choose", this));
        } else {
            iv_choose_state.setBackgroundResource(ResUtil.getDrawableID("juns_fw_album_image_not_chose", this));
        }
        currentPic = position;
        ((TextView) findViewById(ResUtil.getID("tv_title", this))).setText((currentPic + 1) + "/" + getImagesCount());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View view) {
        toggleChooseState(currentPic);
        //如果被选中
        if (getChooseStateFromList(currentPic)) {
            if (lastPic <= 0) {
                toggleChooseState(currentPic);
                ViewUtils.sdkShowTips(AlbumPreviewActivity.this, String.format(getString(ResUtil.getStringID("tn_fw_album_choose_pic_num_out_of_index", AlbumPreviewActivity.this)), totalPics));
                return;
            }
            pickList.add(getPathFromList(currentPic));
            lastPic--;
            iv_choose_state.setBackgroundResource(ResUtil.getDrawableID("juns_fw_album_image_choose", this));
            if (lastPic == totalPics - 1) {
                btn_choose_finish.setTextColor(getResources().getColor(ResUtil.getColorID("tn_fw_album_white", this)));
            }
            btn_choose_finish.setText(String.format(getString(ResUtil.getStringID("tn_fw_album_choose_pic_finish_with_num", this)), totalPics - lastPic, totalPics));
        } else {
            pickList.remove(getPathFromList(currentPic));
            lastPic++;
            iv_choose_state.setBackgroundResource(ResUtil.getDrawableID("juns_fw_album_image_not_chose", this));
            if (lastPic == totalPics) {
                btn_choose_finish.setTextColor(getResources().getColor(ResUtil.getColorID("tn_fw_album_found_description_color", this)));
                btn_choose_finish.setText(getString(ResUtil.getStringID("tn_fw_album_choose_pic_finish", this)));
            } else {
                btn_choose_finish.setText(String.format(getString(ResUtil.getStringID("tn_fw_album_choose_pic_finish_with_num", this)), totalPics - lastPic, totalPics));
            }
        }
    }

    /**
     * 通过位置获取该位置图片的path
     */
    private String getPathFromList(int position) {
        return allImageList.get(position).path;
    }

    /**
     * 通过位置获取该位置图片的选中状态
     */
    private boolean getChooseStateFromList(int position) {
        return allImageList.get(position).isPicked;
    }

    /**
     * 反转图片的选中状态
     */
    private void toggleChooseState(int position) {
        allImageList.get(position).isPicked = !allImageList.get(position).isPicked;
    }

    /**
     * 获得所有的图片数量
     */
    private int getImagesCount() {
        return allImageList.size();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("pick_data", pickList);
        data.putExtra("isFinish", isFinish);
        setResult(RESULT_OK, data);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pickList = null;
        allImageList = null;
    }

    private class MyViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return getImagesCount();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(AlbumPreviewActivity.this).inflate(ResUtil.getLayoutID("juns_fw_album_widget_zoom_iamge", AlbumPreviewActivity.this), null);
            final ZoomImageView zoomImageView = (ZoomImageView) view.findViewById(ResUtil.getID("zoom_image_view", AlbumPreviewActivity.this));

            AlbumBitmapCacheHelper.getInstance().addPathToShowlist(getPathFromList(position));
            zoomImageView.setTag(getPathFromList(position));
            Bitmap bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(AlbumPreviewActivity.this, getPathFromList(position), 0, 0, new AlbumBitmapCacheHelper.ILoadImageCallback() {
                @Override
                public void onLoadImageCallBack(Bitmap bitmap, String path, Object... objects) {
                    ZoomImageView view = ((ZoomImageView) viewPager.findViewWithTag(path));
                    if (view != null && bitmap != null) {
                        ((ZoomImageView) viewPager.findViewWithTag(path)).setSourceImageBitmap(bitmap, AlbumPreviewActivity.this);
                    }
                }
            }, position);
            if (bitmap != null) {
                zoomImageView.setSourceImageBitmap(bitmap, AlbumPreviewActivity.this);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
            AlbumBitmapCacheHelper.getInstance().removePathFromShowlist(getPathFromList(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
