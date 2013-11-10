package com.example.nfcbluetoothfinal;

import java.io.IOException;
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

import com.example.nfcbluetoothfinal.util.BluetoothSession;
import com.example.nfcbluetoothfinal.util.Messages;

public class NfcModule implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private static final String TAG = "NfcModule";
	
	/**
	 * If changed it needs to be adopted in the manifest file as well!!
	 */
	private static final String MIME_TYPE = "application/com.example.nfcbluetoothfinal";
	
	private static final String ERROR_MSG = "error occured";
	
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
		BluetoothSession infos = new BluetoothSession(bluetooth.getAddress(), bluetooth.getName());
		NdefMessage msg = null;
		
		byte[] bytes;
		try {
			bytes = BluetoothSession.serialize(infos);
			msg = new NdefMessage(new NdefRecord[] { 
					createMimeRecord(MIME_TYPE, bytes)
			});
		} catch (IOException e) {
			Log.e(TAG, "could not serialize object");
			bytes = ERROR_MSG.getBytes();
			msg = new NdefMessage(new NdefRecord[] { 
					createMimeRecord(MIME_TYPE, bytes)
			});
		}
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
        
    	byte[] bytes = msg.getRecords()[0].getPayload();
    	BluetoothSession infos;
        	
    	try {
    		infos = BluetoothSession.deserialize(bytes);
    		handler.obtainMessage(Messages.NFC_INTENT_PROCESSED, infos).sendToTarget();
    	} catch (Exception e) {
    		Log.e(TAG, "error while deserializing!!", e);
    		
			String errorMsg = new String(bytes);
			if (errorMsg.equals(ERROR_MSG)) {
				//error occured on the sender
			}
    		
			//TODO jeton: reset all when this goes wrong! on ndef push complete?
			
    		handler.obtainMessage(Messages.NFC_ERROR_PROCESSING_INFOS).sendToTarget();
    	}
	}

}
