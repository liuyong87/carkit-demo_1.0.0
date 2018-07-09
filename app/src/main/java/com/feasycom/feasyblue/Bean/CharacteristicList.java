package com.feasycom.feasyblue.Bean;


import android.bluetooth.BluetoothGattCharacteristic;

import java.util.List;

public class CharacteristicList {
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList;

    public CharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList) {
        this.bluetoothGattCharacteristicList = bluetoothGattCharacteristicList;
    }

    public List<BluetoothGattCharacteristic> getBluetoothGattCharacteristicList() {
        return bluetoothGattCharacteristicList;
    }

    public void setBluetoothGattCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList) {
        this.bluetoothGattCharacteristicList = bluetoothGattCharacteristicList;
    }

    public BluetoothGattCharacteristic getItem(int position) {
       return  bluetoothGattCharacteristicList.get(position);
    }

    public int getCount() {
        return bluetoothGattCharacteristicList.size();
    }
}
