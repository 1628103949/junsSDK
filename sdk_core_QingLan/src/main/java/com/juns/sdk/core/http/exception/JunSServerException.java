package com.juns.sdk.core.http.exception;

import com.juns.sdk.core.http.JunSResponse;

/**
 * Data：24/12/2018-11:07 AM
 * Author: ranger
 */
public class JunSServerException extends Exception {

    private String serverMsg;
    private String serverData;

    public JunSServerException(JunSResponse tnResponse) {
        super(tnResponse.toString());
        serverMsg = tnResponse.msg;
        serverData = tnResponse.data;
    }

    public String getServerMsg() {
        return serverMsg;
    }

    public String getServerData() {
        return serverData;
    }
}
