package com.apex.UISettings.utils;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import com.apex.UISettings.R;

public class BatteryUtil {

    public static final String TAG = "ApexBatteryUtil";
    private static final int LOW_BATTERY_THRESHOLD = 15; // 低电量阈值(%)
    private static final float LOW_TEMP_THRESHOLD = 0.0f; // 低温阈值(℃)
    private static final float HIGH_TEMP_THRESHOLD = 60.0f; // 高温阈值(℃)
    private static boolean isLowBatteryDialogShown = false;
    private static boolean isLowTempDialogShown = false;
    private static boolean isHighTempDialogShown = false;
    private static boolean isInvalidBatteryDialogShown = false;

    /**
     * 检查电池状态并返回对应的提示信息（若状态正常则返回 null）
     */
    public static BatteryStatusInfo checkBatteryStatus(Context context, Intent batteryIntent) {
        Log.d(TAG,"start checkBatteryStatus");
        if (batteryIntent == null) return null;

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        Log.d(TAG,"temperature: " + temperature);
        int health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
        boolean isCharging = isCharging(batteryIntent);

        float batteryPct = (level * 100f) / scale;
        float tempCelsius = temperature / 10.0f;

        BatteryStatusInfo statusInfo = new BatteryStatusInfo();

        // 低电量检测
        if (batteryPct <= LOW_BATTERY_THRESHOLD && !isCharging && !isLowBatteryDialogShown) {
            statusInfo.title = context.getString(R.string.low_battery);
            statusInfo.message = context.getString(R.string.low_battery_to_shutdown);
            statusInfo.iconId = R.drawable.low_battery;
            isLowBatteryDialogShown = true;
        } else if ((batteryPct > LOW_BATTERY_THRESHOLD || isCharging) && isLowBatteryDialogShown) {
            isLowBatteryDialogShown = false;
        }

        // 低温检测
        if (tempCelsius <= LOW_TEMP_THRESHOLD && !isLowTempDialogShown) {
            Log.i(TAG,context.getString(R.string.battery_low_temperature));
            statusInfo.title = context.getString(R.string.battery_warnings);
            statusInfo.message = context.getString(R.string.battery_low_temperature);
            statusInfo.iconId = R.drawable.low_temp_battery;
            isLowTempDialogShown = true;
        } else if (tempCelsius > LOW_TEMP_THRESHOLD && isLowTempDialogShown) {
            isLowTempDialogShown = false;
        }

        // 高温检测
        if (tempCelsius >= HIGH_TEMP_THRESHOLD && !isHighTempDialogShown) {
            Log.d(TAG,context.getString(R.string.battery_high_temperature));
            statusInfo.title = context.getString(R.string.battery_warnings);
            statusInfo.message = context.getString(R.string.battery_high_temperature);
            statusInfo.iconId = R.drawable.high_temp_battery;
            isHighTempDialogShown = true;
        } else if (tempCelsius < HIGH_TEMP_THRESHOLD && isHighTempDialogShown) {
            isHighTempDialogShown = false;
        }

        // 非法电池检测
        String invalidMsg = getInvalidBatteryMsg(health);
        if (invalidMsg != null && !isInvalidBatteryDialogShown) {
            statusInfo.title = context.getString(R.string.illegal_battery_warnings);
            statusInfo.message = invalidMsg;
            isInvalidBatteryDialogShown = true;
        } else if (invalidMsg == null && isInvalidBatteryDialogShown) {
            isInvalidBatteryDialogShown = false;
        }

        // 如果没有状态触发，返回 null
        return statusInfo.title == null ? null : statusInfo;
    }

    /**
     * 判断是否正在充电
     */
    private static boolean isCharging(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    /**
     * 获取非法电池对应的提示信息
     */
    private static String getInvalidBatteryMsg(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return "电池已损坏，请更换电池";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return "电池过压，存在安全风险";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return "电池过热，可能导致损坏";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return "电池出现未知故障，请检查";
            default:
                return null; // 电池状态正常
        }
    }
    /**
     * 电池状态信息类，用于传递弹窗标题和内容
     */
    public static class BatteryStatusInfo {
        public int iconId;
        public String title;
        public String message;

    }
    public static void startToShutDownDevice(Context mContext){
        //倒计时60s关机
        new Handler(Looper.getMainLooper()).postDelayed(() -> BatteryUtil.shutdownDevice(mContext), 60 * 1000);
    }
    public static void shutdownDevice(Context mContext) {
        try {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            // 反射调用 Hidden API: shutdown(boolean confirm, String reason, boolean wait)
            pm.getClass()
                    .getMethod("shutdown", boolean.class, String.class, boolean.class)
                    .invoke(pm, false, null, true);
        } catch (Exception e) {
            Log.w(TAG, "shutdown failed", e);
        }
    }
}