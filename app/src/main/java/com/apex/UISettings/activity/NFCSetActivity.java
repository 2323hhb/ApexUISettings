package com.apex.UISettings.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.apex.UISettings.R;
import com.opendroid.system.ExtSystemManager;
import com.opendroid.system.api.ui.ExtUIManager;
import com.apex.UISettings.contants.NFCSetConstants.NFCSet;

public class NFCSetActivity extends AppCompatActivity {
    private static final String TAG = "NFCSetActivity";
    private ExtUIManager extUIManager;
    private int alpha;
    private int gravity;
    private Boolean display;
    private Switch NFCDisplay;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nfcset);

        extUIManager = ExtSystemManager.get(this, ExtUIManager.class);
        if (extUIManager == null) {
            Log.e(TAG, "extUIManager is null");
            return;
        }
        @SuppressLint("WrongViewCast") ImageButton backButton = findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
        NFCDisplay = findViewById(R.id.nfc_display_switch);
        Bundle NFCSetBundle = new Bundle();
        int QueryNFCSettingsRes = extUIManager.getNfcWindowConfiguration(NFCSetBundle);
        if (QueryNFCSettingsRes == 0){
            alpha = NFCSetBundle.getInt(NFCSet.Type.ALPHA);
//            Log.d(TAG,"alpha is "+alpha);
            gravity = NFCSetBundle.getInt(NFCSet.Type.GRAVITY);
//            Log.d(TAG,"gravity is "+gravity);
            display = NFCSetBundle.getBoolean(NFCSet.Type.DISPLAY);
//            Log.d(TAG,"display is "+display);
            NFCDisplay.setChecked(display);
        }else {
            Log.d(TAG,"get NFCSettings failed");
        }
        setupNfcDisplay();
        restoreSettings();
        setupReset();
    }

    private void setupNfcDisplay() {
        // NFC Display开关
        LinearLayout displaySettingRow = findViewById(R.id.nfc_display_switch_set);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch displaySwitch = findViewById(R.id.nfc_display_switch);
        if (displaySettingRow != null && displaySwitch != null) {
            displaySettingRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displaySwitch.setChecked(!displaySwitch.isChecked());
                    if (!displaySwitch.isChecked() && extUIManager != null) {
                        extUIManager.hideNfcWindow();
                    }
                }
            });

            displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateNfcSettingsVisibility(isChecked);
                    if (isChecked){
                        Bundle NFCSettingsBundle = new Bundle();
                        NFCSettingsBundle.putInt(NFCSet.Type.ALPHA,alpha);
                        NFCSettingsBundle.putInt(NFCSet.Type.GRAVITY,gravity);
                        extUIManager.showNfcWindow(NFCSettingsBundle);
                    }
                }
            });
        }

        RadioGroup nfcAlphaRadioGroup = findViewById(R.id.nfc_alpha_radio_group);
        if (nfcAlphaRadioGroup != null) {
            setupAlphaListener(nfcAlphaRadioGroup);
        }

        RadioGroup nfcPositionRadioGroup = findViewById(R.id.nfc_position_radio_group);
        if (nfcPositionRadioGroup != null) {
            setupPositionListener(nfcPositionRadioGroup);
        }
    }

    private void setupAlphaListener(RadioGroup nfcAlphaRadioGroup) {
        nfcAlphaRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == -1) return; // 避免clearCheck()时的处理
            Bundle bundle = new Bundle();
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                String tag = (String) selectedRadioButton.getTag();
//                Log.d(TAG,"selectedAlphaTag= "+ tag);
                switch (tag) {
                    case NFCSet.AlphaType.Close:
                        bundle.putInt(NFCSet.Type.ALPHA, 0);
                        break;
                    case NFCSet.AlphaType.Thirty:
                        bundle.putInt(NFCSet.Type.ALPHA, 1);
                        break;
                    case NFCSet.AlphaType.Seventy:
                        bundle.putInt(NFCSet.Type.ALPHA, 2);
                        break;
                }
