package com.onesadjam.blegatttestjava;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.util.UUID;

public class BleGattCallback extends BluetoothGattCallback {
    static final UUID _DescriptorName = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
    private String _ConfigurationServiceUuid = "3052c6a5-0928-a48f-8d40-4375aaa9a55e";
    private String _ConfigurationKeyCharacteristicUuid = "2dcb38cd-f329-49aa-6749-6d6e6bb8c6b2";
    private String _ConfigurationValue = "02D015424321C334A48CFEAF86109C4F";
    private UUID _ConfigurationKeyCharacteristic = UUID.fromString(_ConfigurationKeyCharacteristicUuid);
    private UUID _ConfigurationService = UUID.fromString(_ConfigurationServiceUuid);
    private MainActivity _MainActivity;
    private BluetoothGattCharacteristic _ConfigurationCharacteristic;

    public BleGattCallback(MainActivity activity)
    {
        _MainActivity = activity;
    }

    @Override
    public void onCharacteristicWrite(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
			int status)
    {
        super.onCharacteristicWrite(gatt, characteristic, status);

        _MainActivity.appendLogText(status != BluetoothGatt.GATT_SUCCESS
                ? "OnCharacteristicWrite: Write failed."
                : "OnCharacteristicWrite: Write succeded.");
        gatt.disconnect();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
        super.onConnectionStateChange(gatt, status, newState);

        switch (newState)
        {
            case BluetoothProfile.STATE_DISCONNECTED:
                _MainActivity.appendLogText("Disconnected");
                gatt.close();
                break;
            case BluetoothProfile.STATE_CONNECTED:
                _MainActivity.appendLogText("Connected");
                gatt.discoverServices();
                break;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
        if (status != BluetoothGatt.GATT_SUCCESS)
        {
            _MainActivity.appendLogText("Failed to discover services");
            gatt.disconnect();
            return;
        }
        _MainActivity.appendLogText("Services Discovered");

        BluetoothGattService configurationService = gatt.getService(_ConfigurationService);
        if (configurationService == null)
        {
            _MainActivity.appendLogText("Unable to find configuration service");
            gatt.disconnect();
            return;
        }

        _ConfigurationCharacteristic = configurationService.getCharacteristic(_ConfigurationKeyCharacteristic);
        if (_ConfigurationCharacteristic == null)
        {
            _MainActivity.appendLogText("Unable to find characteristic");
            gatt.disconnect();
            return;
        }

        if ((_ConfigurationCharacteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) != 0)
        {
            _MainActivity.appendLogText("Value is readable; fetching value");
            gatt.readCharacteristic(_ConfigurationCharacteristic);
        }
        else
        {
            BluetoothGattDescriptor name = _ConfigurationCharacteristic.getDescriptor(_DescriptorName);
            if (name != null)
            {
                _MainActivity.appendLogText("Value is not readable, fetching name");
                gatt.readDescriptor(name);
            }
            else
            {
                _MainActivity.appendLogText("Value is not readable and unable to find name descriptor. Abort.");
            }
        }
    }

    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            _MainActivity.appendLogText("Read descriptor name: " + new String(descriptor.getValue()));
            byte[] configKeyBytes = new byte[0];
            try {
                configKeyBytes = StringToByteArrayFastest(_ConfigurationValue);
            } catch (Exception err)
            {
                _MainActivity.appendLogText("Failed to get bytes from configuration value");
            }

            _ConfigurationCharacteristic.setValue(configKeyBytes);
            if (!gatt.writeCharacteristic(_ConfigurationCharacteristic))
            {
                _MainActivity.appendLogText("Failed to write characteristic");
                gatt.disconnect();
            }
            else
            {
                _MainActivity.appendLogText("Writing characteristic...");
            }
        }
        else
        {
            if (status == 137)
            {
                _MainActivity.appendLogText("Failed to read descriptor, trying again");
                gatt.readDescriptor(descriptor);
            }
            else {
                _MainActivity.appendLogText("Failed to read descriptor, giving up.");
                gatt.disconnect();
            }
        }
    }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            _MainActivity.appendLogText("Read characteristic value: " + new String(characteristic.getValue()));
            byte[] configKeyBytes = new byte[0];
            try {
                configKeyBytes = StringToByteArrayFastest(_ConfigurationValue);
            } catch (Exception err)
            {
                _MainActivity.appendLogText("Failed to read characteristic value");
            }

            _ConfigurationCharacteristic.setValue(configKeyBytes);
            if (!gatt.writeCharacteristic(_ConfigurationCharacteristic))
            {
                _MainActivity.appendLogText("Failed to write characteristic");
                gatt.disconnect();
            }
            else
            {
                _MainActivity.appendLogText("Writing characteristic...");
            }
        }
    }

    private byte[] StringToByteArrayFastest(String hex) throws Exception
    {
        if (hex.length() % 2 == 1)
            throw new Exception("The binary key cannot have an odd number of digits");

        byte[] arr = new byte[hex.length() >> 1];

        for (int i = 0; i < hex.length() >> 1; ++i)
        {
            arr[i] = (byte) ((GetHexVal(hex.charAt(i << 1)) << 4) + (GetHexVal(hex.charAt((i << 1) + 1))));
        }

        return arr;
    }

    private int GetHexVal(char hex)
    {
        int val = (int) hex;
        //For uppercase A-F letters:
        // return val - (val < 58 ? 48 : 55);
        //For lowercase a-f letters:
        return val - (val < 58 ? 48 : 87);
        //Or the two combined, but a bit slower:
        //return val - (val < 58 ? 48 : (val < 97 ? 55 : 87));
    }
}
