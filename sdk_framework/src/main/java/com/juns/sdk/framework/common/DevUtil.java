package com.juns.sdk.framework.common;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.juns.sdk.framework.utils.DeviceUtils;
import com.juns.sdk.framework.utils.EncryptUtils;
import com.juns.sdk.framework.utils.PhoneUtils;
import com.juns.sdk.framework.utils.ScreenUtils;

import java.util.UUID;

/**
 * Data：12/12/2018-3:32 PM
 * Author: ranger
 */
public class DevUtil {

    private static final String SALT = "goodman";

    /**
     * 获得设备硬件标识
     *
     * @param context 上下文
     * @return 设备硬件标识
     */
    public static String getDeviceId(Context context) {
        StringBuilder sbDeviceId = new StringBuilder();

        //获得设备默认IMEI（>=6.0 需要READ_PHONE_STATE权限）
        String imei = null;
        //获取设备MAC（>=6.0 需要ACCESS_WIFI_STATE权限）
        String mac = null;
        boolean isIMEIValid = false;
        boolean isMacValid = false;
        try {
            imei = PhoneUtils.getIMEI();
            if (!TextUtils.isEmpty(imei) && !imei.equals("null") && isValidIMEI(Long.parseLong(imei))) {
                isIMEIValid = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mac = DeviceUtils.getMacAddress();
            if (!TextUtils.isEmpty(mac) && !mac.equals("02:00:00:00:00:00") && !mac.equals("null") && isValidMac(mac)) {
                isMacValid = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isIMEIValid && isMacValid) {
            return createDevId(imei + mac);
        } else if (isIMEIValid) {
            return createDevId(imei);
        } else if (isMacValid) {
            return createDevId(mac);
        }

        //获得硬件uuid（根据硬件相关属性，生成uuid）（无需权限）
//        String uuid = getDeviceUUID().replace("-", "");

        //如果以上硬件标识数据均无法获得，
        //则DeviceId默认使用系统随机数，这样保证DeviceId不为空
        return createDevId(UUID.randomUUID().toString());
    }

    private static String createDevId(String info) {
        StringBuilder devSb = new StringBuilder();
        devSb.append(info);
        devSb.append(SALT);
        return EncryptUtils.encryptMD5ToString(devSb.reverse().toString()).toLowerCase();
    }

    /**
     * 获得设备硬件uuid
     * 使用硬件信息，计算出一个随机数
     *
     * @return 设备硬件uuid
     */
    private static String getDeviceUUID() {
        try {
            StringBuilder devInfoSb = new StringBuilder();
            devInfoSb.append(20181013)
                    .append(Build.BOARD)
                    .append(Build.BRAND)
                    .append(Build.DEVICE)
                    .append(Build.HARDWARE)
                    .append(Build.ID)
                    .append(Build.MODEL)
                    .append(Build.PRODUCT)
                    .append(Build.SERIAL)
                    .append(ScreenUtils.getScreenWidth())
                    .append(ScreenUtils.getScreenHeight());
            return EncryptUtils.encryptMD5ToString(devInfoSb.reverse().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Function for finding and returning
    // sum of digits of a number
    private static int sumDig(int n) {
        int a = 0;
        while (n > 0) {
            a = a + n % 10;
            n = n / 10;
        }
        return a;
    }

    private static boolean isValidIMEI(long n) {
        // Converting the number into String
        // for finding length
        String s = Long.toString(n);
        int len = s.length();

        if (len != 15)
            return false;

        int sum = 0;
        for (int i = len; i >= 1; i--) {
            int d = (int) (n % 10);
            // Doubling every alternate digit
            if (i % 2 == 0)
                d = 2 * d;
            // Finding sum of the digits
            sum += sumDig(d);
            n = n / 10;
        }
        return (sum % 10 == 0);
    }

    private static boolean isValidMac(String macStr) {

        if (macStr == null || macStr.equals("")) {
            return false;
        }
        String macAddressRule = "([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}";
        // 这是真正的MAC地址；正则表达式；
        if (macStr.matches(macAddressRule)) {
            return true;
        } else {
            return false;
        }
    }

}
