package com.juns.sdk.core.api;

import android.content.Context;

import com.juns.sdk.framework.common.TUitls;

import java.io.File;
import java.util.Properties;

/**
 * Description: tiny sdk config.
 * Data：26/09/2018-12:23 PM
 * Author: ranger
 */
public class JunSDebugConfig {
    private static final String CONFIG_FILE_NAME = "juns" + File.separator + "juns_debug.ini";
    private boolean debugSwitch = false;

    private JunSDebugConfig(Context ctx) {
        Properties properties = TUitls.readAssetsConfig(ctx, CONFIG_FILE_NAME);
        if (properties == null) {
            return;
        }
        String switchStr = properties.getProperty("switch");
        if (switchStr.equals("1")) {
            debugSwitch = true;
        }
    }

    public static JunSDebugConfig init(Context ctx) {
        return new JunSDebugConfig(ctx);
    }

    public boolean getDebugSwitch() {
        return debugSwitch;
    }
    
}
