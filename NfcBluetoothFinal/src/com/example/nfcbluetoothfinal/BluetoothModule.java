package com.example.nfcbluetoothfinal;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

public class BluetoothModule {
	
	private Handler handler = null;
	private BluetoothAdapter adapter = null;
	private BluetoothState state;
	private boolean broadcastReceiverRegistered = false;
	
	public enum BluetoothState {
		STATE_NONE
	}

	public BluetoothModule(BluetoothAdapter adapter, Handler handler) {
		this.adapter = adapter;
		this.handler = handler;
		state = BluetoothState.STATE_NONE;
	}
	
	public synchronized BluetoothState getSate() {
		return state;
	}
	
	public synchronized void start() {
		// TODO Auto-generated method stub
		
	}
	
	public synchronized void stop() {
		// TODO Auto-generated method stub
		
	}

}
