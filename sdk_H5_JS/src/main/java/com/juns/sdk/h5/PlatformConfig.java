package com.juns.sdk.h5;

import android.content.Context;


import com.juns.sdk.framework.common.TUitls;

import java.io.File;
import java.util.Properties;

public class PlatformConfig {
    private static final String CONFIG_FILE_NAME = "juns" + File.separator + "juns_h5_config.ini";

    private String h5Link;

    private PlatformConfig(Context context){
        Properties properties = TUitls.readAssetsConfig(context, CONFIG_FILE_NAME);
        if (properties != null) {
            h5Link = properties.getProperty("game_link");
        }
    }

    public static PlatformConfig init(Context ctx) {
        return new PlatformConfig(ctx);
    }

    public String getH5Link() {
        return h5Link;
    }

    public void setH5Link(String h5Link) {
        this.h5Link = h5Link;
    }

}
