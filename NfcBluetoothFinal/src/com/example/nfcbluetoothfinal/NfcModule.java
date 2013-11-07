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
import com.example.nfcbluetoothfinal.util.P2PCommException;

public class NfcModule implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private static final String TAG = "NfcModule";
	
	private NfcAdapter adapter = null;
	private Handler handler = null;
	
	public NfcModule(Activity activity) throws P2PCommException {
		adapter = NfcAdapter.getDefaultAdapter(activity);
		if (adapter == null)
			throw new P2PCommException(Messages.ERROR_NO_NFC);
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public boolean isEnabled() {
		return adapter.isEnabled();
	}
	
	public void setCallbacks(Activity activity) {
		Log.e(TAG, "setting callbacks");
		// Register callback to set NDEF message
		adapter.setNdefPushMessageCallback(this, activity);
		// Register callback to listen for message-sent success
		adapter.setOnNdefPushCompleteCallback(this, activity);
	}
	
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		if (bluetooth == null || !bluetooth.isEnabled()) {
			Log.e(TAG, "bluetooth null or not enabled");
		}
		
		NdefMessage msg = null;
		
		byte[] bytes = bluetooth.getAddress().getBytes();
		msg = new NdefMessage(new NdefRecord[] { createMimeRecord(
				"application/com.example.nfcbluetooth", bytes) });
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
		Log.e(TAG, "ndef push complete");
	}

	public void processNfcIntent(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        
        //TODO jeton: replace with private
        
        try {
        	String remoteDeviceAddress = new String(msg.getRecords()[0].getPayload());
        	handler.obtainMessage(Messages.NFC_INTENT_PROCESSED, remoteDeviceAddress).sendToTarget();
        } catch (Exception e) {
        	Log.e(TAG, "error while deserializing!!", e);
        	//TODO jeton: do something!
        }
	}

}
