package com.game.chijun;

import android.content.Context;
import java.util.UUID;

public class UniversalID {



    public static String getUniversalID(Context context) {
        String uuid = "";
        uuid = UUID.randomUUID().toString();
        return uuid.trim();
    }


}