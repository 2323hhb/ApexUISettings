package com.apex.UISettings.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.opendroid.devicemanager.ExtDeviceManager.getSupportDeviceList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.apex.UISettings.contants.CFDSetConstants;
import com.apex.UISettings.utils.SystemPropertiesUtil;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;

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
    private boolean isCFDSupport = false;
    private boolean isNFCSupport = false;
//    private BroadcastReceiver mBatteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        //brightnessSeekBar = findViewById(R.id.brightness_seekbar);
        initViews();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        @SuppressLint("WrongViewCast") ImageButton backButton = findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }

        restoreSettings();
        //设置整行点击控制Switch
        setupRowClickControls();
        checkDeviceStateToSetTitle();
        //亮度调节SeekBar控制
        setupBrightnessControl();
        initCustomerDisplay();

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
        logger.i("hasTouchSupport: " + hasTouchSupport);
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
        displaySettingRow.setVisibility(GONE);

        if (displaySwitch != null) {
            displaySettingRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displaySwitch.setChecked(!displaySwitch.isChecked());
//                    saveSetting("display_switch", displaySwitch.isChecked());
                }
            });

            displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    saveSetting(CFDSetConstants.CFDStateRestore.DISPLAY_SWITCH, isChecked);
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
        //touchSwitch = findViewById(R.id.touch_switch);
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
                        saveSetting(CFDSetConstants.CFDStateRestore.TOUCH_SWITCH, touchSwitch.isChecked());
                    }
                }
            });

            touchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // 保存Touch开关状态
                    saveSetting(CFDSetConstants.CFDStateRestore.TOUCH_SWITCH, isChecked);
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
                    if ((type == 1 || type == 3) && NFC_state.equals(CFDSetConstants.DeviceState.NORMAL)){
//                    if (type == 1 || type == 3){
                            nfcSettingRow.setVisibility(VISIBLE);
                            isNFCSupport = true;
                    }
                }
                if (deviceTypeId == CFDSetConstants.DeviceTypeId.LCD) {
                    index = device.get(CFDSetConstants.DevicesInfo.INDEX).getAsInt();
                    String LCD_state = device.get(CFDSetConstants.DevicesInfo.STATE).getAsString();
                    Log.d(TAG,"LCD_Index: "+index);
                    Log.d(TAG,"LCD_state: "+LCD_state);
                    if (index == 1 && LCD_state.equals(CFDSetConstants.DeviceState.NORMAL)){
                        displaySettingRow.setVisibility(VISIBLE);
                        if (vsr.getSubScreenBrightness() !=0){
                            displaySwitch.setChecked(true);
                            saveSetting(CFDSetConstants.CFDStateRestore.DISPLAY_SWITCH, displaySwitch.isChecked());
                        }
                        touchSettingRow.setVisibility(VISIBLE);
                        isCFDSupport = true;
                        logger.d("isCFDSupport: "+isCFDSupport);
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
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LauncherActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(CFDSetConstants.CFDStateRestore.BRIGHTNESS, brightness);
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
    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    private void saveSetting(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }
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
            boolean displaySwitchState = sharedPreferences.getBoolean(CFDSetConstants.CFDStateRestore.DISPLAY_SWITCH, false);
            displaySwitch.setChecked(displaySwitchState);
        }

        // 恢复Touch开关状态
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch touchSwitch = findViewById(R.id.touch_switch);
        if (touchSwitch != null) {
            if (displaySwitch != null && displaySwitch.isChecked()) {
                boolean touchSwitchState = sharedPreferences.getBoolean(CFDSetConstants.CFDStateRestore.TOUCH_SWITCH, false);
                touchSwitch.setChecked(touchSwitchState);
            } else {
                touchSwitch.setChecked(false);
            }
            if (touchSettingRow != null && displaySwitch != null) {
                boolean displayEnabled = displaySwitch.isChecked();
                touchSettingRow.setEnabled(displayEnabled);
                touchSettingRow.setAlpha(displayEnabled ? 1.0f : 0.5f);
            }
        }
    }

    public void checkDeviceStateToSetTitle(){
        TextView settingsTitle = findViewById(R.id.settings_title);
        if (isCFDSupport && isNFCSupport){
            settingsTitle.setText(R.string.cfd_nfc_settings);
        }else if (isCFDSupport){
            settingsTitle.setText(R.string.cfd_settings);
        }else if (isNFCSupport){
            settingsTitle.setText(R.string.nfc_settings);
        }else {
            settingsTitle.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}