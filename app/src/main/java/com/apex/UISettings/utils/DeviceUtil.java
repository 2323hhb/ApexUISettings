package com.apex.UISettings.utils;

/**
 * @description Device Utility class for platform-specific checks
 * @author ch
 * @date 2024/5/16
 * @edit
 */
public class DeviceUtil {
    private static String TAG = "ApexDeviceUtil";

    private DeviceUtil() {
        // private constructor to prevent instantiation
    }

    public static boolean isC20x() {
        String productName = getProductName();
//        Log.d(TAG,"productName: "+productName);
        return productName.contains("C20") || productName.contains("C9Plus");
    }

    public static boolean isRkPlatform() {
        String productName = getProductName();
        return productName.contains("C20SE") ||
                productName.contains("C20Lite") ||
                productName.contains("C20DS");
    }

    public static String getProductName() {
        return SystemPropertiesUtil.get("ro.product.name", "");
    }

    public static String getBuildType() {
        return SystemPropertiesUtil.get("ro.build.type", "");
    }

    public static boolean supportLabelPrint() {
        return getPlatformName().equals(PlatformNames.M20);
    }

    public static String getPlatformName() {
        return SystemPropertiesUtil.get("ro.sys.platform.name", "");
    }

    public static boolean supportUsbModeSwitch() {
        return "AN-LFC-G00".equals(SystemPropertiesUtil.get("ro.sys.platform.name", ""));
    }

    public static boolean isUser() {
        return "user".equals(SystemPropertiesUtil.get("ro.build.type", ""));
    }

    public static boolean isDebug() {
        return "userdebug".equals(SystemPropertiesUtil.get("ro.build.type", ""));
    }

    public static boolean isPortDevice() {
        String platformName = getPlatformName();
        return platformName.equals(PlatformNames.M20) || platformName.equals(PlatformNames.M20SE);
    }

    // PlatformNames class (can be a separate file)
    public static class PlatformNames {
        public static final String M20 = "M20";
        public static final String M20SE = "M20SE";
    }
}
