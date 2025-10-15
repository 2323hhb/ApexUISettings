package com.apex.UISettings.vsr;

import com.apex.UISettings.utils.Logger;

/**
 * @description
 * @author ch
 * @date 2024/11/29
 * @edit
 */
public class VSRManager extends VendorSoftwareReq {

    private static final Logger logger = Logger.getLogger(VSRManager.class);

    // 单例实现（对应 Kotlin object）
    private static final VSRManager INSTANCE = new VSRManager();

    private VSRManager() {}

    public static VSRManager getInstance() {
        return INSTANCE;
    }

    // 懒加载 RecommendVSR / DeprecatedVSR
    private VendorSoftwareReq recommendVSR;
    private VendorSoftwareReq deprecatedVSR;

    private VendorSoftwareReq getRecommendVSR() {
        if (recommendVSR == null) {
            recommendVSR = new RecommendVSR();
        }
        return recommendVSR;
    }

    private VendorSoftwareReq getDeprecatedVSR() {
        if (deprecatedVSR == null) {
            deprecatedVSR = new DeprecatedVSR();
        }
        return deprecatedVSR;
    }

    private <T> T execute(Executable<T> first, Executable<T> second, T defValue) {
        try {
            return first.execute();
        } catch (UnsupportedOperationException e) {
            logger.w("execute vsr first failed, retry to execute second.");
            try {
                return second.execute();
            } catch (Exception e2) {
                logger.w("execute vsr second failed, return default value");
                return defValue;
            }
        } catch (Exception e) {
            return defValue;
        }
    }

    @FunctionalInterface
    private interface Executable<T> {
        T execute();
    }

    @Override
    public boolean openDrawer() {
        return execute(
                () -> getRecommendVSR().openDrawer(),
                () -> getDeprecatedVSR().openDrawer(),
                false
        );
    }

    @Override
    public boolean setEthPower(boolean up) {
        return execute(
                () -> getRecommendVSR().setEthPower(up),
                () -> getDeprecatedVSR().setEthPower(up),
                false
        );
    }

    @Override
    public String getBatteryCoinVol() {
        return execute(
                () -> getRecommendVSR().getBatteryCoinVol(),
                () -> getDeprecatedVSR().getBatteryCoinVol(),
                ""
        );
    }

    @Override
    public boolean setChargeEnable(boolean enable) {
        return execute(
                () -> getRecommendVSR().setChargeEnable(enable),
                () -> getDeprecatedVSR().setChargeEnable(enable),
                false
        );
    }

    @Override
    public String getTpVersion() {
        return execute(
                () -> getRecommendVSR().getTpVersion(),
                () -> getDeprecatedVSR().getTpVersion(),
                ""
        );
    }

    @Override
    public String getLcdVersion() {
        return execute(
                () -> getRecommendVSR().getLcdVersion(),
                () -> getDeprecatedVSR().getLcdVersion(),
                ""
        );
    }

    @Override
    public boolean setFlashLightControlEnable(boolean enable) {
        return execute(
                () -> getRecommendVSR().setFlashLightControlEnable(enable),
                () -> getDeprecatedVSR().setFlashLightControlEnable(enable),
                false
        );
    }

    @Override
    public boolean setFlashLightState(boolean turnOn) {
        return execute(
                () -> getRecommendVSR().setFlashLightState(turnOn),
                () -> getDeprecatedVSR().setFlashLightState(turnOn),
                false
        );
    }

    @Override
    public String getEepromCRC() {
        return execute(
                () -> getRecommendVSR().getEepromCRC(),
                () -> getDeprecatedVSR().getEepromCRC(),
                ""
        );
    }

    @Override
    public String getEepromSize() {
        return execute(
                () -> getRecommendVSR().getEepromSize(),
                () -> getDeprecatedVSR().getEepromSize(),
                ""
        );
    }

    @Override
    public String getEepromVersion() {
        return execute(
                () -> getRecommendVSR().getEepromVersion(),
                () -> getDeprecatedVSR().getEepromVersion(),
                ""
        );
    }

