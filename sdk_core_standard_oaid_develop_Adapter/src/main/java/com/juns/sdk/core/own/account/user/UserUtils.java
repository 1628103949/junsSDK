package com.juns.sdk.core.own.account.user;

import android.text.TextUtils;

import com.juns.sdk.core.sdk.SDKData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Data：27/02/2019-11:14 AM
 * Author: ranger
 */
public class UserUtils {

    public static void addUserRecord(User user) {
        if (user == null || TextUtils.isEmpty(user.getUserName()) || TextUtils.isEmpty(user.getUserPwd())) {
            return;
        }
        //获取UserList
        LinkedList<User> userList = getUserRecord();
        if (userList != null) {
            if (!userList.isEmpty()) {
                //先检查是否存在，并删除
                Iterator<User> iterator = userList.iterator();
                while (iterator.hasNext()) {
                    User userItem = iterator.next();
                    if (!TextUtils.isEmpty(user.getUserId()) &&
                            !TextUtils.isEmpty(userItem.getUserId()) &&
                            user.getUserId().equals(userItem.getUserId())) {
                        iterator.remove();
                    }
                }
            }
            //添加到首位
            userList.addFirst(user);
        }
        //restore user record
        if (userList != null && !userList.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < userList.size(); i++) {
                    JSONObject itemJson = User.userToJsonObject(userList.get(i));
                    if (itemJson != null) {
                        jsonArray.put(itemJson);
                    }
                }
                if (jsonArray.length() > 0) {
                    //存储UserList
                    StoreUtils.storeUserList(jsonArray.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteAnRecord(User user) {
        LinkedList<User> userList = getUserRecord();
        if (userList == null || userList.isEmpty() || user == null || TextUtils.isEmpty(user.getUserId())) {
            return;
        }

        //先检查是否存在，并删除
        Iterator<User> iterator = userList.iterator();
        while (iterator.hasNext()) {
            User userItem = iterator.next();
            if (!TextUtils.isEmpty(user.getUserId()) &&
                    !TextUtils.isEmpty(userItem.getUserId()) &&
                    user.getUserId().equals(userItem.getUserId())) {
                iterator.remove();
            }
        }
        //restore user record
        if (!userList.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < userList.size(); i++) {
                    JSONObject itemJson = User.userToJsonObject(userList.get(i));
                    if (itemJson != null) {
                        jsonArray.put(itemJson);
                    }
                }
                if (jsonArray.length() > 0) {
                    //存储UserList
                    StoreUtils.storeUserList(jsonArray.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            StoreUtils.storeUserList("");
        }
    }

    public static LinkedList<User> getUserRecord() {
        LinkedList<User> userList = new LinkedList<>();
        String usersString = StoreUtils.getUserList();
        if (!TextUtils.isEmpty(usersString)) {
            try {
                JSONArray arrayJson = new JSONArray(usersString);
                for (int i = 0; i < arrayJson.length(); i++) {
                    JSONObject itemJson = arrayJson.getJSONObject(i);
                    if (itemJson != null && !TextUtils.isEmpty(itemJson.toString())) {
                        User itemUser = User.jsonToUser(itemJson.toString());
                        if (itemUser != null) {
                            userList.addLast(itemUser);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userList;
    }

    private static void updateUserRecordPwd(User newUser) {
        LinkedList<User> userList = getUserRecord();
        if (userList != null && !userList.isEmpty()) {
            //先检查是否存在，并更新
            Iterator<User> iterator = userList.iterator();
            while (iterator.hasNext()) {
                User userItem = iterator.next();

                if (!TextUtils.isEmpty(userItem.getUserName()) && !TextUtils.isEmpty(newUser.getUserName())) {
                    if (userItem.getUserName().equals(newUser.getUserName())) {
                        //更新密码
                        userItem.setUserPwd(newUser.getUserPwd());
                    }
                }

                if (!TextUtils.isEmpty(userItem.getUserPhone()) && !TextUtils.isEmpty(newUser.getUserPhone())) {
                    if (userItem.getUserPhone().equals(newUser.getUserPhone())) {
                        //更新密码
                        userItem.setUserPwd(newUser.getUserPwd());
                    }
                }
            }
        }

        //restore user record
        if (userList != null && !userList.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < userList.size(); i++) {
                    JSONObject itemJson = User.userToJsonObject(userList.get(i));
                    if (itemJson != null) {
                        jsonArray.put(itemJson);
                    }
                }
                if (jsonArray.length() > 0) {
                    //存储UserList
                    StoreUtils.storeUserList(jsonArray.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static User getLastUser() {
        return User.jsonToUser(SDKData.getUserLastLogin());
    }

    public static void setLastUser(User user) {
        SDKData.setUserLastLogin(User.userToJson(user));
    }

    private static void updateLastUserPwd(User newUser) {
        //更新最近登录帐号
        User lastUser = getLastUser();
        if (lastUser != null) {
            if (!TextUtils.isEmpty(lastUser.getUserName()) && !TextUtils.isEmpty(newUser.getUserName())) {
                if (lastUser.getUserName().equals(newUser.getUserName())) {
                    //更新密码
                    lastUser.setUserPwd(newUser.getUserPwd());
                    setLastUser(lastUser);
                }
            }

            if (!TextUtils.isEmpty(lastUser.getUserPhone()) && !TextUtils.isEmpty(newUser.getUserPhone())) {
                if (lastUser.getUserPhone().equals(newUser.getUserPhone())) {
                    //更新密码
                    lastUser.setUserPwd(newUser.getUserPwd());
                    setLastUser(lastUser);
                }
            }
        }
    }

    public static void updateSDKUserPwd(String username, String phone, String newPassword) {
        if ((TextUtils.isEmpty(newPassword) || (TextUtils.isEmpty(username)) && TextUtils.isEmpty(phone))) {
            return;
        }
        User newUser = new User();
        newUser.setUserPwd(newPassword);
        newUser.setUserName(username);
        newUser.setUserPhone(phone);
        //更新最近登录帐号
        updateLastUserPwd(newUser);
        //更新登录记录
        updateUserRecordPwd(newUser);
    }

    public static String toOpenUser(User user) {
        if (TextUtils.isEmpty(user.getUserId()) || TextUtils.isEmpty(user.getUserName()) || TextUtils.isEmpty(user.getUserToken())) {
            return "no user data, please contact technical.";
        }
        try {
            JSONObject userJson = new JSONObject();
            userJson.put("userId", user.getUserId());
            userJson.put("userName", user.getUserName());
            userJson.put("token", user.getUserToken());
            return userJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
