package com.juns.channel;
import android.content.Intent;
import android.graphics.Color;
import com.quicksdk.QuickSdkSplashActivity;
public class SplashActivity extends QuickSdkSplashActivity {
    @Override
    public int getBackgroundColor() {
        return Color.WHITE;
    }

    @Override
    public void onSplashStop() {
        //闪屏结束后，跳转到游戏界面
        Intent intent = new Intent();
        intent.setAction("juns_game_action");
        intent.addCategory("juns_game_category");
        startActivity(intent);
        finish();
    }
}
