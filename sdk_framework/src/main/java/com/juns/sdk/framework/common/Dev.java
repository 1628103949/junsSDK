package com.juns.sdk.framework.common;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.juns.sdk.framework.safe.JunSEncrypt;
import com.juns.sdk.framework.utils.DeviceUtils;
import com.juns.sdk.framework.utils.EncryptUtils;
import com.juns.sdk.framework.utils.PhoneUtils;

import java.util.UUID;

/**
 * Description: device info helper.
 * Data：28/09/2018-12:21 PM
 * Author: ranger
 */
public class Dev {

    private final static String FILE_DIR = ".dev_juns";
    private final static String SP_NAME = "dev_juns";
    private final static String ENCODE_KEY = "WmLHsy10OYMDba";
    private final static String IMEI_NAME = "_Imei";
    private final static String MAC_NAME = "_Mac";
    private final static String DEV_NAME = "_Dev";

    public static String getDevId(Context ctx) {
        String dev = null;

        dev = getSPString(ctx, DEV_NAME, "");
        if (!TextUtils.isEmpty(dev)) {
            return dev;
        }


//        dev = getLocalData(getDeviceFileName() + DEV_NAME);
//        if (!TextUtils.isEmpty(dev)) {
//            setSPString(ctx, DEV_NAME, dev);
//            return dev;
//        }

        dev = DevUtil.getDeviceId(ctx);
        setSPString(ctx, DEV_NAME, dev);
//        setLocalData(getDeviceFileName() + DEV_NAME, dev);
        return dev;
    }

    public static String getMacAddress(Context ctx) {
        String mac = null;

        mac = getSPString(ctx, MAC_NAME, "");
        if (!TextUtils.isEmpty(mac)) {
            return mac;
        }

//        mac = getLocalData(getDeviceFileName() + MAC_NAME);
//        if (!TextUtils.isEmpty(mac)) {
//            setSPString(ctx, MAC_NAME, mac);
//            return mac;
//        }

        try {
            mac = DeviceUtils.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(mac) && !mac.equals("02:00:00:00:00:00") && !mac.equals("null")) {
            setSPString(ctx, MAC_NAME, mac);
//            setLocalData(getDeviceFileName() + MAC_NAME, mac);
            return mac;
        }

        mac = EncryptUtils.encryptMD5ToString(UUID.randomUUID().toString()).toLowerCase();
        setSPString(ctx, MAC_NAME, mac);
//        setLocalData(getDeviceFileName() + MAC_NAME, mac);
        return mac;
    }

    public static String getPhoneIMEI(Context ctx) {

        String imei = null;
        imei = getSPString(ctx, IMEI_NAME, "");
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }

        try {
            imei = PhoneUtils.getIMEI();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(imei) && !imei.equals("null")) {
            setSPString(ctx, IMEI_NAME, imei);
//            setLocalData(getDeviceFileName() + IMEI_NAME, imei);
            return imei;
        }

        imei = EncryptUtils.encryptMD5ToString(UUID.randomUUID().toString()).toLowerCase();
        setSPString(ctx, IMEI_NAME, imei);
//        setLocalData(getDeviceFileName() + IMEI_NAME, imei);
        return imei;
    }

    private static String getDeviceFileName() {
        String name = DeviceUtils.getManufacturer();
        if (TextUtils.isEmpty(name)) {
            name = "Device";
        }
        return name;
    }

//    private static String getLocalData(String fileName) {
//        try {
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                File file = getFile(fileName);
//                String encodeData = "";
//                BufferedReader reader = null;
//                try {
//                    reader = new BufferedReader(new FileReader(file));
//                    String tempString = null;
//                    int line = 1;
//                    while ((tempString = reader.readLine()) != null) {
//                        encodeData = encodeData + tempString;
//                        line++;
//                    }
//                    reader.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (reader != null) {
//                        try {
//                            reader.close();
//                        } catch (Exception e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                }
//                if (encodeData.equals("")) {
//                    return null;
//                } else {
//                    return JunSEncrypt.decryptInfo(ENCODE_KEY, encodeData);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    private static void setLocalData(String fileName, String data) {
//        try {
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                try {
//                    File file = getFile(fileName);
//                    FileWriter writer = new FileWriter(file.getAbsolutePath(), false);
//                    writer.write(JunSEncrypt.encryptInfo(ENCODE_KEY, data));
//                    writer.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private static File getFile(String fileName) {
//        if (!TextUtils.isEmpty(fileName) && SDCardUtils.isSDCardEnable()) {
//            try {
//                String filePath = SDCardUtils.getSDCardPath() + File.separator + FILE_DIR + File.separator + fileName;
//                if (FileUtils.createOrExistsFile(filePath)) {
//                    return new File(filePath);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

    private static void setSPString(Context ctx, String key, String value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, JunSEncrypt.encryptInfo(ENCODE_KEY, value));
        editor.apply();
    }

    private static String getSPString(Context ctx, String key, String dValue) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return JunSEncrypt.decryptInfo(ENCODE_KEY, sharedPreferences.getString(key, dValue));
    }

    public static TelephonyManager telephonyManager;

    public static String getSimNumber(Context ctx) {
        telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }
}
