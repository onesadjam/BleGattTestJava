package com.onesadjam.blegatttestjava;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BondStateReceiver extends BroadcastReceiver {

    private MainActivity _MainActivity;

    public BondStateReceiver(MainActivity activity) {
        _MainActivity = activity;
    }

    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", BluetoothDevice.BOND_NONE);
        if (status == BluetoothDevice.BOND_BONDING) return; // Ignore bondingin in process

        _MainActivity.onBonded(status);
        context.unregisterReceiver(this);
    }
}
