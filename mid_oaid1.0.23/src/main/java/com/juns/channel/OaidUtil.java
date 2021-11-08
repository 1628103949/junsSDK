package com.juns.channel;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.juns.sdk.core.sdk.IOaidAdapter;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class OaidUtil implements IOaidAdapter,IIdentifierListener {
    private static final String TAG = "OaidUtil";
    private static final TNLog logger = LogFactory.getLog(TAG, true);
    static  String oaid = "";
    @Override
    public void init(Context context) {
        logger.print("init: ");
    }

    @Override
    public void start(Activity mainAct) {
        logger.print("start: ");
        getDeviceIds(mainAct);
    }

    @Override
    public String getOaid() {
        logger.print("getOaid: "+oaid);
        return oaid;
    }

    public void getDeviceIds(Context cxt){

        long timeb=System.currentTimeMillis();
        // 方法调用
        int nres = CallFromReflect(cxt);

        long timee=System.currentTimeMillis();
        long offset=timee-timeb;
        logger.print("nres: "+nres);
        if(nres == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT){//不支持的设备

        }else if( nres == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE){//加载配置文件出错

        }else if(nres == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT){//不支持的设备厂商

        }else if(nres == ErrorCode.INIT_ERROR_RESULT_DELAY){//获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程

        }else if(nres == ErrorCode.INIT_HELPER_CALL_ERROR){//反射调用出错

        }
        //Log.d(getClass().getSimpleName(),"return value: "+String.valueOf(nres));

    }

    private int CallFromReflect(Context cxt){
        return MdidSdkHelper.InitSdk(cxt,true,this);
    }

    @Override
    public void OnSupport(boolean isSupport, IdSupplier _supplier) {
        if(_supplier==null) {
            logger.print("_supplier: not supplier");
            return;
        }
        oaid=_supplier.getOAID();
    }
}

