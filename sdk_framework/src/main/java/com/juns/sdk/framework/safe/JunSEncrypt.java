package com.juns.sdk.framework.safe;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.juns.sdk.framework.utils.EncryptUtils;

/**
 * Data：12/12/2018-3:39 PM
 * Author: ranger
 */
public class JunSEncrypt {

    private static final String TINY_FLAG = "junshanggame.com.com";
    //moc.moc.emaggnahsnuj

    public static String encryptInfo(String key, String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        String result = null;
        try {
            result = AESCrypt.encrypt(key, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String encryptInfo(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        String result = null;
        try {
            result = AESCrypt.encrypt(new StringBuffer(TINY_FLAG).reverse().toString(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String decryptInfo(String key, String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }

        String result = "";
        try {
            result = AESCrypt.decrypt(key, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String decryptInfo(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }

        String result = "";
        try {
            result = AESCrypt.decrypt(new StringBuffer(TINY_FLAG).reverse().toString(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String encryptDInfo(String key, String iv, String dInfo) {
        if (TextUtils.isEmpty(dInfo)) {
            return "";
        }

        try {
            byte[] enKey = EncryptUtils.encryptMD5ToString(key).toLowerCase().substring(0, 24).getBytes("UTF-8");
            byte[] enIv = iv.getBytes("UTF-8");
            byte[] enData = dInfo.getBytes("UTF-8");
            return Base64.encodeToString(TDESCrypt.des3EncodeCBC(enKey, enIv, enData), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decryptDInfo(String key, String iv,  String dInfo) {
        if (TextUtils.isEmpty(dInfo)) {
            return "";
        }
        try {
            byte[] enKey = EncryptUtils.encryptMD5ToString(key).toLowerCase().substring(0, 24).getBytes("UTF-8");
            byte[] enIv = iv.getBytes("UTF-8");
            byte[] enData = Base64.decode(dInfo, Base64.DEFAULT);
            return new String(TDESCrypt.des3DecodeCBC(enKey, enIv, enData), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