//                Log.d(TAG, String.valueOf(bundle));
                extUIManager.showNfcWindow(bundle);
            }
        });
    }

    private void setupPositionListener(RadioGroup nfcPositionRadioGroup) {
        nfcPositionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == -1) return;
            Bundle bundle = new Bundle();
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                String tag = (String) selectedRadioButton.getTag();
//                Log.d(TAG,"selectedPositionTag = "+ tag);
                switch (tag) {
                    case NFCSet.PositionType.PositionA:
                        bundle.putInt(NFCSet.Type.GRAVITY, 0);
                        break;
                    case NFCSet.PositionType.PositionB:
                        bundle.putInt(NFCSet.Type.GRAVITY, 1);
                        break;
                    case NFCSet.PositionType.Both:
                        bundle.putInt(NFCSet.Type.GRAVITY, 2);
                        break;
                }
//                Log.d(TAG, String.valueOf(bundle));
                extUIManager.showNfcWindow(bundle);
            }
        });
    }

    private void setupReset() {
        LinearLayout nfcSettingRow = findViewById(R.id.reset_nfc_row);
        if (nfcSettingRow != null) {
            nfcSettingRow.setOnClickListener(v -> {
                resetNfcSettings();
            });
        }
    }

    private void resetNfcSettings() {

        if (extUIManager != null) {
            int ret = extUIManager.resetNfcWindow();
            Log.d(TAG, "Reset NFC Window: " + ret);
        }

        // 重置Alpha设置
        RadioGroup nfcAlphaRadioGroup = findViewById(R.id.nfc_alpha_radio_group);
        if (nfcAlphaRadioGroup != null) {
            nfcAlphaRadioGroup.setOnCheckedChangeListener(null);
            nfcAlphaRadioGroup.clearCheck();
            setupAlphaListener(nfcAlphaRadioGroup);
        }
        RadioGroup nfcPositionRadioGroup = findViewById(R.id.nfc_position_radio_group);
        if (nfcPositionRadioGroup != null) {
            nfcPositionRadioGroup.setOnCheckedChangeListener(null);
            nfcPositionRadioGroup.clearCheck();
            setupPositionListener(nfcPositionRadioGroup);
        }
        NFCDisplay.setChecked(false);


    }
    private void updateNfcSettingsVisibility(boolean isVisible) {

        LinearLayout nfcAlphaSettingRow = findViewById(R.id.nfc_alpha_setting_row);
        if (nfcAlphaSettingRow != null) {
            nfcAlphaSettingRow.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }

        RadioGroup nfcAlphaRadioGroup = findViewById(R.id.nfc_alpha_radio_group);
        if (nfcAlphaRadioGroup != null && nfcAlphaSettingRow == null) {
            nfcAlphaRadioGroup.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
        LinearLayout nfcPositionSettingRow = findViewById(R.id.nfc_position_setting_row);
        if (nfcPositionSettingRow != null) {
            nfcPositionSettingRow.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }

        RadioGroup nfcPositionRadioGroup = findViewById(R.id.nfc_position_radio_group);
        if (nfcPositionRadioGroup != null && nfcPositionSettingRow == null) {
            nfcPositionRadioGroup.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
        LinearLayout nfcResetRow = findViewById(R.id.reset_nfc_row);
        if (nfcResetRow != null) {
            nfcResetRow.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }

    }
    private void restoreSettings() {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch nfcDisplaySwitch = findViewById(R.id.nfc_display_switch);
        if (nfcDisplaySwitch != null) {
            nfcDisplaySwitch.setChecked(display);
            updateNfcSettingsVisibility(display);
        }

        RadioGroup nfcAlphaRadioGroup = findViewById(R.id.nfc_alpha_radio_group);
        if (nfcAlphaRadioGroup != null) {
            int selectedAlpha = alpha;
                switch (selectedAlpha) {
                    case 0:
                        nfcAlphaRadioGroup.check(R.id.nfc_alpha_close);
                        break;
                    case 1:
                        nfcAlphaRadioGroup.check(R.id.nfc_alpha_30);
                        break;
                    case 2:
                        nfcAlphaRadioGroup.check(R.id.nfc_alpha_70);
                        break;
            }
        }
        RadioGroup nfcPositionRadioGroup = findViewById(R.id.nfc_position_radio_group);
        if (nfcPositionRadioGroup != null) {
            int selectedPosition = gravity;
                switch (selectedPosition) {
                    case 0:
                        nfcPositionRadioGroup.check(R.id.nfc_position_a);
                        break;
                    case 1:
                        nfcPositionRadioGroup.check(R.id.nfc_position_b);
                        break;
                    case 2:
                        nfcPositionRadioGroup.check(R.id.nfc_position_both);
                        break;
                }
        }
    }
}