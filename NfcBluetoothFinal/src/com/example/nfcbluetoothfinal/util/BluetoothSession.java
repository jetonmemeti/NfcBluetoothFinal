package com.example.nfcbluetoothfinal.util;

import com.example.nfcbluetoothfinal.BluetoothModule;


//TODO jeton: add crypto things here
public class BluetoothSession {
	private BluetoothSessionInfos infos;
	private PaymentRole role;
	private boolean finished = false;
	
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
	
	public void setFinished() {
		finished = true;
	}
	
	public boolean isFinished() {
		return finished;
	}

	public void process(byte[] bytes, BluetoothModule bluetoothModule) {
		role.process(bytes, bluetoothModule);
	}

}
