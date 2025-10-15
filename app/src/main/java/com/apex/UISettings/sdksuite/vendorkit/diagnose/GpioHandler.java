package com.apex.UISettings.sdksuite.vendorkit.diagnose;

import com.apex.UISettings.utils.DeviceUtil;

/**
 * @description
 * @author ch
 * @date 2025/3/10
 * @edit
 */
public class GpioHandler {

    static {
        try {
            if (DeviceUtil.isRkPlatform()) {
                System.loadLibrary("jni_vendorkit");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Declare external methods
    public native int getValue(int gpio);
    public native int setValue(int gpio, int value);
    public native int request(int gpio);
    public native int free(int gpio);

}
