package com.apex.UISettings.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
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
    public static CountDownTimer currentCountdownTimer;
    @SuppressLint("StaticFieldLeak")
    private static View floatingView;
    public static String currentDialogType = null;
    public static final String DIALOG_TYPE_LOW_BATTERY = "low_battery";
    public static final String DIALOG_TYPE_HIGH_TEMP = "high_temp";
    public static final String DIALOG_TYPE_LOW_TEMP = "low_temp";

    // 用于重新弹窗的参数缓存
    private static Context cachedContext;
    private static int cachedIconId;
    private static String cachedTitle;
    private static String cachedMessage;
    private static int cachedInvalidBatteryState;
    private static String cachedBatteryCode;

    @SuppressLint("SetTextI18n")
    public static void showBatteryDialog(Context context, int iconId, String title, String message, int invalidBatteryState, String batteryCode) {
        // 缓存参数用于重新弹窗
        cachedContext = context;
        cachedIconId = iconId;
        cachedTitle = title;
        cachedMessage = message;
        cachedInvalidBatteryState = invalidBatteryState;
        cachedBatteryCode = batteryCode;

        dismissDialogUI();

        //系统弹窗
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.battery_dialog, null);
        ImageView dialogIcon = floatingView.findViewById(R.id.dialog_icon);
        TextView dialogTitle = floatingView.findViewById(R.id.dialog_title);
        TextView dialogMessage = floatingView.findViewById(R.id.dialog_message);
        TextView dialogCode = floatingView.findViewById(R.id.dialog_code);
        View dialogDivider = floatingView.findViewById(R.id.dialog_divider);
        Button dialogButton = floatingView.findViewById(R.id.dialog_button);
        dialogIcon.setImageResource(iconId);
        dialogTitle.setText(title);
        logger.i("dialogTitle: " + title);

        if (!title.equals(context.getString(R.string.low_battery))) {
            if (dialogDivider != null) dialogDivider.setVisibility(View.VISIBLE);
            if (dialogButton != null) dialogButton.setVisibility(View.VISIBLE);
        } else {
            if (dialogDivider != null) dialogDivider.setVisibility(View.VISIBLE);
            if (dialogButton != null) dialogButton.setVisibility(View.VISIBLE);
            dialogMessage.setText(message);
        }

        if (invalidBatteryState == BatteryConstants.InvalidBatteryState.GOOD){
            dialogMessage.setText(message);
        } else {
            message = getInvalidBatteryMessage(context, invalidBatteryState);
            dialogMessage.setText(message);
        }

        dialogCode.setText("(Code:" + batteryCode + ")");
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        params.dimAmount = 0.6f;

        boolean isLowBattery = title.equals(context.getString(R.string.low_battery));
        boolean isHighTemp = title.equals(context.getString(R.string.battery_high_temperature_to_shutdown_title));
        boolean isLowTemp = title.equals(context.getString(R.string.battery_low_temperature_to_shutdown_title));

        if (isLowBattery) {
            currentDialogType = DIALOG_TYPE_LOW_BATTERY;
            logger.i("startToCountdown - low battery");
            startToCountdown(context, dialogMessage, dialogDivider, dialogButton, message, 60 * 1000);
        }

        if (isHighTemp) {
            currentDialogType = DIALOG_TYPE_HIGH_TEMP;
            logger.i("startToCountdown - high temp");
            startToCountdown(context, dialogMessage, dialogDivider, dialogButton, message, 30 * 1000);
        }

        if (isLowTemp) {
            currentDialogType = DIALOG_TYPE_LOW_TEMP;
            logger.i("startToCountdown - low temp");
            startToCountdown(context, dialogMessage, dialogDivider, dialogButton, message, 30 * 1000);
        }
        if (dialogButton != null) {
            dialogButton.setOnClickListener(v -> {
                dismissDialogUI();
            });
        }
        try {
            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            logger.e("Failed to show system battery dialog: " + e.getMessage());
        }
    }

    private static void startToCountdown(Context context, TextView messageView, View divider, Button button, String message, int times) {
        if (currentCountdownTimer != null) {
            currentCountdownTimer.cancel();
        }

        currentCountdownTimer = new CountDownTimer(times, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);

                // 当倒计时到15秒时，重新弹出不可取消的对话框
                if (secondsLeft == 15) {
                    logger.i("Countdown reached 15s, showing non-dismissible dialog");
                    showFinalWarningDialog(context, cachedIconId, cachedTitle, cachedMessage,
                            cachedInvalidBatteryState, cachedBatteryCode);
                    return;
                }

                @SuppressLint({"StringFormatInvalid", "LocalSuppress"})
                String formattedText = String.format(message, secondsLeft);
                Spanned coloredMessage = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY);
                SpannableStringBuilder spannableString = new SpannableStringBuilder(coloredMessage);
                String fullMessage = spannableString.toString();
                String secondsStr = String.valueOf(secondsLeft);
                int start = fullMessage.indexOf(secondsStr);
                if (start != -1) {
                    int end = start + secondsStr.length();
                    spannableString.setSpan(
                            new ForegroundColorSpan(Color.RED),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
                if (messageView != null) {
                    messageView.setText(spannableString);
                    messageView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            @Override
            public void onFinish() {
                // 倒计时结束
                logger.i("Countdown finished");
                // 可以执行关机等操作
                // BatteryUtil.shutdownDevice(context);
            }
        };
        currentCountdownTimer.start();
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private static void showFinalWarningDialog(Context context, int iconId, String title, String message, int invalidBatteryState, String batteryCode) {
        dismissDialogUI();
        //系统弹窗
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.battery_dialog, null);
        ImageView dialogIcon = floatingView.findViewById(R.id.dialog_icon);
        TextView dialogTitle = floatingView.findViewById(R.id.dialog_title);
        TextView dialogMessage = floatingView.findViewById(R.id.dialog_message);
        TextView dialogCode = floatingView.findViewById(R.id.dialog_code);
        View dialogDivider = floatingView.findViewById(R.id.dialog_divider);
        Button dialogButton = floatingView.findViewById(R.id.dialog_button);

        dialogIcon.setImageResource(iconId);
        dialogTitle.setText(title);
        logger.i("Final warning dialogTitle: " + title);

        if (dialogDivider != null) dialogDivider.setVisibility(View.VISIBLE);
        if (dialogButton != null) {
            dialogButton.setVisibility(View.VISIBLE);
            // 设置点击监听器，但不执行任何操作（不可取消）
            dialogButton.setOnClickListener(v -> {
                logger.i("Final warning dialog button clicked, but cannot be dismissed");
            });
        }

        if (invalidBatteryState == BatteryConstants.InvalidBatteryState.GOOD){
            dialogMessage.setText(message);
        } else {
            message = getInvalidBatteryMessage(context, invalidBatteryState);
            dialogMessage.setText(message);
        }
        dialogCode.setText("(Code:" + batteryCode + ")");
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

        // 最后15秒倒计时，不可取消
        startFinalCountdown(context, dialogMessage, message, 15 * 1000);
        try {
            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            logger.e("Failed to show final warning dialog: " + e.getMessage());
        }
    }
    private static void startFinalCountdown(Context context, TextView messageView, String message, int times) {
        if (currentCountdownTimer != null) {
            currentCountdownTimer.cancel();
        }

        currentCountdownTimer = new CountDownTimer(times, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                @SuppressLint({"StringFormatInvalid", "LocalSuppress"})
                String formattedText = String.format(message, secondsLeft);
                Spanned coloredMessage = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY);
                SpannableStringBuilder spannableString = new SpannableStringBuilder(coloredMessage);
                String fullMessage = spannableString.toString();
                String secondsStr = String.valueOf(secondsLeft);
                int start = fullMessage.indexOf(secondsStr);
                if (start != -1) {
                    int end = start + secondsStr.length();
                    spannableString.setSpan(
                            new ForegroundColorSpan(Color.RED),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
                if (messageView != null) {
                    messageView.setText(spannableString);
                    messageView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            @Override
            public void onFinish() {
                //最后15秒倒计时结束
                // BatteryUtil.shutdownDevice(context);
            }
        };
        currentCountdownTimer.start();
    }

    private static String getInvalidBatteryMessage(Context mContext, int invalidBatteryState) {
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
        if (currentCountdownTimer != null) {
            currentCountdownTimer.cancel();
            currentCountdownTimer = null;
        }
        dismissDialogUI();
    }
    private static void dismissDialogUI() {
        if (windowManager != null && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception e) {
                logger.e("Failed to dismiss dialog: " + e.getMessage());
            }
            floatingView = null;
        }
        // 清空对话框类型
        currentDialogType = null;
    }
}