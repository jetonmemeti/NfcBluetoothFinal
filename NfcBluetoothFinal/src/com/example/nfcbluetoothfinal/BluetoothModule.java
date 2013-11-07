package com.example.nfcbluetoothfinal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.os.Handler;

import com.example.nfcbluetoothfinal.util.BluetoothBroadcastReceiver;
import com.example.nfcbluetoothfinal.util.Messages;
import com.example.nfcbluetoothfinal.util.P2PCommException;

public class BluetoothModule {
	
	private BluetoothAdapter adapter = null;
	private BluetoothBroadcastReceiver broadcastReceiver = null;
	private BluetoothState state;
	
	public enum BluetoothState {
		STATE_NONE
	}

	public BluetoothModule() throws P2PCommException {
		adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null)
			throw new P2PCommException(Messages.ERROR_NO_BLUETOOTH);
		state = BluetoothState.STATE_NONE;
	}
	
	public boolean isEnabled() {
		return adapter.isEnabled();
	}
	
	public BluetoothAdapter getBluetoothAdapter() {
		return adapter;
	}

	public void enable() {
		adapter.enable();
	}

	public void registerBroadcastReceiver(Activity activity, Handler handler) {
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		broadcastReceiver = new BluetoothBroadcastReceiver(handler);
		activity.registerReceiver(broadcastReceiver, filter);
	}

	public void unregisterBroadcastReceiver(Activity activity) {
		activity.unregisterReceiver(broadcastReceiver);
	}

	public synchronized BluetoothState getState() {
		return state;
	}

	public synchronized void start() {
		// TODO Auto-generated method stub
		
	}
	
	public synchronized void stop() {
		// TODO Auto-generated method stub
		
	}

}
