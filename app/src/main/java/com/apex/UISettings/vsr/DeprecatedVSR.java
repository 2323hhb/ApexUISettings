package com.apex.UISettings.vsr;

import com.apex.UISettings.utils.FileUtil;
import com.apex.UISettings.utils.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * @description
 * @author ch
 * @date 2024/11/29
 * @edit
 */
public class DeprecatedVSR extends VendorSoftwareReq {

    private static final Logger logger = Logger.getLogger(DeprecatedVSR.class);

    private static final String EEPROM_PATH = "/sys/class/i2c-dev/i2c-2/device/2-0050/eepromBatteryData";
    private static final String TP_VERSION = "/sys/class/ld_class/ld_gpio_class/get_tp_version";
    private static final String LCD_VERSION = "/sys/class/ld_class/ld_gpio_class/get_lcd_version";
    private static final String SCANNER_FLASHLIGHT_GPIO = "/sys/class/ld_class/ld_gpio_class/scannerFlashlightGpio";
    private static final String GET_SCANNER_NAME = "/sys/class/ld_class/ld_gpio_class/get_scanner_name";
    private static final String SCANNER_FLASHLIGHT_CONTROL = "/sys/class/ld_class/ld_gpio_class/scannerFlashlightControl";
    private static final String EEPROM_CRC = "/sys/class/i2c-dev/i2c-2/device/2-0050/checkEepromCRC";
    private static final String EEPROM_SIZE = "/sys/class/i2c-dev/i2c-2/device/2-0050/eepromSize";
    private static final String STATE_CODE = "/sys/class/ld_class/ld_gpio_class/state_code";
    private static final String CHARGE_ENABLE = "/sys/devices/platform/soc/4a80000.i2c/i2c-0/0-006b/setChangeEnable";
    private static final String BATTERY_VOLTAGE = "/sys/devices/platform/soc/soc:ldgpiodev/getButtonBatteryVoltage";
    private static final String ETH_POWER = "/sys/class/ld_class/ld_gpio_class/eth_power";
    private static final String DRAWER_OPEN = "/sys/devices/platform/soc/soc:meig_gpio_demo/output1";
    private static final String SUB_BRIGHTNESS = "/sys/class/backlight/aw99703-bl/brightness";
    private static final String GET_SUB_USB_STATE = "/sys/class/ld_class/ld_gpio_class/getSubUsbState";

    @Override
    public String getTpVersion() {
        enforceOperation(TP_VERSION);
        logger.w("getTpVersion from DeprecatedVSR");
        return readFromFileOrNull(TP_VERSION);
    }

    @Override
    public String getLcdVersion() {
        enforceOperation(LCD_VERSION);
        logger.w("getLcdVersion from DeprecatedVSR");
        return readFromFileOrNull(LCD_VERSION);
    }

    @Override
    public boolean setFlashLightState(boolean turnOn) {
        enforceOperation(SCANNER_FLASHLIGHT_GPIO);
        logger.w("setFlashLightState from DeprecatedVSR");
        return FileUtil.writeToFile(SCANNER_FLASHLIGHT_GPIO, turnOn ? "1" : "0");
    }

    @Override
    public boolean setFlashLightControlEnable(boolean enable) {
        enforceOperation(SCANNER_FLASHLIGHT_CONTROL);
        logger.w("setFlashLightControlEnable from DeprecatedVSR");
        return FileUtil.writeToFile(SCANNER_FLASHLIGHT_CONTROL, enable ? "1" : "0");
    }

    @Override
    public String getEepromCRC() {
        enforceOperation(EEPROM_CRC);
        logger.w("getEepromCRC from DeprecatedVSR");
        return FileUtil.readFromFile(EEPROM_CRC);
    }

    @Override
    public String getEepromSize() {
        enforceOperation(EEPROM_SIZE);
        logger.w("getEepromSize from DeprecatedVSR");
        return FileUtil.readFromFile(EEPROM_SIZE);
    }

    @Override
    public String getEepromVersion() {
        enforceOperation(EEPROM_PATH);
        logger.w("getEepromVersion from DeprecatedVSR");
        return toHexString(getValue("EepromVersion", 1));
    }

    @Override
    public byte[] getEepromData() {
        enforceOperation(EEPROM_PATH);
        logger.w("getEepromData from DeprecatedVSR");
        return getValue("All");
    }

    @Override
    public String getSOHValue() {
        enforceOperation(EEPROM_PATH);
        logger.w("getSOHValue from DeprecatedVSR");
        return String.valueOf(getValue("SOHValue", 1)[0]);
    }

    @Override
    public String getChargingCyclesNumber() {
        enforceOperation(EEPROM_PATH);
        logger.w("getChargingCyclesNumber from DeprecatedVSR");
        return toHexString(getValue("ChargingCyclesNumber"));
    }

