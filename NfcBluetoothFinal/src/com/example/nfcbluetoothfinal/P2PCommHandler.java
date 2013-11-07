package com.example.nfcbluetoothfinal;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.example.nfcbluetoothfinal.BluetoothModule.BluetoothState;
import com.example.nfcbluetoothfinal.util.P2PCommException;

public class P2PCommHandler {
	private static final String TAG = "P2PCommHandler";

	private BluetoothModule bluetooth = null;
	private NfcModule nfc = null;
	
	public P2PCommHandler(Activity activity) throws P2PCommException {
		bluetooth = new BluetoothModule();
		nfc = new NfcModule(activity);
	}
	
	public boolean bluetoothEnabled() {
		return bluetooth.getBluetoothAdapter().isEnabled();
	}

	public void enableBluetooth() {
		bluetooth.enable();
	}

	public void registerBroadcastReceiver(Activity activity, Handler handler) {
		bluetooth.registerBroadcastReceiver(activity, handler);
	}
	
	public void init(Activity activity, Handler handler) {
		nfc.setHandler(handler);
		nfc.setCallbacks(activity);
	}

	public boolean nfcEnabled() {
		return nfc.isEnabled();
	}

	public void processNfcIntent(Intent intent) {
		nfc.processNfcIntent(intent);
	}

	public synchronized BluetoothState getSate() {
		return bluetooth.getState();
	}
	
	public synchronized void start() {
		bluetooth.start();
	}

	public synchronized void stop(Activity activity) {
		bluetooth.unregisterBroadcastReceiver(activity);
		bluetooth.stop();
	}
	
	
	
	
}
