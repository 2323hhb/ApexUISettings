package com.apex.UISettings.utils;

import android.util.Log;
import java.lang.reflect.Method;

/**
 * SystemProperties 反射工具类
 * 用于访问 Android 隐藏的系统属性 API
 */
public class SystemPropertiesUtil {

    private static final String TAG = "SystemPropertiesUtil";
    private static final String CLASS_NAME = "android.os.SystemProperties";

    private static Class<?> sSystemPropertiesClass;
    private static Method sGetMethod;
    private static Method sGetWithDefaultMethod;
    private static Method sGetIntMethod;
    private static Method sGetLongMethod;
    private static Method sGetBooleanMethod;
    private static Method sSetMethod;

    static {
        try {
            sSystemPropertiesClass = Class.forName(CLASS_NAME);

            // get(String key)
            sGetMethod = sSystemPropertiesClass.getMethod("get", String.class);

            // get(String key, String def)
            sGetWithDefaultMethod = sSystemPropertiesClass.getMethod("get", String.class, String.class);

            // getInt(String key, int def)
            sGetIntMethod = sSystemPropertiesClass.getMethod("getInt", String.class, int.class);

            // getLong(String key, long def)
            sGetLongMethod = sSystemPropertiesClass.getMethod("getLong", String.class, long.class);

            // getBoolean(String key, boolean def)
            sGetBooleanMethod = sSystemPropertiesClass.getMethod("getBoolean", String.class, boolean.class);

            // set(String key, String val)
            sSetMethod = sSystemPropertiesClass.getDeclaredMethod("set", String.class, String.class);

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SystemProperties reflection", e);
        }
    }

    /**
     * 获取系统属性值
     * @param key 属性键
     * @return 属性值，如果不存在返回空字符串
     */
    public static String get(String key) {
        return get(key, "");
    }

    /**
     * 获取系统属性值，带默认值
     * @param key 属性键
     * @param def 默认值
     * @return 属性值，如果不存在返回默认值
     */
    public static String get(String key, String def) {
        try {
            if (sGetWithDefaultMethod != null) {
                return (String) sGetWithDefaultMethod.invoke(null, key, def);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get property: " + key, e);
        }
        return def;
    }

    /**
     * 获取整型系统属性
     * @param key 属性键
     * @param def 默认值
     * @return 属性值
     */
    public static int getInt(String key, int def) {
        try {
            if (sGetIntMethod != null) {
                return (Integer) sGetIntMethod.invoke(null, key, def);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get int property: " + key, e);
        }
        return def;
    }

    /**
     * 获取长整型系统属性
     * @param key 属性键
     * @param def 默认值
     * @return 属性值
     */
    public static long getLong(String key, long def) {
        try {
            if (sGetLongMethod != null) {
                return (Long) sGetLongMethod.invoke(null, key, def);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get long property: " + key, e);
        }
        return def;
    }

    /**
     * 获取布尔型系统属性
     * @param key 属性键
     * @param def 默认值
     * @return 属性值
     */
    public static boolean getBoolean(String key, boolean def) {
        try {
            if (sGetBooleanMethod != null) {
                return (Boolean) sGetBooleanMethod.invoke(null, key, def);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get boolean property: " + key, e);
        }
        return def;
    }

    /**
     * 设置系统属性
     * 注意：需要系统权限 android.permission.WRITE_SETTINGS
     * @param key 属性键
     * @param val 属性值
     */
    public static void set(String key, String val) {
        try {
            if (sSetMethod != null) {
                sSetMethod.invoke(null, key, val);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set property: " + key, e);
        }
    }

    /**
     * 检查 SystemProperties 是否可用
     * @return true 如果可用
     */
    public static boolean isAvailable() {
        return sSystemPropertiesClass != null && sGetWithDefaultMethod != null;
    }
}