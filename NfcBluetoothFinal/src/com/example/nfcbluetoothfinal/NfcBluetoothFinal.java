package com.example.nfcbluetoothfinal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Menu;
import android.widget.Toast;

import com.example.nfcbluetoothfinal.BluetoothModule.BluetoothState;
import com.example.nfcbluetoothfinal.util.BluetoothBroadcastReceiver;
import com.example.nfcbluetoothfinal.util.BluetoothSessionInfos;
import com.example.nfcbluetoothfinal.util.Messages;

public class NfcBluetoothFinal extends Activity {
	private BluetoothAdapter bluetoothAdapter = null;
	private NfcAdapter nfcAdapter = null;
	private BluetoothBroadcastReceiver broadcastReceiver = null;
	private boolean broadcastReceiverRegistered = false;
	
	private BluetoothModule bluetoothModule = null;
	private NfcModule nfcModule = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, Messages.ERROR_NO_BLUETOOTH, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			Toast.makeText(this, Messages.ERROR_NO_BLUETOOTH, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (!nfcAdapter.isEnabled()) {
			//prompt dialog to enable nfc
			Toast.makeText(this, Messages.ACTIVATE_NFC, Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
		} else if (!bluetoothAdapter.isEnabled()) {
			//enable bluetooth programatically
			registerBroadcastReceiver(handler);
			bluetoothAdapter.enable();
		} else {
			registerBroadcastReceiver(handler);
			initBluetooth();
			initNfc();
		}
	}
	
	private void registerBroadcastReceiver(Handler handler) {
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		broadcastReceiver = new BluetoothBroadcastReceiver(handler);
		this.registerReceiver(broadcastReceiver, filter);
		broadcastReceiverRegistered = true;
    }
	
	private void unregisterBroadcastReceiver() {
		if (broadcastReceiverRegistered) {
			this.unregisterReceiver(broadcastReceiver);
		}
	}	
	private void initBluetooth() {
		bluetoothModule = new BluetoothModule(bluetoothAdapter, handler);
	}
	
	private void initNfc() {
		nfcModule = new NfcModule(this, nfcAdapter, handler);
	}
	
	@Override
	public synchronized void onResume() {
		super.onResume();
		
		if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			if (nfcModule != null)
				nfcModule.processNfcIntent(getIntent());
		}
	}
	
	private void startListeningBluetooth() {
		if (bluetoothModule != null) {
			if (bluetoothModule.getSate() == BluetoothState.STATE_NONE) {
				bluetoothModule.startListening();
			}
		}
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}
	
	@Override
	public synchronized void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		//TODO jeton: call on destroy?
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (bluetoothModule != null)
			bluetoothModule.stop();
		
		unregisterBroadcastReceiver();
		
		if (bluetoothAdapter.isEnabled())
			bluetoothAdapter.disable();
	}
	
	private void stopBluetoothModule() {
		if (bluetoothModule != null)
			bluetoothModule.stop();
	}
	
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Messages.BLUETOOTH_ENABLED:
				Toast.makeText(getApplicationContext(), "turned bluetooth on", Toast.LENGTH_SHORT).show();
            	if (bluetoothModule == null)
                	initBluetooth();
            	initNfc();
            	break;
			case Messages.NFC_INTENT_PROCESSED:
				BluetoothSessionInfos infos = (BluetoothSessionInfos) msg.obj;
				bluetoothModule.setSessionInfos(infos);
				bluetoothModule.connect();
				break;
			case Messages.NFC_PUSH_COMPLETE:
				Boolean success = (Boolean) msg.obj;
				if (success) {
					startListeningBluetooth();
					break;
				}
			case Messages.NFC_ERROR_PROCESSING_INFOS:
				Toast.makeText(getApplicationContext(), Messages.ERROR_NFC, Toast.LENGTH_LONG).show();
				break;
			case Messages.BLUETOOTH_CONNECTION_ESTABLISHED:
				//actually no need to do anything
				break;
			case Messages.BLUETOOTH_TURNED_OFF:
				Toast.makeText(getApplicationContext(), Messages.TURNED_BLUETOOTH_OFF, Toast.LENGTH_LONG).show();
				stopBluetoothModule();
				break;
			case Messages.BLUETOOTH_CONNECTION_FAILED:
				Toast.makeText(getApplicationContext(), Messages.ERROR_BLUETOOTH_CONNECTION_FAILED, Toast.LENGTH_LONG).show();
				stopBluetoothModule();
				break;
			case Messages.BLUETOOTH_CONNECTION_LOST:
				Toast.makeText(getApplicationContext(), Messages.ERROR_BLUETOOTH_CONNECTION_LOST, Toast.LENGTH_LONG).show();
				stopBluetoothModule();
				break;
			case Messages.BLUETOOTH_STATE_CHANGED:
				switch ((BluetoothState) msg.obj) {
				case STATE_NONE:
				case STATE_LISTENING:
				case STATE_CONNECTING:
					break;
				case STATE_CONNECTED:
					//TODO jeton: start protocol!
					bluetoothModule.processProtocol(null);
					//TODO jeton: on fail or abort finish protocol!!
					break;
				}
				break;
			case Messages.P2P_PROTOCOL_MESSAGE:
				byte[] bytes = (byte[]) msg.obj;
				bluetoothModule.processProtocol(bytes);
			}
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
