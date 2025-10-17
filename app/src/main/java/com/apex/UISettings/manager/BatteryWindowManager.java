package com.apex.UISettings.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.apex.UISettings.R;
import com.apex.UISettings.contants.BatteryConstants;
import com.apex.UISettings.utils.BatteryUtil;
import com.apex.UISettings.utils.Logger;

public class BatteryWindowManager {
    private static final Logger logger = Logger.getLogger("BatteryWindowManager");
    private static WindowManager windowManager;
    private static CountDownTimer currentCountdownTimer;
    @SuppressLint("StaticFieldLeak")
    private static View floatingView;

    @SuppressLint("SetTextI18n")
    public static void showBatteryDialog(Context context, int iconId, String title, String message, int invalidBatteryState, String batteryCode) {
        // 清除之前的计时器
        if (currentCountdownTimer != null) {
            currentCountdownTimer.cancel();
        }
        dismissDialog();
        //系统弹窗
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.battery_dialog, null);
        ImageView dialogIcon = floatingView.findViewById(R.id.dialog_icon);
        TextView dialogTitle = floatingView.findViewById(R.id.dialog_title);
        TextView dialogMessage = floatingView.findViewById(R.id.dialog_message);
        View dialogDivider = floatingView.findViewById(R.id.dialog_divider);
        Button dialogButton = floatingView.findViewById(R.id.dialog_button);
        dialogIcon.setImageResource(iconId);
        dialogTitle.setText(title);
        if (!title.equals(context.getString(R.string.low_battery))) {
            if (dialogDivider != null) dialogDivider.setVisibility(View.VISIBLE);
            if (dialogButton != null) dialogButton.setVisibility(View.VISIBLE);

        } else {
            if (dialogDivider != null) dialogDivider.setVisibility(View.GONE);
            if (dialogButton != null) dialogButton.setVisibility(View.GONE);
            dialogMessage.setText(message);
        }
        if (invalidBatteryState == BatteryConstants.InvalidBatteryState.GOOD){
            dialogMessage.setText(message + "\n(Code:"+batteryCode+")");
        }else {
            dialogMessage.setText(getInvalidBatteryMessage(context,invalidBatteryState)+ "\n(Code:"+batteryCode+")");
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8),
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        params.dimAmount = 0.6f;

        if (title.equals(context.getString(R.string.low_battery))) {
            startToCountdown(context, dialogMessage, 60 * 1000, batteryCode);
        }
        if (title.equals(context.getString(R.string.battery_high_temperature_to_shutdown_title))) {
            Log.i("BatteryWindowManager", "startToCountdown");
            startToCountdown(context, dialogMessage, 10 * 1000, batteryCode);
        }
        if (dialogButton != null) {
            dialogButton.setOnClickListener(v -> {
                dismissDialog();
            });
        }
        try {
            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            logger.e("Failed to show system battery dialog: " + e.getMessage());
        }
    }

    private static void startToCountdown(Context context, TextView messageView, int times, String batteryCode) {
        currentCountdownTimer = new CountDownTimer(times, 1000) { // 60秒倒计时，每秒更新
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String formattedText = String.format(context.getString(R.string.low_battery_to_shutdown), secondsLeft);
                CharSequence coloredMessage = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_COMPACT);
                String updatedMessage = coloredMessage + "\n(Code:" + batteryCode + ")";
                if (messageView != null) {
                    messageView.setText(updatedMessage);
                }
            }

            @Override
            public void onFinish() {
                BatteryUtil.shutdownDevice(context);
            }
        };
        currentCountdownTimer.start();
    }

    private static String getInvalidBatteryMessage(Context mContext,int invalidBatteryState) {
        Log.d("BatteryWindowManager", "invalidBatteryState: " + invalidBatteryState);
        switch (invalidBatteryState) {
            case BatteryConstants.InvalidBatteryState.DEAD:
                return mContext.getString(R.string.battery_dead);
            case BatteryConstants.InvalidBatteryState.UNKNOWN:
                return mContext.getString(R.string.illegal_battery);
            case BatteryConstants.InvalidBatteryState.OVERHEAT:
                return mContext.getString(R.string.battery_high_temperature_to_shutdown);
            case BatteryConstants.InvalidBatteryState.COLD:
                return mContext.getString(R.string.battery_low_temperature_to_shutdown);
            default:
                return "";
        }
    }

    public static void dismissDialog() {
        if (windowManager != null && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception e) {
                logger.e("Failed to dismiss dialog: " + e.getMessage());
            }
            floatingView = null;
        }
    }
}