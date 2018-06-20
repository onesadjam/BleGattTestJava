package com.onesadjam.blegatttestjava;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String _MyDeviceAddress = "B0:B0:B0:B0:00:18";
    private BluetoothLeScanner _Scanner;
    private BluetoothManager _Manager;
    private BluetoothAdapter _Adapter;
    private BleScanCallback _ScanCallback;
    private BleGattCallback _GattCallback;
    private BluetoothDevice _Device;
    private TextView _LogTextView;
    private BondStateReceiver _BondStateReceiver;
    private final int MY_PERMISSIONS_REQUEST = 134;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _LogTextView = (TextView)findViewById(R.id.logTextView);
        ValidatePermissions();

        _BondStateReceiver = new BondStateReceiver(this);


        _LogTextView.setText("Started scanning....");
    }

    private void ValidatePermissions() {
        String[] requiredPermissions =
                {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                };
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                _LogTextView.setText("Failed check for permission " + permission + ". Requesting all permissions.\n");
                ActivityCompat.requestPermissions(this, requiredPermissions, MY_PERMISSIONS_REQUEST);
                return;
            }
        }
        StartScanning();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    appendLogText("Permission granted.");
                    StartScanning();
                } else {
                    appendLogText("Permission denied, Abort.");
                }
                return;
            }
        }
    }

    public void StartScanning()
    {
        // Start scanning for the beacon we want to write a value to
        ScanSettings.Builder scanModeBuilder = new ScanSettings.Builder();
        scanModeBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

        ScanFilter.Builder deviceAddressFilterBuilder = new ScanFilter.Builder();
        deviceAddressFilterBuilder.setDeviceAddress(_MyDeviceAddress);

        _Manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        _Adapter = _Manager.getAdapter();
        _Scanner = _Adapter.getBluetoothLeScanner();
        _ScanCallback = new BleScanCallback(this);
        _GattCallback = new BleGattCallback(this);

        List<ScanFilter> scanFilterList = new ArrayList<ScanFilter>();
        scanFilterList.add(deviceAddressFilterBuilder.build());

        _Scanner.startScan(scanFilterList, scanModeBuilder.build(), _ScanCallback);
    }

    public void onBeaconFound(ScanResult scanResult)
    {
        appendLogText("Found BLE device");
        _Scanner.stopScan(_ScanCallback);
        appendLogText("Stopped scanning");
        _Device = scanResult.getDevice();
        if (_Device.getBondState() != BluetoothDevice.BOND_BONDED)
        {
            appendLogText("Bonding...");
            registerReceiver(_BondStateReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
            _Device.createBond();
        }
        else
        {
            _Device.connectGatt(this, false, _GattCallback);
            appendLogText("Connecting...");
        }
    }

    public void onBonded(int bondStatus)
    {
        if (bondStatus == BluetoothDevice.BOND_BONDED)
        {
            appendLogText("Bonded.");
            _Device.connectGatt(this, false, _GattCallback);
            appendLogText("Connecting...");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        _Scanner.stopScan(_ScanCallback);
    }

    public void appendLogText(final String text)
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run()
            {
                _LogTextView.setText(_LogTextView.getText() + "\n" + text);
            } });
    }
}
