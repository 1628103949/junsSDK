package com.juns.sdk.core.sdk.track;

import java.util.HashMap;

/**
 * Data：05/11/2018-4:33 PM
 * Author: ranger
 */
public class Track {

    public static final int TN_ROLE_CREATE = 1;
    public static final int TN_ROLE_ENTER = 2;
    public static final int TN_ROLE_UPGRADE = 3;
    public static final int TN_ROLE_UPDATE = 4;
    public static final int TN_ROLE_QUIT = 5;

    public static void trackRole(HashMap<String, String> roleInfo, int type) {
        return;
//        UploadRoleTask.uploadRole(roleInfo, type);
    }

}
