package com.yuxiang.li.tuil;

/***
 * @ClassName: EasyUtil
 * @Description: 工具类
 * @Auther: liyx
 */
public class EasyUtil {

    public static String uncaptialize(String className) {
        char[] chars = className.toCharArray();
        chars[0] = chars[0] += 32;
        return String.valueOf(chars);
    }
}
