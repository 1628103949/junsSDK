package com.juns.sdk.core.own.account.user;

import android.text.TextUtils;

import org.json.JSONObject;

/**
 * Data：26/02/2019-4:07 PM
 * Author: ranger
 */
public class User {

    private String userName;
    private String userId;
    private String userPwd;
    private String userPhone;
    private String userToken;

    public static JSONObject userToJsonObject(User user) {
        if (user == null || TextUtils.isEmpty(user.getUserId()) || TextUtils.isEmpty(user.getUserName()) || TextUtils.isEmpty(user.getUserPwd())) {
            return null;
        }
        try {
            JSONObject userJson = new JSONObject();
            userJson.put("userName", user.getUserName());
            userJson.put("userId", user.getUserId());
            userJson.put("userPwd", user.getUserPwd());
            if (!TextUtils.isEmpty(user.getUserPhone())) {
                userJson.put("userPhone", user.getUserPhone());
            }
            return userJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String userToJson(User user) {
        if (user == null || TextUtils.isEmpty(user.getUserId()) || TextUtils.isEmpty(user.getUserName()) || TextUtils.isEmpty(user.getUserPwd())) {
            return null;
        }
        try {
            JSONObject userJson = new JSONObject();
            userJson.put("userName", user.getUserName());
            userJson.put("userId", user.getUserId());
            userJson.put("userPwd", user.getUserPwd());
            if (!TextUtils.isEmpty(user.getUserPhone())) {
                userJson.put("userPhone", user.getUserPhone());
            }
            return userJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User jsonToUser(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            JSONObject userJson = new JSONObject(json);
            User mUser = new User();
            mUser.userName = userJson.getString("userName");
            mUser.userId = userJson.getString("userId");
            mUser.userPwd = userJson.getString("userPwd");
            if (userJson.has("userPhone")) {
                mUser.userPhone = userJson.getString("userPhone");
            }
            if (!TextUtils.isEmpty(mUser.getUserId()) && !TextUtils.isEmpty(mUser.getUserPwd()) && !TextUtils.isEmpty(mUser.getUserName())) {
                return mUser;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    @Override
    public String toString() {
        try {
            JSONObject userJson = new JSONObject();
            userJson.put("userName", userName);
            userJson.put("userId", userId);
            if (!TextUtils.isEmpty(userPwd)) {
                userJson.put("userPwd", userPwd);
            }
            if (!TextUtils.isEmpty(userPhone)) {
                userJson.put("userPhone", userPhone);
            }
            if (!TextUtils.isEmpty(userToken)) {
                userJson.put("userToken", userToken);
            }
            return userJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