    @Override
    public byte[] getEepromData() {
        return execute(
                () -> getRecommendVSR().getEepromData(),
                () -> getDeprecatedVSR().getEepromData(),
                new byte[0]
        );
    }

    @Override
    public String getSOHValue() {
        return execute(
                () -> getRecommendVSR().getSOHValue(),
                () -> getDeprecatedVSR().getSOHValue(),
                ""
        );
    }

    @Override
    public String getChargingCyclesNumber() {
        return execute(
                () -> getRecommendVSR().getChargingCyclesNumber(),
                () -> getDeprecatedVSR().getChargingCyclesNumber(),
                ""
        );
    }

    @Override
    public String getBatteryCapacity() {
        return execute(
                () -> getRecommendVSR().getBatteryCapacity(),
                () -> getDeprecatedVSR().getBatteryCapacity(),
                ""
        );
    }

    @Override
    public String getBatterySupplier() {
        return execute(
                () -> getRecommendVSR().getBatterySupplier(),
                () -> getDeprecatedVSR().getBatterySupplier(),
                ""
        );
    }

    @Override
    public String getBatteryDesignNo() {
        return execute(
                () -> getRecommendVSR().getBatteryDesignNo(),
                () -> getDeprecatedVSR().getBatteryDesignNo(),
                ""
        );
    }

    @Override
    public String getBatteryDesignLife() {
        return execute(
                () -> getRecommendVSR().getBatteryDesignLife(),
                () -> getDeprecatedVSR().getBatteryDesignLife(),
                ""
        );
    }

    @Override
    public String getBatteryGenerationDate() {
        return execute(
                () -> getRecommendVSR().getBatteryGenerationDate(),
                () -> getDeprecatedVSR().getBatteryGenerationDate(),
                ""
        );
    }

    @Override
    public String getBatteryTraceNo() {
        return execute(
                () -> getRecommendVSR().getBatteryTraceNo(),
                () -> getDeprecatedVSR().getBatteryTraceNo(),
                ""
        );
    }

    @Override
    public String getBatteryMaterialNumber() {
        return execute(
                () -> getRecommendVSR().getBatteryMaterialNumber(),
                () -> getDeprecatedVSR().getBatteryMaterialNumber(),
                ""
        );
    }

    @Override
    public String getBatteryStateCode() {
        return execute(
                () -> getRecommendVSR().getBatteryStateCode(),
                () -> getDeprecatedVSR().getBatteryStateCode(),
                ""
        );
    }

    @Override
    public String getBatterySN() {
        return execute(
                () -> getRecommendVSR().getBatterySN(),
                () -> getDeprecatedVSR().getBatterySN(),
                ""
        );
    }

    @Override
    public String getBatteryType() {
        return execute(
                () -> getRecommendVSR().getBatteryType(),
                () -> getDeprecatedVSR().getBatteryType(),
                ""
        );
    }

    @Override
    public String getBatteryFirstUseTime() {
        return execute(
                () -> getRecommendVSR().getBatteryFirstUseTime(),
                () -> getDeprecatedVSR().getBatteryFirstUseTime(),
                ""
        );
    }

    @Override
    public String getOverdischargeNumber() {
        return execute(
                () -> getRecommendVSR().getOverdischargeNumber(),
                () -> getDeprecatedVSR().getOverdischargeNumber(),
                ""
        );
    }

    @Override
    public String getScannerName() {
        return execute(
                () -> getRecommendVSR().getScannerName(),
                () -> getDeprecatedVSR().getScannerName(),
                ""
        );
    }

    @Override
    public boolean setSubScreenBrightness(String brightness) {
        return execute(
                () -> getRecommendVSR().setSubScreenBrightness(brightness),
                () -> getDeprecatedVSR().setSubScreenBrightness(brightness),
                false
        );
    }

    @Override
    public int getSubScreenBrightness() {
        return execute(
                () -> getRecommendVSR().getSubScreenBrightness(),
                () -> getDeprecatedVSR().getSubScreenBrightness(),
                0
        );
    }

    @Override
    public String getSubUsbState() {
        return execute(
                () -> getRecommendVSR().getSubUsbState(),
                () -> getDeprecatedVSR().getSubUsbState(),
                "0"
        );
    }
}
