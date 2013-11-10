package com.example.nfcbluetoothfinal.util;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

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
			int prevStateInt = intent.getExtras().getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE);
			int stateInt = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
			if (stateInt == BluetoothAdapter.STATE_ON) {
		        handler.obtainMessage(Messages.BLUETOOTH_ENABLED).sendToTarget();
			} else if (stateInt == BluetoothAdapter.STATE_TURNING_OFF && prevStateInt == BluetoothAdapter.STATE_ON) {
				//TODO jeton: does not work!!
				Log.e("BluetoothBroadcastReceiver", "bluetooth turning off recognized");
				handler.obtainMessage(Messages.BLUETOOTH_TURNED_OFF).sendToTarget();
//			} else if (stateInt == BluetoothAdapter.STATE_OFF && prevStateInt == BluetoothAdapter.STATE_TURNING_OFF) {
			} else if (stateInt == BluetoothAdapter.STATE_OFF) {
				Log.e("BluetoothBroadcastReceiver", "bluetooth off");
				if (prevStateInt == BluetoothAdapter.STATE_TURNING_OFF)
					Log.e("BluetoothBroadcastReceiver", "prev state was turning off");
			}
		}
	}

}
