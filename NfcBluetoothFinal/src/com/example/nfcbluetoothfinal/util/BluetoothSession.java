package com.example.nfcbluetoothfinal.util;

import java.util.UUID;

//TODO jeton: add crypto things here
public class BluetoothSession {
	
	private static final String SERVICE_NAME = "com.example.nfcbluetoothfinal";

	private UUID serviceUUID;
	
	public BluetoothSession() {
		serviceUUID = UUID.randomUUID();
	}
	
	public UUID getServiceUUID() {
		return serviceUUID;
	}
	
	public String getServiceName() {
		return SERVICE_NAME;
	}

}
