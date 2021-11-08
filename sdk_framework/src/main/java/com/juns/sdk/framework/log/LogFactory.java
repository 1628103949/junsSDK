package com.juns.sdk.framework.log;

/**
 * Description: log factory.
 * Data：26/09/2018-10:08 AM
 * Author: ranger
 */
public class LogFactory {

    public static TNLog getLog(String tag) {
        return new TNLog(tag, true);
    }

    public static TNLog getLog(String tag, boolean logSwitch) {
        return new TNLog(tag, logSwitch);
    }


}
