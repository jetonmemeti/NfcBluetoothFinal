package com.example.nfcbluetoothfinal.util;

import java.io.Serializable;

import android.util.Log;

import com.example.nfcbluetoothfinal.BluetoothModule;

public class SellerRole implements PaymentRole, Serializable {
	private static final long serialVersionUID = 9102988167134534030L;
	
	private enum State {
		STATE_START,
		STATE_PAYMENT_REQUEST_SENT,
		STATE_WAIT_FOR_PAYMENT_CONFIRMATION,
		STATE_TRANSACTION_CONFIRMATION_SENT,
		STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT,
		STATE_END;
	}
	
	private State state;
	
	public SellerRole() {
		this.state = State.STATE_START;
	}
	
	@Override
	public synchronized void process(byte[] bytes, BluetoothModule bluetoothModule) {
		String s;
		switch (state) {
		case STATE_START:
			s = "requestPayment(SA, BtcA, TNrs)";
			Log.i("SellerRole", "STATE_START: " + s);
			bluetoothModule.write(s.getBytes());
			state = State.STATE_PAYMENT_REQUEST_SENT;
			break;
		case STATE_PAYMENT_REQUEST_SENT:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_PAYMENT_REQUEST_SENT: " + s);
			state = State.STATE_WAIT_FOR_PAYMENT_CONFIRMATION;
			break;
		case STATE_WAIT_FOR_PAYMENT_CONFIRMATION:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_WAIT_FOR_PAYMENT_CONFIRMATION: " + s);
			
			s = "confirmTransaction(C2)";
			bluetoothModule.write(s.getBytes());
			state = State.STATE_TRANSACTION_CONFIRMATION_SENT;
			break;
		case STATE_TRANSACTION_CONFIRMATION_SENT:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_TRANSACTION_CONFIRMATION_SENT: " + s);
			state = State.STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT;
			break;
		case STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT:
			s = new String(bytes);
			Log.i("SellerRole", "STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT: " + s);
			
			bluetoothModule.getHandler().obtainMessage(Messages.P2P_PROTOCOL_FINISHED).sendToTarget();
			state = State.STATE_END;
			break;
		case STATE_END:
			Log.i("SellerRole", "STATE_END");
			//do nothing
			break;
		}
	}

}
