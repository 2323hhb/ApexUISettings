package com.apex.UISettings.vsr;

import android.text.TextUtils;
import com.apex.UISettings.utils.FileUtil;

/**
 * @description Abstract class for Vendor Software Requests
 * @author ch
 * @date 2024/11/29
 * @edit
 */
public abstract class VendorSoftwareReq {

    public abstract boolean openDrawer() throws UnsupportedOperationException;

    public abstract boolean setEthPower(boolean up) throws UnsupportedOperationException;

    public abstract String getBatteryCoinVol() throws UnsupportedOperationException;

    public abstract boolean setChargeEnable(boolean enable) throws UnsupportedOperationException;

    public abstract String getTpVersion() throws UnsupportedOperationException;

    public abstract String getLcdVersion() throws UnsupportedOperationException;

    public abstract boolean setFlashLightControlEnable(boolean enable) throws UnsupportedOperationException;

    public abstract boolean setFlashLightState(boolean turnOn) throws UnsupportedOperationException;

    public abstract String getEepromCRC() throws UnsupportedOperationException;

    public abstract String getEepromSize() throws UnsupportedOperationException;

    public abstract String getEepromVersion() throws UnsupportedOperationException;

    public abstract byte[] getEepromData() throws UnsupportedOperationException;

    public abstract String getSOHValue() throws UnsupportedOperationException;

    public abstract String getChargingCyclesNumber() throws UnsupportedOperationException;

    public abstract String getBatteryCapacity() throws UnsupportedOperationException;

    public abstract String getBatterySupplier() throws UnsupportedOperationException;

    public abstract String getBatteryDesignNo() throws UnsupportedOperationException;

    public abstract String getBatteryDesignLife() throws UnsupportedOperationException;

    public abstract String getBatteryGenerationDate() throws UnsupportedOperationException;

    public abstract String getBatteryTraceNo() throws UnsupportedOperationException;

    public abstract String getBatteryMaterialNumber() throws UnsupportedOperationException;

    public abstract String getBatteryStateCode() throws UnsupportedOperationException;

    public abstract String getBatterySN() throws UnsupportedOperationException;

    public abstract String getBatteryType() throws UnsupportedOperationException;

    public abstract String getBatteryFirstUseTime() throws UnsupportedOperationException;

    public abstract String getOverdischargeNumber() throws UnsupportedOperationException;

    public abstract String getScannerName() throws UnsupportedOperationException;

    public abstract boolean setSubScreenBrightness(String brightness) throws UnsupportedOperationException;

    public abstract int getSubScreenBrightness() throws UnsupportedOperationException;

    public abstract String getSubUsbState() throws UnsupportedOperationException;

    // Protected methods
    protected void enforceOperation(String file) {
        if (!FileUtil.checkFileExists(file)) {
            throw new UnsupportedOperationException();
        }
    }

    protected String readFromFileOrNull(String file) {
        String value = FileUtil.readFromFile(file);
        if (TextUtils.isEmpty(value)) {
            return null;
        } else {
            return value;
        }
    }

}
