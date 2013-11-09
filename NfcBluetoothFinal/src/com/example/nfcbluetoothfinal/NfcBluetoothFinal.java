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
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.example.nfcbluetoothfinal.BluetoothModule.BluetoothState;
import com.example.nfcbluetoothfinal.util.BluetoothBroadcastReceiver;
import com.example.nfcbluetoothfinal.util.BluetoothSessionInitiationInformation;
import com.example.nfcbluetoothfinal.util.Messages;

public class NfcBluetoothFinal extends Activity {
	private static final String TAG = "NfcBluetoothFinal";
	
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
		
		Log.e(TAG, "+++ ON CREATE +++");
		
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
		Log.e(TAG, "++ ON START ++");
		
		if (!nfcAdapter.isEnabled()) {
			//prompt dialog to enable nfc
			Toast.makeText(this, Messages.ACTIVATE_NFC, Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
		} else if (!bluetoothAdapter.isEnabled()) {
			//enable bluetooth programatically
			registerBroadcastReceiver(handler);
			bluetoothAdapter.enable();
		} else {
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
		Log.e(TAG, "+ ON RESUME +");
		
		if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			if (nfcModule != null)
				nfcModule.processNfcIntent(getIntent());
		}
		
		start();
	}
	
	/*
	 * TODO jeton: call listen or accept (i know who has to do what!!)
	 */
	private void start() {
		Log.e(TAG, "called start()");
		if (bluetoothModule != null) {
			if (bluetoothModule.getSate() == BluetoothState.STATE_NONE) {
				bluetoothModule.start();
			}
		}
	}
	
	private void start(BluetoothSessionInitiationInformation infos) {
		Log.e(TAG, "called start(info...)");
		if (bluetoothModule != null) {
			if (bluetoothModule.getSate() == BluetoothState.STATE_NONE) {
				bluetoothModule.setSessionInfos(infos);
				bluetoothModule.start();
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
		Log.e(TAG, "- ON PAUSE -");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG, "-- ON STOP --");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "--- ON DESTROY ---");
		
		if (bluetoothModule != null)
			bluetoothModule.stop();
		
		unregisterBroadcastReceiver();
	}
	
	private void connectDevice() {
		Log.e(TAG, "should start now");
		bluetoothModule.connect();
    }
	
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		//TODO jeton: error handling! e.g. what to do after nfc error processing infos?
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Messages.BLUETOOTH_ENABLED:
				Toast.makeText(getApplicationContext(), "turned bluetooth on", Toast.LENGTH_SHORT).show();
            	Log.e(TAG, "received broadcast intent");
            	if (bluetoothModule == null)
                	initBluetooth();
            	start();
            	initNfc();
            	break;
			case Messages.NFC_INTENT_PROCESSED:
				Log.e(TAG, "handler received nfc intent --> ready to start bluetooth connection");
				BluetoothSessionInitiationInformation infos = (BluetoothSessionInitiationInformation) msg.obj;
				Log.e(TAG, "initiator-address: "+infos.getInitiatorDeviceAddress());
				Log.e(TAG, "initiator-name: "+infos.getInitiatorDeviceName());
				Log.e(TAG, "initiator-uuid: "+infos.getServiceUUID());
				start(infos);
				connectDevice();
				break;
			case Messages.NFC_PUSH_COMPLETE:
            	Log.e(TAG, "handler received: nfc push complete");
            	break;
			case Messages.NFC_ERROR_PROCESSING_INFOS:
				Log.e(TAG, "handler received: error processing nfc message!!");
				break;
			case Messages.BLUETOOTH_CONNECTION_ESTABLISHED:
				Log.e(TAG, "handler received: bluetooth connection successfully established");
				break;
			case Messages.BLUETOOTH_CONNECTION_FAILED:
				Log.e(TAG, "handler received: bluetooth connection failed");
				break;
			case Messages.BLUETOOTH_CONNECTION_LOST:
				Log.e(TAG, "handler received: bluetooth connection lost");
				break;
			case Messages.BLUETOOTH_STATE_CHANGED:
				switch ((BluetoothState) msg.obj) {
				case STATE_NONE:
					Log.e(TAG, "handler received: bluetooth state: NONE");
					break;
				case STATE_LISTENING:
					Log.e(TAG, "handler received: bluetooth state: LISTENING");
					break;
				case STATE_CONNECTING:
					Log.e(TAG, "handler received: bluetooth state: CONNECTING");
					break;
				case STATE_CONNECTED:
					Log.e(TAG, "handler received: bluetooth state: CONNECTED");
					break;
				}
				break;
			case Messages.BLUETOOTH_MESSAGE_RECEIVED:
				//TODO
				break;
			case Messages.BLUETOOTH_MESSAGE_SEND:
				//TODO
				break;
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
