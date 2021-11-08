package com.juns.channel;

import android.content.Context;
import android.util.Log;


import com.bun.miitmdid.core.InfoCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.bun.miitmdid.pojo.IdSupplierImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Data：2020-05-24-20:15
 * Author: ranger
 */
public class MiIdHelper implements IIdentifierListener {
    public static String mOAID, mVAID, mAAID;

    public static final String TAG = "MiIdHelper";
    public static final int HELPER_VERSION_CODE = 20210301; // DemoHelper版本号

    private boolean isCertInit = false;
    private boolean isDeviceSupported = false;

    public boolean isSDKLogOn = true;          // TODO （1）设置 是否开启sdk日志
    private boolean isSupport;

    public MiIdHelper() {
        System.loadLibrary("nllvm1623827671");  // 加固版本在调用前必须载入SDK安全库
        // DemoHelper版本需与SDK版本一致
        if(MdidSdkHelper.SDK_VERSION_CODE != HELPER_VERSION_CODE){
            Log.e(TAG,"SDK version incorrect.");
            throw new RuntimeException("SDK version incorrect");
        }
    }

    public int startGetOaid(Context ctx) {
        long startTime = System.currentTimeMillis();
        int code = CallFromReflect(ctx);
        long endTime = System.currentTimeMillis();
        long time = endTime - startTime;
        return code;
    }
    /**
     * 获取OAID
     * @param cxt
     */
    public void getDeviceIds(Context cxt){

        // 计时统计(可删去)
        long timeBegin=System.currentTimeMillis();

        // 初始化SDK证书
        if(!isCertInit){ // 证书只需初始化一次
            // 证书为PEM文件中的所有文本内容（包括首尾行、换行符）
            isCertInit = MdidSdkHelper.InitCert(cxt, loadPemFromAssetFile(cxt, cxt.getPackageName() + ".cert.pem"));
            if(!isCertInit){
                Log.w(TAG, "getDeviceIds: cert init failed");
            }
        }

        // 调用SDK获取ID
        int code = MdidSdkHelper.InitSdk(cxt, isSDKLogOn, this);

        // 计时统计(可删去)
        long timeOffset = System.currentTimeMillis() - timeBegin;
        Log.d(TAG,"call sdk time used(ms): " + timeOffset);

        // 根据SDK返回的code进行不同处理
        IdSupplierImpl unsupportedIdSupplier = new IdSupplierImpl();
        if(code == InfoCode.INIT_ERROR_CERT_ERROR){                         // 证书未初始化或证书无效，SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"cert not init or check not pass");
            onSupport(unsupportedIdSupplier);
        }else if(code == InfoCode.INIT_ERROR_DEVICE_NOSUPPORT){             // 不支持的设备, SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"device not supported");
            onSupport(unsupportedIdSupplier);
        }else if( code == InfoCode.INIT_ERROR_LOAD_CONFIGFILE){            // 加载配置文件出错, SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"failed to load config file");
            onSupport(unsupportedIdSupplier);
        }else if(code == InfoCode.INIT_ERROR_MANUFACTURER_NOSUPPORT){      // 不支持的设备厂商, SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"manufacturer not supported");
            onSupport(unsupportedIdSupplier);
        }else if(code == InfoCode.INIT_ERROR_SDK_CALL_ERROR){             // sdk调用出错, SSDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"sdk call error");
            onSupport(unsupportedIdSupplier);
        } else if(code == InfoCode.INIT_INFO_RESULT_DELAY) {             // 获取接口是异步的，SDK内部会回调onSupport
            Log.i(TAG, "result delay (async)");
        }else if(code == InfoCode.INIT_INFO_RESULT_OK){                  // 获取接口是同步的，SDK内部会回调onSupport
            Log.i(TAG, "result ok (sync)");
        }else {
            // sdk版本高于DemoHelper代码版本可能出现的情况，无法确定是否调用onSupport
            // 不影响成功的OAID获取
            Log.w(TAG,"getDeviceIds: unknown code: " + code);
        }
    }


//    public JSONObject getDeviceIds(Context ctx) {
//        long startTime = System.currentTimeMillis();
//        int code = CallFromReflect(ctx);
//        long endTime = System.currentTimeMillis();
//        long time = endTime - startTime;
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("description", descriptionCode(code));
//            jsonObject.put("code", code);
//            jsonObject.put("time", time);
//            jsonObject.put("isSupport", isSupport);
//            jsonObject.put("oaid", oaid);
//            jsonObject.put("vaid", vaid);
//            jsonObject.put("aaid", aaid);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return jsonObject;
//    }

    private int CallFromReflect(Context cxt) {
        return MdidSdkHelper.InitSdk(cxt, true, this);
    }




    @Override
    public void onSupport(IdSupplier idSupplier) {
        if (idSupplier != null) {
           // SDKCore.logger.print("device oaid --> " + idSupplier.getOAID());
            mOAID = idSupplier.getOAID();
            mVAID = idSupplier.getVAID();
            mAAID = idSupplier.getAAID();
//            _supplier.shutDown();
        }
    }
    /**
     * 从asset文件读取证书内容
     * @param context
     * @param assetFileName
     * @return 证书字符串
     */
    public static String loadPemFromAssetFile(Context context, String assetFileName){
        try {
            InputStream is = context.getAssets().open(assetFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null){
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "loadPemFromAssetFile failed");
            return "";
        }
    }

//    private String descriptionCode(int code) {
//        switch (code) {
//            case ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT:
//                return "DEVICE_NOSUPPORT";
//            case ErrorCode.INIT_ERROR_LOAD_CONFIGFILE:
//                return "LOAD_CONFIGFILE";
//            case ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
//                return "MANUFACTURER_NOSUPPORT";
//            case ErrorCode.INIT_ERROR_RESULT_DELAY:
//                return "RESULT_DELAY";
//            case ErrorCode.INIT_HELPER_CALL_ERROR:
//                return "HELPER_CALL_ERROR";
//            default:
//                return "SUCCESS";
//        }
//    }
}

