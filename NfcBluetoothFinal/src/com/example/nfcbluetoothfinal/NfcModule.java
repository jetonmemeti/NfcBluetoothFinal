package com.example.nfcbluetoothfinal;

import java.nio.charset.Charset;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;

import com.example.nfcbluetoothfinal.util.Messages;

public class NfcModule implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private static final String TAG = "NfcModule";
	
	private static final String MIME_TYPE = "application/com.example.nfcbluetoothfinal";
	
	private NfcAdapter adapter = null;
	private Handler handler = null;
	
	public NfcModule(Activity activity, NfcAdapter adapter, Handler handler) {
		this.adapter = adapter;
		this.handler = handler;
		
		setCallbacks(activity);
	}

	private void setCallbacks(Activity activity) {
		// Register callback to set NDEF message
		adapter.setNdefPushMessageCallback(this, activity);
		// Register callback to listen for message-sent success
		adapter.setOnNdefPushCompleteCallback(this, activity);
	}
	
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		
		NdefMessage msg = null;
		
		//TODO jeton: replace with bluetoothmessage (needs to have encryption in it)
		
		byte[] bytes = bluetooth.getAddress().getBytes();
		msg = new NdefMessage(new NdefRecord[] { 
				createMimeRecord(MIME_TYPE, bytes)
		});
		return msg;
	}
	
	// Creates a custom MIME type encapsulated in an NDEF record
    private NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
	
	@Override
	public void onNdefPushComplete(NfcEvent event) {
		handler.obtainMessage(Messages.NFC_PUSH_COMPLETE).sendToTarget();
	}

	public void processNfcIntent(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        
        try {
        	String remoteDeviceAddress = new String(msg.getRecords()[0].getPayload());
        	handler.obtainMessage(Messages.NFC_INTENT_PROCESSED, remoteDeviceAddress).sendToTarget();
        } catch (Exception e) {
        	Log.e(TAG, "error while deserializing!!", e);
        	//TODO jeton: do something!
        }
	}

}
