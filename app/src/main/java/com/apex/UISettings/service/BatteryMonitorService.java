package com.apex.UISettings.service;

import static com.opendroid.devicemanager.ExtDeviceManager.getSupportDeviceList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.Nullable;
import com.apex.UISettings.R;
import com.apex.UISettings.contants.CFDSetConstants;
import com.apex.UISettings.utils.BatteryUtil;
import com.apex.UISettings.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opendroid.base.ExtStringWrapper;

import java.util.Objects;

public class BatteryMonitorService extends Service {

    private BroadcastReceiver mBatteryReceiver;
    private BatteryUtil.BatteryStatusInfo mLastStatusInfo;
    private static final Logger logger = Logger.getLogger("ApexBatteryMonitorService");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boolean shouldStartService = false;
        logger.i("ApexUISettings-service BatteryMonitorService created");
        ExtStringWrapper deviceList = new ExtStringWrapper();
        int result = getSupportDeviceList(deviceList);
        logger.d(String.valueOf(result));
        if (result == 0) {
            String jsonResult = deviceList.value;
            logger.d("Device list: " + jsonResult);
            Gson gson = new Gson();
            JsonArray devices = gson.fromJson(jsonResult, JsonArray.class);
            for (int i = 0; i < devices.size(); i++) {
                JsonObject device = devices.get(i).getAsJsonObject();
                int deviceTypeId = device.get(CFDSetConstants.DevicesType.DEVICE_TYPE_ID).getAsInt();
                if (deviceTypeId == CFDSetConstants.DeviceTypeId.BATTERY) {
                    String batteryState = device.get(CFDSetConstants.DevicesInfo.STATE).getAsString();
                    logger.d("Battery state: " + batteryState);
                    if (batteryState.equals(CFDSetConstants.DeviceState.NORMAL)) {
                        shouldStartService = true;
                        registerBatteryReceiver();
                        break;
                    }
                }
            }
        }
        if (!shouldStartService) {
            logger.i("No battery device found, stopping service");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.i("ApexUISettings-service destroyed");

        if (mBatteryReceiver != null) {
            try {
                unregisterReceiver(mBatteryReceiver);
            } catch (IllegalArgumentException e) {
//                logger.e("Receiver already unregistered: " + e.getMessage());
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void registerBatteryReceiver() {
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("BatteryMonitorService", String.valueOf(intent));
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    BatteryUtil.BatteryStatusInfo statusInfo = BatteryUtil.checkBatteryStatus(context, intent);
                    if (statusInfo != null) {
                        Log.d("BatteryMonitorService", statusInfo.toString());
                        logger.i("Battery status changed - Title: " + statusInfo.title);
                        Intent broadcastIntent = new Intent("com.apex.UISettings.BATTERY_STATUS_CHANGED");
                        broadcastIntent.setPackage("com.apex.UISettings");
                        broadcastIntent.putExtra("iconId", statusInfo.iconId);
                        broadcastIntent.putExtra("title", statusInfo.title);
                        broadcastIntent.putExtra("message", statusInfo.message);
                        Log.d("BatteryMonitorService", "message:" + statusInfo.message);
                        broadcastIntent.putExtra("isLowBattery",
                                Objects.equals(statusInfo.title, getString(R.string.low_battery)));
                        broadcastIntent.putExtra("batteryCode", statusInfo.batteryCode);
                        logger.i("About to send broadcast with permission using sendBroadcastAsUser");
                        try {
                            sendBroadcastAsUser(
                                    broadcastIntent,
                                    android.os.Process.myUserHandle(),
                                    "com.apex.UISettings.permission.BATTERY_STATUS"
                            );
                            logger.i("Protected broadcast sent successfully via sendBroadcastAsUser");
                        } catch (Exception e) {
                            logger.e("registerBatteryReceiver Failed to send broadcast: " + e.getMessage());
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        logger.i("Battery receiver registered successfully");
    }

    private boolean hasStatusChanged(BatteryUtil.BatteryStatusInfo newStatus) {
        if (mLastStatusInfo == null) return true;

        return !Objects.equals(mLastStatusInfo.title, newStatus.title) ||
                !Objects.equals(mLastStatusInfo.message, newStatus.message) ||
                mLastStatusInfo.batteryCode != newStatus.batteryCode;
    }
}