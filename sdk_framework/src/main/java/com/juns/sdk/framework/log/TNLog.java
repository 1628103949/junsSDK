package com.juns.sdk.framework.log;

import android.text.TextUtils;

import com.juns.sdk.framework.logger.Logger;

/**
 * Description: JunS log util.
 * Data：26/09/2018-9:28 AM
 * Author: ranger
 */
public class TNLog {

    private String tag = null;
    private boolean logSwitch = false;

    public TNLog(String tag, boolean logSwitch) {
        this.tag = tag;
        this.logSwitch = logSwitch;
    }

    // 开发日志级别说明，由低至高
    // 1.Verbose: 开发调试过程中一些详细信息，不应该编译进产品中，只在开发阶段使用；
    // 2.Debug: 用于调试的信息，编译进产品，但可以在运行时关闭；
    // 3.Info:例如一些运行时的状态信息，这些状态信息在出现问题的时候能提供帮助；
    // 4.Warn：警告系统出现了异常，即将出现错误；
    // 5.Error：系统已经出现了错误；

    // 某些厂商手机，默认或禁止打印Verbose&Debug级别日志，建议开发时候，使用Info及其以上日志级别

    public void print(String message) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            //运行状态日志使用Info级别
            Logger.t(tag).i(message);
        }
    }

    public void d(String message) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            Logger.t(tag).d(message);
        }
    }

    public void e(String message) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            Logger.t(tag).e(message);
        }
    }

    public void w(String message) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            Logger.t(tag).w(message);
        }
    }

    public void i(String message) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            Logger.t(tag).i(message);
        }
    }

    public void v(String message) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            Logger.t(tag).v(message);
        }
    }

    /**
     * Formats the given json content and print it
     */
    public void json(String json) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            Logger.t(tag).json(json);
        }
    }

    /**
     * Formats the given xml content and print it
     */
    public void xml(String xml) {
        if (logSwitch && !TextUtils.isEmpty(tag)) {
            Logger.t(tag).xml(xml);
        }
    }

}
