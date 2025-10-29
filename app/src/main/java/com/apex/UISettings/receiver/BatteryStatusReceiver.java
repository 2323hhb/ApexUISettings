package com.apex.UISettings.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.apex.UISettings.manager.BatteryWindowManager;
import com.apex.UISettings.utils.Logger;

public class BatteryStatusReceiver extends BroadcastReceiver {
    private static final Logger logger = Logger.getLogger("BatteryStatusReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.i("BatteryStatusReceiver received BATTERY_STATUS_CHANGED");

        if ("com.apex.UISettings.BATTERY_STATUS_CHANGED".equals(intent.getAction())) {
            int iconId = intent.getIntExtra("iconId", 0);
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
//            boolean isLowBattery = intent.getBooleanExtra("isLowBattery", false);
            int invalidBatteryState = intent.getIntExtra("invalidBatteryState", 0);
            String batteryCode = intent.getStringExtra("batteryCode");
            BatteryWindowManager.showBatteryDialog(context, iconId, title, message,invalidBatteryState,batteryCode);
        }
    }
}