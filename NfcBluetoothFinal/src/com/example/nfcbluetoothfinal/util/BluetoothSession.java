package com.example.nfcbluetoothfinal.util;

import com.example.nfcbluetoothfinal.BluetoothModule;


//TODO jeton: add crypto things here
public class BluetoothSession {
	private BluetoothSessionInfos infos;
	private PaymentRole role;
	
	public BluetoothSession(boolean asSeller) {
		infos = new BluetoothSessionInfos();
		role = (asSeller) ? new SellerRole() : new BuyerRole();
	}
	
	public BluetoothSessionInfos getSessionInfos() {
		return infos;
	}
	
	public void setInfos(BluetoothSessionInfos infos) {
		this.infos = infos;
	}
	
	public void proceed(byte[] bytes, BluetoothModule bluetoothModule) {
		role.proceed(bytes, bluetoothModule);
	}

}
