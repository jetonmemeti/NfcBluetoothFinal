package com.example.nfcbluetoothfinal.util;

import java.io.Serializable;

import android.util.Log;

import com.example.nfcbluetoothfinal.BluetoothModule;

public class BuyerRole implements PaymentRole, Serializable {
	private static final long serialVersionUID = -6308811188885952621L;

	public static final int STATE_START = 0;
	public static final int STATE_WAIT_FOR_PAYMENT_REQUEST = 1;
	public static final int STATE_PAYMENT_CONFIRMATION_SENT = 2;
	public static final int STATE_WAIT_FOR_TRANSACTION_CONFIRMATION = 3;
	public static final int STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT = 4;
	
	private int state;
	
	public BuyerRole() {
		this.state = STATE_START;
	}
	
	@Override
	public synchronized void process(byte[] bytes, BluetoothModule bluetoothModule) {
		String s;
		switch (state) {
		case STATE_START:
			Log.i("BuyerRole", "STATE_START");
			state = STATE_WAIT_FOR_PAYMENT_REQUEST;
			break;
		case STATE_WAIT_FOR_PAYMENT_REQUEST:
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_WAIT_FOR_PAYMENT_REQUEST: " + s);
			
			s = "confirmPayment(Cb=Eprb(SA;BA;BtcA;TNrs;TNrb), BA)";
			bluetoothModule.write(s.getBytes());
			state = STATE_PAYMENT_CONFIRMATION_SENT;
			break;
		case STATE_PAYMENT_CONFIRMATION_SENT:
			// wait for transaction confirmation
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_PAYMENT_CONFIRMATION_SENT: " + s);
			state = STATE_WAIT_FOR_TRANSACTION_CONFIRMATION;
			break;
		case STATE_WAIT_FOR_TRANSACTION_CONFIRMATION:
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_WAIT_FOR_TRANSACTION_CONFIRMATION: " + s);
			
			s = "ackTransaction()";
			bluetoothModule.write(s.getBytes());
			state = STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT;
			break;
		case STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT:
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT: " + s);
			//TODO jeton: finish protocol!! handler.obtainMessage() ?
			break;
		}
	}

}
