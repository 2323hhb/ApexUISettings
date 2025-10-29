package com.apex.UISettings.utils;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;
import com.apex.UISettings.contants.BatteryConstants;
import com.apex.UISettings.vsr.VSRManager;
import com.apex.UISettings.R;

public class BatteryUtil {

    private static final Logger logger = Logger.getLogger("ApexBatteryUtil");
    private static final int LOW_BATTERY_THRESHOLD = 50; // 低电量阈值(%)
    private static final float LOW_TEMP_THRESHOLD = -20.0f; // 低温阈值(℃)
    private static final float HIGH_TEMP_THRESHOLD = 45.0f; // 高温阈值(℃)
    public static boolean isLowBatteryDialogShown = false;
    private static boolean isLowTempDialogShown = false;
    private static boolean isHighTempDialogShown = false;
    private static boolean isInvalidBatteryDialogShown = false;


    public static class BatteryStatusInfo {
        public int iconId;
        public String title;
        public String message;
        public boolean isLowBattery = false;
        public int invalidBatteryState;
        public String batteryCode;
        public boolean isCharging;
    }
    /**
     * 检查电池状态并返回对应的提示信息（若状态正常则返回 null）
     */
    public static BatteryStatusInfo checkBatteryStatus(Context context, Intent batteryIntent) {
        logger.i("start checkBatteryStatus");
        if (batteryIntent == null) return null;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        Log.d("ApexBatteryUtil","temperature: " + temperature);
        int health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
        boolean isPresent = isPresent(batteryIntent);
        boolean isCharging = isCharging(batteryIntent);
        logger.i("isCharging: " + isCharging);
        String batteryCode = VSRManager.getInstance().getBatteryStateCode();
        Log.d("ApexBatteryUtil","batteryCode: "+ batteryCode);
        float batteryPct = (level * 100f) / scale;
        float tempCelsius = temperature / 10.0f;
        BatteryStatusInfo statusInfo = new BatteryStatusInfo();
        statusInfo.batteryCode = batteryCode;


        // 低电量检测
        if (batteryPct <= LOW_BATTERY_THRESHOLD && !isCharging && !isLowBatteryDialogShown) {
                logger.d("hhb batteryPct: "+ batteryPct);
                statusInfo.title = context.getString(R.string.low_battery);
                statusInfo.message = context.getString(R.string.low_battery_to_shutdown);
                statusInfo.iconId = R.drawable.low_battery;
                statusInfo.isLowBattery = true;
                statusInfo.batteryCode = batteryCode;
                isLowBatteryDialogShown = true;
            }
//        else if ((batteryPct > LOW_BATTERY_THRESHOLD || isCharging) && isLowBatteryDialogShown) {
//                isLowBatteryDialogShown = false;
//            }

        // 低温检测
        if (tempCelsius <= LOW_TEMP_THRESHOLD && !isLowTempDialogShown) {
                Log.d("ApexBatteryUtil","Low temperature: " + temperature);
                logger.i(context.getString(R.string.battery_low_temperature));
                statusInfo.title = context.getString(R.string.battery_warnings);
                statusInfo.message = context.getString(R.string.battery_low_temperature);
                statusInfo.iconId = R.drawable.low_temp_battery;
                statusInfo.batteryCode = batteryCode;
                isLowTempDialogShown = true;
            } else if (tempCelsius > LOW_TEMP_THRESHOLD && isLowTempDialogShown) {
                isLowTempDialogShown = false;
            }


        // 高温检测
        if (tempCelsius >= HIGH_TEMP_THRESHOLD && !isHighTempDialogShown) {
            Log.d("ApexBatteryUtil","High temperature: " + temperature);
                statusInfo.title = context.getString(R.string.battery_high_temperature_title);
            statusInfo.message = context.getString(R.string.battery_high_temperature);
                statusInfo.iconId = R.drawable.high_temp_battery;
                statusInfo.batteryCode = batteryCode;
                isHighTempDialogShown = true;
            } else if (tempCelsius < HIGH_TEMP_THRESHOLD && isHighTempDialogShown) {
                isHighTempDialogShown = false;
            }

        // 非法电池检测
        int invalidMsg = getInvalidBatteryMsg(health);
        Log.d("ApexBatteryUtil","invalidMsg: " + invalidMsg);
        if (invalidMsg != 0 && !isInvalidBatteryDialogShown) {
                statusInfo.invalidBatteryState= invalidMsg;
                statusInfo.batteryCode = batteryCode;
                if (invalidMsg == BatteryConstants.InvalidBatteryState.UNKNOWN){
                    if (isPresent){
                        statusInfo.title = context.getString(R.string.illegal_battery_warnings);
                        statusInfo.message = context.getString(R.string.illegal_battery);
                        statusInfo.iconId = R.drawable.battery_abnormality_warning;
                    }else {
                        statusInfo.title = context.getString(R.string.battery_warnings);
                        statusInfo.message = context.getString(R.string.battery_not_present);
                        statusInfo.iconId = R.drawable.battery_not_detected;
                    }
                }else {
                    switch (invalidMsg){

                        case BatteryConstants.InvalidBatteryState.DEAD:
                            statusInfo.title = context.getString(R.string.battery_warnings);
                            statusInfo.message = context.getString(R.string.battery_dead);
                            statusInfo.iconId = R.drawable.battery_abnormality_warning;
                            break;
                        case BatteryConstants.InvalidBatteryState.OVERHEAT:
                            statusInfo.title = context.getString(R.string.battery_high_temperature_to_shutdown_title);
                            statusInfo.message = context.getString(R.string.battery_high_temperature_to_shutdown);
                            statusInfo.iconId = R.drawable.high_temp_battery;
                            break;
                        case BatteryConstants.InvalidBatteryState.COLD:
                            statusInfo.title = context.getString(R.string.battery_low_temperature_to_shutdown_title);
                            statusInfo.message = context.getString(R.string.battery_low_temperature_to_shutdown);
                            statusInfo.iconId = R.drawable.low_temp_battery;
                            break;
                    }
                }
                isInvalidBatteryDialogShown = true;
        } else if (invalidMsg ==0 && isInvalidBatteryDialogShown) {
                isInvalidBatteryDialogShown = false;
        }
        if (statusInfo.title != null || statusInfo.isCharging){
            return statusInfo;
        }else {
            return null;
        }
    }

    private static boolean isPresent(Intent batteryIntent) {
        return batteryIntent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
    }
    /**
     * 判断是否正在充电
     */
    public static boolean isCharging(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }
    /**
     * 获取非法电池对应的提示信息
     */
    private static int getInvalidBatteryMsg(int health) {
        Log.d("ApexBatteryUtil","health: " + health);
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return BatteryConstants.InvalidBatteryState.DEAD;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                return BatteryConstants.InvalidBatteryState.UNKNOWN;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return BatteryConstants.InvalidBatteryState.OVERHEAT;
            case BatteryManager.BATTERY_HEALTH_COLD:
                return BatteryConstants.InvalidBatteryState.COLD;
            default:
                return 0;
        }
    }
    public static void shutdownDevice(Context mContext) {
        try {
            Log.i("ApexBatteryUtil","shutdownDevice!!!");
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            // 反射调用 Hidden API: shutdown(boolean confirm, String reason, boolean wait)
            pm.getClass()
                    .getMethod("shutdown", boolean.class, String.class, boolean.class)
                    .invoke(pm, false, null, true);
        } catch (Exception e) {
            logger.w("ApexUISettings shutdown failed" + e);
        }
    }
}