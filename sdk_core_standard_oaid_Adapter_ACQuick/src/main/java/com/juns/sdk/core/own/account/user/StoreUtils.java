package com.juns.sdk.core.own.account.user;

import android.text.TextUtils;

import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.safe.JunSEncrypt;
import com.juns.sdk.framework.utils.DeviceUtils;
import com.juns.sdk.framework.utils.FileUtils;
import com.juns.sdk.framework.utils.SDCardUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Data：26/02/2019-4:15 PM
 * Author: ranger
 */
public class StoreUtils {
    private final static String ENCODE_KEY = "qoduzgdlusdnmcblpatv";
    private final static String USER_FILE_NAME = "_User";
    private final static String FILE_DIR = ".juns_game";

    private final static int FORM_NO = 0;
    private final static int FORM_SD = 1;
    private final static int FORM_SP = 2;

    public static void storeUserList(String userListStr) {
        //1.先保存至SDCard，保存成功则return终止
        //2.SDCard保存失败，则保存至SP中
//        if (TextUtils.isEmpty(userListStr)) {
//            return;
//        }
        //加密存储
        String encryptData = JunSEncrypt.encryptInfo(ENCODE_KEY, userListStr);
        int formWhere = SDKData.getUserStoreWh();
        if (formWhere == FORM_NO) {
            boolean isSuccess = false;
            //保存到SDCard
            isSuccess = setSDCardData(getDeviceFileName() + USER_FILE_NAME, encryptData);
            if (!isSuccess) {
                //保存到SP
                SDKData.setUserRecord(encryptData);
                SDKData.setUserStoreWh(FORM_SP);
            } else {
                SDKData.setUserStoreWh(FORM_SD);
            }
        } else if (formWhere == FORM_SP) {
            SDKData.setUserRecord(encryptData);
            SDKData.setUserStoreWh(FORM_SP);
        } else {
            setSDCardData(getDeviceFileName() + USER_FILE_NAME, encryptData);
            SDKData.setUserStoreWh(FORM_SD);
        }
    }

    public static String getUserList() {
        //1.先从SDCard中获取，有则返回
        //2.SDCard中获取不到，则从SP中获取
        //3.SP中也获取不到，则返回null
        String encryptData = null;
        int form = SDKData.getUserStoreWh();
        if (form == FORM_NO) {
            //首次获取
            //从SDCard中获取
            encryptData = getSDCardData(getDeviceFileName() + USER_FILE_NAME);
            if (!TextUtils.isEmpty(encryptData)) {
                SDKData.setUserStoreWh(FORM_SD);
            }
        } else if (form == FORM_SP) {
            encryptData = SDKData.getUserRecord();
            if (!TextUtils.isEmpty(encryptData)) {
                SDKData.setUserStoreWh(FORM_SP);
            }
        } else {
            encryptData = getSDCardData(getDeviceFileName() + USER_FILE_NAME);
            if (!TextUtils.isEmpty(encryptData)) {
                SDKData.setUserStoreWh(FORM_SD);
            }
        }
        String userData = JunSEncrypt.decryptInfo(ENCODE_KEY, encryptData);
        if (TextUtils.isEmpty(userData)) {
            //获取不到或被篡改
            SDKData.setUserStoreWh(FORM_NO);
            return null;
        } else {
            return userData;
        }
    }

    private static String getDeviceFileName() {
        String name = DeviceUtils.getManufacturer();
        if (TextUtils.isEmpty(name)) {
            name = "Device";
        }
        return name;
    }

    private static boolean setSDCardData(String fileName, String data) {
        try {
            if (SDCardUtils.isSDCardEnable()) {
                try {
                    File file = getFile(fileName);
                    FileWriter writer = new FileWriter(file.getAbsolutePath(), false);
                    writer.write(JunSEncrypt.encryptInfo(ENCODE_KEY, data));
                    writer.close();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getSDCardData(String fileName) {
        try {
            if (SDCardUtils.isSDCardEnable()) {
                File file = getFile(fileName);
                String encodeData = "";
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String tempString = null;
                    int line = 1;
                    while ((tempString = reader.readLine()) != null) {
                        encodeData = encodeData + tempString;
                        line++;
                    }
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                if (encodeData.equals("")) {
                    return null;
                } else {
                    return JunSEncrypt.decryptInfo(ENCODE_KEY, encodeData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File getFile(String fileName) {
        if (!TextUtils.isEmpty(fileName) && SDCardUtils.isSDCardEnable()) {
            try {
                String filePath = SDCardUtils.getSDCardPath() + File.separator + FILE_DIR + File.separator + fileName;
                if (FileUtils.createOrExistsFile(filePath)) {
                    return new File(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
