package com.juns.channel;

import android.content.Intent;
import com.jzyx.sdk.open.SplashActivity;

public class GameSplashActivity extends SplashActivity {
    @Override
    public void gotoGame() {
        Intent intent = new Intent();
        intent.setAction("juns_game_action");
        intent.addCategory("juns_game_category");
        startActivity(intent);
        finish();
    }
}
