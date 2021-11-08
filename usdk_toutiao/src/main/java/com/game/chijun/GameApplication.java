package com.game.chijun;

import android.content.Context;

import com.juns.channel.TouTiaoApplication;
import com.qw.soul.permission.SoulPermission;

/**
 * Created by renyugang on 16/8/10.
 */
public class GameApplication extends TouTiaoApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }


    @Override
    public void onCreate() {
        super.onCreate();
        SoulPermission.init(this);
        SoulPermission.skipOldRom(true);
    }
}
