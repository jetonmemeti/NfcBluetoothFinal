package com.example.nfcbluetoothfinal.util;

import java.io.Serializable;

import android.util.Log;

import com.example.nfcbluetoothfinal.BluetoothModule;

public class SellerRole implements PaymentRole, Serializable {
	private static final long serialVersionUID = 9102988167134534030L;
	
	//TODO jeton: make enum!
	public static final int STATE_START = 0;
	public static final int STATE_PAYMENT_REQUEST_SENT = 1;
	public static final int STATE_WAIT_FOR_PAYMENT_CONFIRMATION = 2;
	public static final int STATE_TRANSACTION_CONFIRMATION_SENT = 3;
	public static final int STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT = 4;
	
	private int state;
	
	public SellerRole() {
		this.state = STATE_START;
	}
	
	@Override
	public synchronized void process(byte[] bytes, BluetoothModule bluetoothModule) {
		String s;
		switch (state) {
		case STATE_START:
			s = "requestPayment(SA, BtcA, TNrs)";
			Log.i("SellerRole", "STATE_START: " + s);
			bluetoothModule.write(s.getBytes());
			state = STATE_PAYMENT_REQUEST_SENT;
			break;
		case STATE_PAYMENT_REQUEST_SENT:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_PAYMENT_REQUEST_SENT: " + s);
			state = STATE_WAIT_FOR_PAYMENT_CONFIRMATION;
			break;
		case STATE_WAIT_FOR_PAYMENT_CONFIRMATION:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_WAIT_FOR_PAYMENT_CONFIRMATION: " + s);
			
			s = "confirmTransaction(C2)";
			bluetoothModule.write(s.getBytes());
			state = STATE_TRANSACTION_CONFIRMATION_SENT;
			break;
		case STATE_TRANSACTION_CONFIRMATION_SENT:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_TRANSACTION_CONFIRMATION_SENT: " + s);
			state = STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT;
			break;
		case STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT: " + s);
			//TODO jeton: finish protocol!! handler.obtainMessage() ?
			break;
		}
	}

}
