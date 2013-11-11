package com.example.nfcbluetoothfinal.util;

import java.io.Serializable;

import android.util.Log;

import com.example.nfcbluetoothfinal.BluetoothModule;

public class BuyerRole implements PaymentRole, Serializable {
	private static final long serialVersionUID = -6308811188885952621L;

	private enum State {
		STATE_START,
		STATE_WAIT_FOR_PAYMENT_REQUEST,
		STATE_PAYMENT_CONFIRMATION_SENT,
		STATE_WAIT_FOR_TRANSACTION_CONFIRMATION,
		STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT,
		STATE_END;
	}
	
	private State state; 
	
	public BuyerRole() {
		this.state = State.STATE_START;
	}
	
	@Override
	public synchronized void process(byte[] bytes, BluetoothModule bluetoothModule) {
		String s;
		switch (state) {
		case STATE_START:
			Log.i("BuyerRole", "STATE_START");
			state = State.STATE_WAIT_FOR_PAYMENT_REQUEST;
			break;
		case STATE_WAIT_FOR_PAYMENT_REQUEST:
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_WAIT_FOR_PAYMENT_REQUEST: " + s);
			
			s = "confirmPayment(Cb=Eprb(SA;BA;BtcA;TNrs;TNrb), BA)";
			bluetoothModule.write(s.getBytes());
			state = State.STATE_PAYMENT_CONFIRMATION_SENT;
			break;
		case STATE_PAYMENT_CONFIRMATION_SENT:
			// wait for transaction confirmation
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_PAYMENT_CONFIRMATION_SENT: " + s);
			state = State.STATE_WAIT_FOR_TRANSACTION_CONFIRMATION;
			break;
		case STATE_WAIT_FOR_TRANSACTION_CONFIRMATION:
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_WAIT_FOR_TRANSACTION_CONFIRMATION: " + s);
			
			s = "ackTransaction()";
			bluetoothModule.write(s.getBytes());
			state = State.STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT;
			break;
		case STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT:
			s = new String(bytes);
			Log.i("BuyerRole", "STATE_TRANSACTION_ACKNOWLEDGEMENT_SENT: " + s);
			
			bluetoothModule.getHandler().obtainMessage(Messages.P2P_PROTOCOL_FINISHED).sendToTarget();
			state = State.STATE_END;
			break;
		case STATE_END:
			//do nothing
			break;
		}
	}

}
