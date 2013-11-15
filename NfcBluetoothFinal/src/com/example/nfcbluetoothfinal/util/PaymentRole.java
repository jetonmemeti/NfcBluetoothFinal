package com.example.nfcbluetoothfinal.util;

import com.example.nfcbluetoothfinal.BluetoothModule;

public interface PaymentRole {
	public void proceed(byte[] bytes, BluetoothModule bluetoothModule);
	
}
