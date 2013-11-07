package com.example.nfcbluetoothfinal;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.example.nfcbluetoothfinal.BluetoothModule.BluetoothState;
import com.example.nfcbluetoothfinal.util.Messages;
import com.example.nfcbluetoothfinal.util.P2PCommException;

public class NfcBluetoothFinal extends Activity {
	private static final String TAG = "NfcBluetoothFinal";
	
	private P2PCommHandler commHandler = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.e(TAG, "+++ ON CREATE +++");
		
		try {
			commHandler = new P2PCommHandler(this);
		} catch (P2PCommException e) {
			Log.e(TAG, e.getMessage());
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.e(TAG, "++ ON START ++");
		
		if (commHandler == null) {
			Log.e(TAG, "commhandler is null and needs to be re-instanciated");
			setupCommHandler();
		}
		
		if (!commHandler.nfcEnabled()) {
			Toast.makeText(this, Messages.ACTIVATE_NFC, Toast.LENGTH_LONG).show();
	        startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
		} else if (!commHandler.bluetoothEnabled()) {
			commHandler.registerBroadcastReceiver(this, handler);
			commHandler.enableBluetooth();
		} else {
			setupCommHandler();
			commHandler.init(this, handler);
		}
	}

	private void setupCommHandler() {
		try  {
			commHandler = new P2PCommHandler(this);
			commHandler.registerBroadcastReceiver(this, handler);
		} catch (P2PCommException e) {
			//not thrown here, would be catched in onCreate()
		}
	}
	
	@Override
	public synchronized void onResume() {
		super.onResume();
		Log.e(TAG, "+ ON RESUME +");
		
		if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			if (commHandler != null)
				commHandler.processNfcIntent(getIntent());
		}
		
		start();
	}

	private void start() {
		if (commHandler != null) {
			if (commHandler.getSate() == BluetoothState.STATE_NONE) {
				commHandler.start();
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
        
        if (commHandler != null)
        	commHandler.stop(this);
    }

	private final Handler handler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Messages.BLUETOOTH_ENABLED:
				Toast.makeText(getApplicationContext(), "turned bluetooth on", Toast.LENGTH_SHORT).show();
            	Log.e(TAG, "received broadcast intent");
//            	if (mChatService == null)
//                	setupChat();
//            	startService();
//            	initNfc();
            	break;
			case Messages.NFC_INTENT_PROCESSED:
				Log.e(TAG, "handler received nfc intent --> ready to start bluetooth connection");
				String deviceAddress = (String) msg.obj;
//				BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
//				connectDevice(remoteDevice, false);
				break;
			case Messages.NFC_PUSH_COMPLETE:
            	Log.e(TAG, "handler received: nfc push complete");
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
