package com.game.chijun;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class UniversalID {

    private static String filePath = File.separator + "UTips" + File.separator + "UUID";

    public static String getUniversalID(Context context) {
        String androidId;
        String uuid = "";
            androidId = "" + Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString();
                } else {
                    uuid = UUID.randomUUID().toString();
                }
            } catch (Exception e) {
                uuid = UUID.randomUUID().toString();
            }

        return uuid.trim();
    }


}