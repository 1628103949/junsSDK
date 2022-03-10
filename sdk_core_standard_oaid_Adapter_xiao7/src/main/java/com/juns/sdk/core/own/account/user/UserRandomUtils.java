package com.juns.sdk.core.own.account.user;

import com.juns.sdk.framework.utils.EncryptUtils;

import java.util.Random;
import java.util.UUID;

/**
 * Data：2020-05-16-21:24
 * Author: ranger
 */
public class UserRandomUtils {

    private final static char[] CHAR_SEED = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z'
    };

    private final static char[] NUM_SEED = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    /**
     * 随机生成由0-9a-zA-Z组合而成的字符串
     *
     * @param len 字符串长度
     * @return 生成结果
     */
    private static String randomChar(int len) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(CHAR_SEED[random.nextInt(CHAR_SEED.length)]);
        }
        return sb.toString();
    }

    /**
     * 随机生成由0-9组合而成的字符串
     *
     * @param len 字符串长度
     * @return 生成结果
     */
    private static String randomNum(int len) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(NUM_SEED[random.nextInt(NUM_SEED.length)]);
        }
        return sb.toString();
    }

    public static String randomUserName() {
        String uuidStr = EncryptUtils.encryptMD5ToString(UUID.randomUUID().toString()).toLowerCase();
        char[] uuidArr = uuidStr.toCharArray();
        Random random = new Random();
        StringBuilder randomSb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            randomSb.append(uuidArr[random.nextInt(uuidArr.length)]);
        }
        String currentTime = String.valueOf(System.currentTimeMillis());
        String millisStr = currentTime.substring(currentTime.length() - 4);
        randomSb.append(millisStr);

        StringBuilder nameSb = new StringBuilder();
        nameSb.append("ds");
        char[] randomArr = randomSb.toString().toCharArray();
        for (int i = 0; i < randomArr.length; i++) {
            nameSb.append(randomArr[random.nextInt(randomArr.length)]);
        }
        return nameSb.toString();
    }

    public static String randomPwd() {
        return randomNum(8);
    }

}
