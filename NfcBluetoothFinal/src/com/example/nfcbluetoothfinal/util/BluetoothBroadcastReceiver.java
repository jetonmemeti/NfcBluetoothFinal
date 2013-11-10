package com.example.nfcbluetoothfinal.util;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.example.nfcbluetoothfinal.util.Messages;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
	private Handler handler;
	
	public BluetoothBroadcastReceiver(Handler handler) {
		this.handler = handler;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			if (intent.getExtras().containsKey(BluetoothAdapter.EXTRA_STATE)) {
				int stateInt = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
				if (stateInt == BluetoothAdapter.STATE_ON) {
			        handler.obtainMessage(Messages.BLUETOOTH_ENABLED).sendToTarget();
				} else if (stateInt == BluetoothAdapter.STATE_TURNING_OFF) {
					handler.obtainMessage(Messages.BLUETOOTH_TURNED_OFF).sendToTarget();
				}
			}
		}
	}

}
