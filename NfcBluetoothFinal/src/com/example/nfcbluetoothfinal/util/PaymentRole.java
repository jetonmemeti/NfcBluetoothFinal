package com.example.nfcbluetoothfinal.util;

import com.example.nfcbluetoothfinal.BluetoothModule;

public interface PaymentRole {
	public void process(byte[] bytes, BluetoothModule bluetoothModule);
	
	public void reset();

}
