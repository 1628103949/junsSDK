package com.juns.sdk.framework.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.juns.sdk.framework.utils.NetworkUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Data：19/12/2018-3:59 PM
 * Author: ranger
 */
public class TUitls {

    public static Properties readAssetsConfig(Context ctx, String fileName) {
        Properties properties = null;
        try {
            InputStream inputStream = ctx.getResources().getAssets().open(fileName);
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Bitmap getImageFromAssetsFile(Context con, String fileName) {
        Bitmap image = null;
        AssetManager am = con.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * return "WIFI";"4G";"3G";"2G";"NO_NETWORK"; "UNKNOWN";
     */
    public static String getNetWorkTypeName() {
        String netType = "";
        NetworkUtils.NetworkType type = NetworkUtils.getNetworkType();

        switch (type) {
            case NETWORK_WIFI:
                netType = "WIFI";
                break;
            case NETWORK_4G:
                netType = "4G";
                break;
            case NETWORK_3G:
                netType = "3G";
                break;
            case NETWORK_2G:
                netType = "2G";
                break;
            case NETWORK_UNKNOWN:
                netType = "UNKNOWN";
                break;
            case NETWORK_NO:
                netType = "NO_NETWORK";
                break;

            default:
                break;
        }
        return netType;
    }

    /**
     * 获取系统设置时区ID
     *
     * @return 时区ID
     */
    public static String getTimeZoneID() {
        return TimeZone.getDefault().getID();
    }

    /**
     * 获取系统设置语言码,安卓
     *
     * @return 语言码
     */
    public static String getLanguageCode() {
        if (Locale.getDefault().getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
            //中文区分简繁体
            if (Locale.getDefault().toString().equals(Locale.SIMPLIFIED_CHINESE.toString())) {
                //简体
                return "zh-Hans";
            } else {
                return "zh-Hant";
            }
        } else {
            return Locale.getDefault().getLanguage();
        }
    }

}
