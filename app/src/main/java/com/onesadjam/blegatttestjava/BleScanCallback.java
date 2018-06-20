package com.onesadjam.blegatttestjava;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

public class BleScanCallback extends ScanCallback {
    private MainActivity _Activity;
    public BleScanCallback(MainActivity activity)
    {
        _Activity = activity;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result)
    {
        super.onScanResult(callbackType, result);

        _Activity.onBeaconFound(result);
    }
}