    @Override
    public String getBatteryCapacity() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatteryCapacity from DeprecatedVSR");
        byte[] capacity = getValue("BatteryCapacity", 2);
        if (capacity.length == 2) {
            String hexString = toHexString(swapEndianness(capacity));
            int value = Integer.parseInt(hexString, 16);
            return String.valueOf(value);
        }
        return null;
    }

    @Override
    public String getBatterySupplier() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatterySupplier from DeprecatedVSR");
        byte[] batteryTypeByteArray = getValue("BatteryType", 2);
        if (batteryTypeByteArray.length == 2) {
            byte byte2 = batteryTypeByteArray[1];
            StringBuilder supplier = new StringBuilder();
            supplier.append((byte2 & 0x80) == 0x80 ? "1" : "0");
            supplier.append((byte2 & 0x40) == 0x40 ? "1" : "0");
            supplier.append((byte2 & 0x20) == 0x20 ? "1" : "0");
            return getSupplierName(supplier.toString());
        }
        return null;
    }

    @Override
    public String getBatteryDesignNo() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatteryDesignNo from DeprecatedVSR");
        byte[] batteryTypeByteArray = getValue("BatteryType", 2);
        if (batteryTypeByteArray.length == 2) {
            byte byte1 = batteryTypeByteArray[0];
            StringBuilder designNo = new StringBuilder();
            designNo.append((byte1 & 0x40) == 0x40 ? "1" : "0");
            designNo.append((byte1 & 0x20) == 0x20 ? "1" : "0");
            designNo.append((byte1 & 0x10) == 0x10 ? "1" : "0");
            return getDesignName(designNo.toString());
        }
        return null;
    }

    @Override
    public String getBatteryDesignLife() {
        logger.w("getBatteryDesignLife from DeprecatedVSR");
        return null;
    }

    @Override
    public String getBatteryGenerationDate() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatteryGenerationDate from DeprecatedVSR");
        return new String(getValue("BatteryGenerationDate", 6));
    }

    @Override
    public String getBatteryTraceNo() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatteryTraceNo from DeprecatedVSR");
        String serialNo = new String(getValue("BatterySN", 23));
        return serialNo.length() > 18 ? serialNo.substring(17) : null;
    }

    @Override
    public String getBatteryMaterialNumber() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatteryMaterialNumber from DeprecatedVSR");
        String serialNo = new String(getValue("BatterySN", 23));
        return serialNo.length() > 10 ? serialNo.substring(0, 10) : null;
    }

    @Override
    public String getBatteryStateCode() {
        enforceOperation(STATE_CODE);
        logger.w("getBatteryStateCode from DeprecatedVSR");
        return FileUtil.readFromFile(STATE_CODE);
    }

    @Override
    public String getBatterySN() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatterySN from DeprecatedVSR");
        return new String(getValue("BatterySN", 23));
    }

    @Override
    public String getBatteryType() {
        enforceOperation(EEPROM_PATH);
        logger.w("getBatteryType from DeprecatedVSR");
        byte[] batteryTypeByteArray = getValue("BatteryType", 2);
        return toHexString(batteryTypeByteArray);
    }

    @Override
    public String getBatteryFirstUseTime() {
        logger.w("getBatteryFirstUseTime from DeprecatedVSR");
        return null;
    }

    @Override
    public String getOverdischargeNumber() {
        logger.w("getOverdischargeNumber from DeprecatedVSR");
        return null;
    }

    @Override
    public boolean setChargeEnable(boolean enable) {
        enforceOperation(CHARGE_ENABLE);
        logger.w("setChargeEnable from DeprecatedVSR");
        return FileUtil.writeToFile(CHARGE_ENABLE, enable ? "1" : "0");
    }

    @Override
    public String getBatteryCoinVol() {
        enforceOperation(BATTERY_VOLTAGE);
        logger.w("getBatteryCoinVol from DeprecatedVSR");
        return FileUtil.readFromFile(BATTERY_VOLTAGE);
    }

    @Override
    public boolean setEthPower(boolean up) {
        enforceOperation(ETH_POWER);
        logger.w("setEthPower from DeprecatedVSR");
        return FileUtil.writeToFile(ETH_POWER, up ? "1" : "0");
    }

    @Override
    public boolean openDrawer() {
        enforceOperation(DRAWER_OPEN);
        logger.w("openDrawer from DeprecatedVSR");
        return FileUtil.writeToFile(DRAWER_OPEN, "1");
    }

    @Override
    public String getScannerName() {
        enforceOperation(GET_SCANNER_NAME);
        logger.w("getScannerName from DeprecatedVSR");
        return readFromFileOrNull(GET_SCANNER_NAME);
    }

    @Override
    public boolean setSubScreenBrightness(String brightness) {
        enforceOperation(SUB_BRIGHTNESS);
        logger.w("setSubScreenBrightness from DeprecatedVSR");
        return FileUtil.writeToFile(SUB_BRIGHTNESS, brightness);
    }

    @Override
    public int getSubScreenBrightness() {
        enforceOperation(SUB_BRIGHTNESS);
        logger.w("getSubScreenBrightness from DeprecatedVSR");
        return Integer.parseInt(FileUtil.readFromFile(SUB_BRIGHTNESS));
    }

    @Override
    public String getSubUsbState() {
        enforceOperation(GET_SUB_USB_STATE);
        logger.w("getSubUsbState from DeprecatedVSR");
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

    private byte[] readValue(int size) {
        byte[] byteArray = new byte[size];
        try (FileInputStream fis = new FileInputStream(new File(EEPROM_PATH))) {
            int count = fis.read(byteArray);
            return java.util.Arrays.copyOfRange(byteArray, 0, count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    private void echoTo(String cmd, String type) {
        try (FileOutputStream fos = new FileOutputStream(EEPROM_PATH)) {
            fos.write((cmd + "," + type).getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getValue(String type) {
        echoTo("get", type);
        return readValue();
    }

    private byte[] getValue(String type, int size) {
        echoTo("get", type);
        return readValue(size);
    }

    private byte[] swapEndianness(byte[] byteArray) {
        byte[] result = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            result[i] = byteArray[byteArray.length - i - 1];
        }
        return result;
    }

    private String getDesignName(String designNo) {
        switch (designNo) {
            case "000":
                return "GanfengLiEnergy-526265";
            case "001":
                return "ATL-526266";
            case "010":
                return "VEKEN-526265PU";
            default:
                return designNo;
        }
    }

    private String getSupplierName(String supplier) {
        switch (supplier) {
            case "000":
                return "GanfengLiEnergy";
            case "001":
                return "SCUD";
            default:
                return supplier;
        }
    }

    private String toHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
}
