package com.apex.UISettings.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.opendroid.devicemanager.ExtDeviceManager.getSupportDeviceList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.apex.UISettings.contants.CFDSetConstants;
import com.apex.UISettings.utils.BatteryUtil;
import com.apex.UISettings.utils.SystemPropertiesUtil;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.apex.UISettings.R;
import com.apex.UISettings.utils.DeviceUtil;
import com.apex.UISettings.vsr.VSRManager;
import com.apex.UISettings.utils.Logger;
import com.opendroid.base.ExtStringWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Objects;


public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "ApexLauncherActivity";
    private static final String PREFS_NAME = "settings_prefs";
    private static final String BRIGHTNESS = "customer_display_brightness";
    private static final int DEFAULT_BRIGHTNESS_VALUE = 255;
    private static final Logger logger = Logger.getLogger("ApexLauncherActivity");
    private SharedPreferences sharedPreferences;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch displaySwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch touchSwitch;
    private SeekBar brightnessSeekBar;
    private LinearLayout brightnessLayout;
    private LinearLayout touchSettingRow;
    VSRManager vsr = VSRManager.getInstance();
    private BroadcastReceiver mBatteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        //brightnessSeekBar = findViewById(R.id.brightness_seekbar);
        initViews();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        @SuppressLint("WrongViewCast") ImageButton backButton = findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
        initCustomerDisplay();
        //设置整行点击控制Switch
        setupRowClickControls();
        //注册电池异常接收广播
        registerReceiver(mBatteryReceiver, filter);
        //亮度调节SeekBar控制
        setupBrightnessControl();
        restoreSettings();
    }
    private void initViews() {
        displaySwitch = findViewById(R.id.display_switch);
        touchSwitch = findViewById(R.id.touch_switch);
        brightnessSeekBar = findViewById(R.id.brightness_seekbar);
        brightnessLayout = findViewById(R.id.brightness_layout);
        touchSettingRow = findViewById(R.id.touch_setting_row);
    }
    /**
     * 初始化副屏功能
     */
    private void initCustomerDisplay() {
        //检查设备是否支持副屏触摸功能
        boolean hasTouchSupport = isSubTp();
        //根据设备能力显示/隐藏触摸设置项
        if (touchSettingRow != null) {
            touchSettingRow.setVisibility(hasTouchSupport ? VISIBLE : GONE);
        }
        //根据Display开关状态显示/隐藏亮度调节
        updateBrightnessVisibility();
        //应用启动时初始化副屏亮度
        initBrightnessOnStartup();
    }

    /**
     * 检查设备是否支持副屏触摸
     */
    private boolean isSubTp() {
        String tpVersion = vsr.getTpVersion();
        return (tpVersion != null) && tpVersion.contains("2nd:");
    }
    /**
     * 发送亮度值到副屏硬件
     */
    private void sendBrightnessToNode(String brightness) {
        logger.d("sendToNode : " + brightness);
        vsr.setSubScreenBrightness(brightness);

        // 如果是C20x设备，缓存亮度值
        if (DeviceUtil.isC20x()) {
            logger.d("write to cache brightness : " + brightness);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(BRIGHTNESS, brightness);
            editor.apply();
        }
    }
    /**
     * 应用启动时初始化副屏亮度
     */
    private void initBrightnessOnStartup() {

        if (DeviceUtil.isC20x()) {
            String value = SystemPropertiesUtil.get("persist.sys.customer.display.enable");
            if (!TextUtils.isEmpty(value) && value.equals("false")) {
                logger.d("CustomerScreenDisplay is false");
            } else {
                String brightness = sharedPreferences.getString(BRIGHTNESS,
                        String.valueOf(DEFAULT_BRIGHTNESS_VALUE));
//                Log.d(TAG,"brightness is "+brightness);
                // 将 0-255 范围的亮度值转换为 0-100 范围的进度值
                int brightnessValue = Integer.parseInt(brightness);
                int progress = (int) ((brightnessValue / 255f) * 100);
//                Log.d(TAG,"progress is "+progress);
                if (brightnessSeekBar != null) {
                    brightnessSeekBar.post(() -> brightnessSeekBar.setProgress(progress));
                }
                logger.d("read from cache brightness : " + brightness);
                if (!TextUtils.isEmpty(brightness)) {
                    vsr.setSubScreenBrightness(brightness);
                }
            }
        }
    }
    private void switchTouchOnCustomerDisplay(boolean open) {
        // 只有在副屏开启的情况下才能设置触摸功能
        if (displaySwitch != null && displaySwitch.isChecked()) {
            Settings.Global.putInt(getContentResolver(),
                    "customer_display_touch_enable", open ? 1 : 0);
            logger.d("Touch on customer display: " + open);
        }
    }
    /**
     * 切换副屏显示功能
     * @param open true=开启副屏, false=关闭副屏
     */
    private void switchCustomerScreenDisplay(boolean open) {
        if (open) {
            if (touchSwitch != null) {
                boolean touchEnabled = sharedPreferences.getBoolean("customerDisplayTouch", true);
                touchSwitch.setChecked(touchEnabled);

                Settings.Global.putInt(getContentResolver(),
                        "customer_display_touch_enable", touchEnabled ? 1 : 0);
            }
            String brightness = sharedPreferences.getString(BRIGHTNESS,
                    String.valueOf(DEFAULT_BRIGHTNESS_VALUE));
            logger.d("read from cache brightness : " + brightness);
            if (!TextUtils.isEmpty(brightness)) {
                vsr.setSubScreenBrightness(brightness);
            }
            if (brightnessLayout != null) {
                brightnessLayout.setVisibility(VISIBLE);
            }

        } else {
            vsr.setSubScreenBrightness("0");
            if (brightnessLayout != null) {
                brightnessLayout.setVisibility(GONE);
            }

            Settings.Global.putInt(getContentResolver(),
                    "customer_display_touch_enable", 0);
        }

        SystemPropertiesUtil.set("persist.sys.customer.display.enable", String.valueOf(open));
        logger.d("Customer screen display: " + open);
    }

    /**
     * 更新亮度区域的可见性
     */
    private void updateBrightnessVisibility() {
        if (brightnessLayout != null && displaySwitch != null) {
            brightnessLayout.setVisibility(
                    displaySwitch.isChecked() ? VISIBLE : GONE);
        }
    }

    private void setupRowClickControls() {
        // Display设置项整行点击
        LinearLayout displaySettingRow = findViewById(R.id.display_setting_row);
        displaySwitch = findViewById(R.id.display_switch);
        displaySettingRow.setVisibility(GONE);

        if (displaySwitch != null) {
            displaySettingRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displaySwitch.setChecked(!displaySwitch.isChecked());
                }
            });

            displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    saveSetting("display_switch", isChecked);
                    switchCustomerScreenDisplay(isChecked);
                    //同步touchSwitch
                    if (touchSwitch != null) {
                        touchSwitch.setChecked(isChecked);
                        touchSettingRow.setEnabled(isChecked);
                        touchSettingRow.setAlpha(isChecked ? 1.0f : 0.5f);
                        switchTouchOnCustomerDisplay(isChecked);
                    }

                }
            });
        }

        touchSettingRow = findViewById(R.id.touch_setting_row);
        touchSwitch = findViewById(R.id.touch_switch);
        touchSettingRow.setVisibility(GONE);
        if (!displaySwitch.isChecked()){
            touchSettingRow.setEnabled(displaySwitch.isChecked());
            touchSettingRow.setAlpha(displaySwitch.isChecked() ? 1.0f : 0.5f);
        }

        if (touchSettingRow != null && touchSwitch != null) {
            touchSettingRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (touchSettingRow != null && displaySwitch.isChecked()) {
                        touchSwitch.setChecked(!touchSwitch.isChecked());
                    }
                }
            });

            touchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // 保存Touch开关状态
                    saveSetting(CFDSetConstants.NFCStateRestore.DISPLAY_SWITCH, isChecked);
                    switchTouchOnCustomerDisplay(isChecked);
                }
            });
        }

        LinearLayout nfcSettingRow = findViewById(R.id.nfc_setting_row);
        nfcSettingRow.setOnClickListener(s->{
            Intent intent = new Intent(this, NFCSetActivity.class);
            startActivity(intent);
        });
        nfcSettingRow.setVisibility(GONE);
        //判断是否支持NFC水印和副屏菜单
        ExtStringWrapper deviceList = new ExtStringWrapper();
        int result = getSupportDeviceList(deviceList);
        int type;
        int index;

        if (result == 0) {
            String jsonResult = deviceList.value;
            Log.d(TAG,jsonResult);
            Gson gson = new Gson();
            JsonArray devices = gson.fromJson(jsonResult, JsonArray.class);
            for (int i = 0; i < devices.size(); i++) {
                JsonObject device = devices.get(i).getAsJsonObject();
                int deviceTypeId = device.get(CFDSetConstants.DevicesType.DEVICE_TYPE_ID).getAsInt();
                if (deviceTypeId == CFDSetConstants.DeviceTypeId.NFC) {
                    type = device.get(CFDSetConstants.DevicesInfo.TYPE).getAsInt();
                    String NFC_state = device.get(CFDSetConstants.DevicesInfo.STATE).getAsString();
                    Log.d(TAG,"NFC_type: "+type);
                    Log.d(TAG,"NFC_state: "+NFC_state);
//                    if ((type == 1 || type == 3) && NFC_state.equals("normal")){
                    if (type == 1 || type == 3){
                            nfcSettingRow.setVisibility(VISIBLE);
                    }
                }
                if (deviceTypeId == CFDSetConstants.DeviceTypeId.LCD) {
                    index = device.get(CFDSetConstants.DevicesInfo.INDEX).getAsInt();
                    String LCD_state = device.get(CFDSetConstants.DevicesInfo.STATE).getAsString();
                    Log.d(TAG,"LCD_Index: "+index);
                    Log.d(TAG,"LCD_state: "+LCD_state);
                    if (index == 1 && LCD_state.equals(CFDSetConstants.DeviceState.NORMAL)){
                        displaySettingRow.setVisibility(VISIBLE);
                        touchSettingRow.setVisibility(VISIBLE);

                    }
                }
                if (deviceTypeId == CFDSetConstants.DeviceTypeId.BATTERY){
                    String batteryState = device.get(CFDSetConstants.DevicesInfo.STATE).getAsString();
                    Log.d(TAG,"batteryState: "+ batteryState);
                    if (batteryState.equals(CFDSetConstants.DeviceState.NORMAL)){
                        mBatteryReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Log.d(TAG,"注册电池广播接收器");
                                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                                    BatteryUtil.BatteryStatusInfo statusInfo = BatteryUtil.checkBatteryStatus(context, intent);
                                    if (statusInfo != null) {
                                        if(Objects.equals(statusInfo.title, context.getString(R.string.low_battery))){
                                            showBatteryAlertDialog(context,statusInfo.iconId,statusInfo.title,statusInfo.message);
                                            BatteryUtil.startToShutDownDevice(context);
                                        }else{
                                            showBatteryAlertDialog(context,statusInfo.iconId,statusInfo.title,statusInfo.message);
                                        }
                                    }
                                }
                            }
                        };
                    }
                }
            }
        }else{
            Log.e(TAG,"opendroidDeviceManager getSupportDeviceList failed");
        }
    }
    private void setupBrightnessControl() {

        if (brightnessSeekBar != null ) {
            brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        int brightness = (int) ((progress / 100f) * 255);
                        logger.i("brightness = " + brightness);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LauncherActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(CFDSetConstants.NFCStateRestore.BRIGHTNESS, brightness);
                        editor.apply();
                        if (brightness < 10) {
                            brightness = 10;
                        }
                        logger.i("set brightness " + brightness);
                        //发送到节点
                        sendBrightnessToNode(String.valueOf(brightness));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }

    // 保存设置到SharedPreferences
    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // 保存设置到SharedPreferences
    private void saveSetting(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    // 保存设置到SharedPreferences
    private void saveSetting(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // 恢复保存的设置
    private void restoreSettings() {
        // 恢复Display开关状态
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch displaySwitch = findViewById(R.id.display_switch);
        if (displaySwitch != null) {
            boolean displaySwitchState = sharedPreferences.getBoolean(CFDSetConstants.NFCStateRestore.DISPLAY_SWITCH, false);
            displaySwitch.setChecked(displaySwitchState);
        }

        // 恢复Touch开关状态
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch touchSwitch = findViewById(R.id.touch_switch);
        if (touchSwitch != null) {
            boolean touchSwitchState = sharedPreferences.getBoolean(CFDSetConstants.NFCStateRestore.TORCH_SWITCH, false);
            touchSwitch.setChecked(touchSwitchState);
        }

//        // 恢复亮度设置
//        SeekBar brightnessSeekBar = findViewById(R.id.brightness_seekbar);
//        if (brightnessSeekBar != null) {
//            int brightness = sharedPreferences.getInt(CFDSetConstants.NFCStateRestore.BRIGHTNESS, 50);
//            brightnessSeekBar.setProgress(brightness);
//        }
    }


    public void showBatteryAlertDialog(Context context, int iconId, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customView = LayoutInflater.from(context).inflate(R.layout.battery_dialog, null);

        ImageView dialogIcon = customView.findViewById(R.id.dialog_icon);
        TextView dialogTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogMessage = customView.findViewById(R.id.dialog_message);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) View dialogDivider = customView.findViewById(R.id.dialog_divider);
//        Button dialogButton = customView.findViewById(R.id.dialog_button);

        dialogIcon.setImageResource(iconId);
        dialogTitle.setText(title);
        dialogMessage.setText(message);

        if (!(title.equals(context.getString(R.string.low_battery)))) {
            // 显示分界线和按钮
            dialogDivider.setVisibility(View.VISIBLE);
//            dialogButton.setVisibility(View.VISIBLE);
            builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        }

        builder.setView(customView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        // 设置按钮居中
        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                // 使用布局参数使按钮居中
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                params.gravity = Gravity.CENTER;  // 设置居中
                positiveButton.setLayoutParams(params);
            }
        });
        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销广播接收器
        if (mBatteryReceiver != null) {
            unregisterReceiver(mBatteryReceiver);
        }
    }
}