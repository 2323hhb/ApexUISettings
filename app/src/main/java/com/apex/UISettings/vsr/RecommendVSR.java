package com.apex.UISettings.vsr;

import com.apex.UISettings.utils.FileUtil;
import com.apex.UISettings.utils.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RecommendVSR extends VendorSoftwareReq {

    private static final Logger logger = Logger.getLogger(RecommendVSR.class);

    private static final String EEPROM_PATH = "/sys/class/apex_class/apex_devinfo_class/eepromBatteryData";
    private static final String GET_TP_VERSION = "/sys/class/apex_class/apex_devinfo_class/get_tp_version";
    private static final String GET_LCD_VERSION = "/sys/class/apex_class/apex_devinfo_class/get_lcd_version";
    private static final String SCANNER_FLASHLIGHT_GPIO = "/sys/class/apex_class/apex_devinfo_class/scannerFlashlightGpio";
    private static final String SCANNER_FLASHLIGHT_CONTROL = "/sys/class/apex_class/apex_devinfo_class/scannerFlashlightControl";
    private static final String EEPROM_CRC = "/sys/class/apex_class/apex_devinfo_class/checkEepromCRC";
    private static final String EEPROM_SIZE = "/sys/class/apex_class/apex_devinfo_class/eepromSize";
    private static final String STATE_CODE = "/sys/class/apex_class/apex_devinfo_class/state_code";
    private static final String CHARGE_ENABLE = "/sys/class/apex_class/apex_devinfo_class/setChargeEnable";
    private static final String BATTERY_VOLTAGE = "/sys/class/apex_class/apex_devinfo_class/getButtonBatteryVoltage";
    private static final String ETH_POWER = "/sys/class/apex_class/apex_gpio_class/eth_power";
    private static final String DRAWER_OPEN = "/sys/class/apex_class/apex_cashbox_class/drawerOpen";
    private static final String GET_SCANNER_NAME = "/sys/class/apex_class/apex_devinfo_class/get_scanner_name";
    private static final String SUB_BRIGHTNESS = "/sys/class/apex_class/apex_led_class/subBrightness";
    private static final String GET_SUB_USB_STATE = "/sys/class/apex_class/apex_devinfo_class/getSubUsbState";

    @Override
    public String getTpVersion() {
        enforceOperation(GET_TP_VERSION);
        logger.i("getTpVersion from RecommendVSR");
        return readFromFileOrNull(GET_TP_VERSION);
    }

    @Override
    public String getLcdVersion() {
        enforceOperation(GET_LCD_VERSION);
        logger.i("getLcdVersion from RecommendVSR");
        return readFromFileOrNull(GET_LCD_VERSION);
    }

    @Override
    public boolean setFlashLightState(boolean turnOn) {
        enforceOperation(SCANNER_FLASHLIGHT_GPIO);
        logger.i("setFlashLightState from RecommendVSR");
        return FileUtil.writeToFile(SCANNER_FLASHLIGHT_GPIO, turnOn ? "1" : "0");
    }

    @Override
    public boolean setFlashLightControlEnable(boolean enable) {
        enforceOperation(SCANNER_FLASHLIGHT_CONTROL);
        logger.i("setFlashLightControlEnable from RecommendVSR");
        return FileUtil.writeToFile(SCANNER_FLASHLIGHT_CONTROL, enable ? "1" : "0");
    }

    @Override
    public String getEepromCRC() {
        enforceOperation(EEPROM_CRC);
        logger.i("getEepromCRC from RecommendVSR");
        String value = FileUtil.readFromFile(EEPROM_CRC);
        return value != null ? value : "";
    }

    @Override
    public String getEepromSize() {
        enforceOperation(EEPROM_SIZE);
        logger.i("getEepromSize from RecommendVSR");
        String value = FileUtil.readFromFile(EEPROM_SIZE);
        return value != null ? value : "";
    }


    @Override
    public String getEepromVersion() {
        enforceOperation(EEPROM_PATH);
        logger.i("getEepromVersion from RecommendVSR");
        byte[] value = getValue("EepromVersion");
        return value != null ? new String(value) : "";
    }

    @Override
    public byte[] getEepromData() {
        enforceOperation(EEPROM_PATH);
        logger.i("getEepromData from RecommendVSR");
        return getValue("All");
    }

    @Override
    public String getBatteryStateCode() {
        enforceOperation(STATE_CODE);
        logger.i("getBatteryStateCode from RecommendVSR");
        String value = FileUtil.readFromFile(STATE_CODE);
        return value != null ? value : "";
    }

    @Override
    public String getSOHValue() {
        enforceOperation(EEPROM_PATH);
        logger.i("getSOHValue from RecommendVSR");
        return new String(getValue("SOHValue"), StandardCharsets.UTF_8);
    }

    @Override
    public String getChargingCyclesNumber() {
        enforceOperation(EEPROM_PATH);
        logger.i("getChargingCyclesNumber from RecommendVSR");
        return new String(getValue("ChargingCyclesNumber"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryCapacity() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryCapacity from RecommendVSR");
        return new String(getValue("BatteryCapacity"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatterySupplier() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatterySupplier from RecommendVSR");
        return new String(getValue("BatterySupplier"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryDesignNo() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryDesignNo from RecommendVSR");
        return new String(getValue("BatteryDesignNo"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryDesignLife() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryDesignLife from RecommendVSR");
        return new String(getValue("BatteryDesignLife"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryGenerationDate() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryGenerationDate from RecommendVSR");
        return new String(getValue("BatteryGenerationDate"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryTraceNo() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryTraceNo from RecommendVSR");
        return new String(getValue("BatteryTraceNo"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryMaterialNumber() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryMaterialNumber from RecommendVSR");
        return new String(getValue("BatteryMaterialNumber"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatterySN() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatterySN from RecommendVSR");
        return new String(getValue("BatterySN"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryType() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryType from RecommendVSR");
        return new String(getValue("BatteryType"), StandardCharsets.UTF_8);
    }

    @Override
    public String getBatteryFirstUseTime() {
        enforceOperation(EEPROM_PATH);
        logger.i("getBatteryFirstUseTime from RecommendVSR");
        return new String(getValue("BatteryFirstUseTime"), StandardCharsets.UTF_8);
    }

    @Override
    public String getOverdischargeNumber() {
        enforceOperation(EEPROM_PATH);
        logger.i("getOverdischargeNumber from RecommendVSR");
        return new String(getValue("OverdischargeNumber"), StandardCharsets.UTF_8);
    }

    @Override
    public boolean setChargeEnable(boolean enable) {
        enforceOperation(CHARGE_ENABLE);
        logger.i("setChargeEnable from RecommendVSR");
        return FileUtil.writeToFile(CHARGE_ENABLE, enable ? "1" : "0");
    }

    @Override
    public String getBatteryCoinVol() {
        enforceOperation(BATTERY_VOLTAGE);
        logger.i("getBatteryCoinVol from RecommendVSR");
        String value = FileUtil.readFromFile(BATTERY_VOLTAGE);
        return value != null ? value : "";
    }

    @Override
    public boolean setEthPower(boolean up) {
        enforceOperation(ETH_POWER);
        logger.i("setEthPower from RecommendVSR");
        return FileUtil.writeToFile(ETH_POWER, up ? "1" : "0");
    }

    @Override
    public boolean openDrawer() {
        enforceOperation(DRAWER_OPEN);
        logger.i("openDrawer from RecommendVSR");
        return FileUtil.writeToFile(DRAWER_OPEN, "1");
    }

    @Override
    public String getScannerName() {
        enforceOperation(GET_SCANNER_NAME);
        logger.i("getScannerName from RecommendVSR");
        return readFromFileOrNull(GET_SCANNER_NAME);
    }

    @Override
    public boolean setSubScreenBrightness(String brightness) {
        enforceOperation(SUB_BRIGHTNESS);
        logger.i("setSubScreenBrightness from RecommendVSR");
        return FileUtil.writeToFile(SUB_BRIGHTNESS, brightness);
    }

    @Override
    public int getSubScreenBrightness() {
        enforceOperation(SUB_BRIGHTNESS);
        logger.i("getSubScreenBrightness from RecommendVSR");
        String value = FileUtil.readFromFile(SUB_BRIGHTNESS);
        return value != null ? Integer.parseInt(value) : 0;
    }

    @Override
    public String getSubUsbState() {
        enforceOperation(GET_SUB_USB_STATE);
        logger.i("getSubUsbState from RecommendVSR");
        return FileUtil.readFromFile(GET_SUB_USB_STATE);
    }

    private byte[] readValue() {
        try {
            return Files.readAllBytes(Paths.get(EEPROM_PATH));
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void echoTo(String cmd, String type) {
        try (FileOutputStream fos = new FileOutputStream(EEPROM_PATH)) {
            String str = cmd + "," + type;
            fos.write(str.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getValue(String type) {
        echoTo("get", type);
        return readValue();
    }
}