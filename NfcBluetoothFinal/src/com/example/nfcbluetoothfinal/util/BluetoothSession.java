package com.example.nfcbluetoothfinal.util;


//TODO jeton: add crypto things here
public class BluetoothSession {
	private BluetoothSessionInfos infos = new BluetoothSessionInfos();
	private PaymentRole role;
	
	public BluetoothSession(boolean asSeller) {
		if (asSeller)
			role = new SellerRole();
		else
			role = new BuyerRole();
	}
	
	public BluetoothSessionInfos getSessionInfos() {
		return infos;
	}
	
	public PaymentRole getPaymentRole() {
		return role;
	}

	public void setInfos(BluetoothSessionInfos infos) {
		this.infos = infos;
	}

}
