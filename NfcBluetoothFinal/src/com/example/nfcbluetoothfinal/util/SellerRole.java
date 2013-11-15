package com.example.nfcbluetoothfinal.util;

import java.io.Serializable;

import android.util.Log;

import com.example.nfcbluetoothfinal.BluetoothModule;

public class SellerRole implements PaymentRole, Serializable {
	private static final long serialVersionUID = 9102988167134534030L;
	
	private static final String ROLE_TEXT = "seller";
	
	private static final String TAG = "SellerRole";
	
	private enum State {
		STATE_START,
		STATE_PAYMENT_ROLE_SENT,
		STATE_WAIT_FOR_PAYMENT_ROLE,
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
	
	public static String getRoleID() {
		return ROLE_TEXT;
	}

	//TODO jeton: delete log output
	@Override
	public synchronized void process(byte[] bytes, BluetoothModule bluetoothModule) {
		String s;
		switch (state) {
		case STATE_START:
			Log.i(TAG, "STATE_START: " + ROLE_TEXT);
			bluetoothModule.write(ROLE_TEXT.getBytes());
			state = State.STATE_PAYMENT_ROLE_SENT;
			break;
		case STATE_PAYMENT_ROLE_SENT:
			s = new String(bytes);
			Log.i(TAG, "STATE_PAYMENT_ROLE_SENT: " + s);
			state = State.STATE_WAIT_FOR_PAYMENT_ROLE;
			break;
		case STATE_WAIT_FOR_PAYMENT_ROLE:
			s = new String(bytes);
			Log.i(TAG, "STATE_WAIT_FOR_PAYMENT_ROLE: " + s);
			if (s.equals(BuyerRole.getRoleID())) {
				s = "requestPayment(SA, BtcA, TNrs)";
				bluetoothModule.write(s.getBytes());
				state = State.STATE_PAYMENT_REQUEST_SENT;
			} else {
				//both are in the same role or other error occured
				bluetoothModule.getHandler().obtainMessage(Messages.P2P_PROTOCOL_ERROR, Messages.P2P_PROTOCOL_ERROR_SAME_ROLE, 0);
				state = State.STATE_END;
			}
			break;
		case STATE_PAYMENT_REQUEST_SENT:
			s = new String(bytes);
			Log.i(TAG, "STATE_PAYMENT_REQUEST_SENT: " + s);
			state = State.STATE_WAIT_FOR_PAYMENT_CONFIRMATION;
			break;
		case STATE_WAIT_FOR_PAYMENT_CONFIRMATION:
			s = new String(bytes);
			Log.i(TAG, "STATE_WAIT_FOR_PAYMENT_CONFIRMATION: " + s);
			
			s = "confirmTransaction(C2)";
			bluetoothModule.write(s.getBytes());
			state = State.STATE_TRANSACTION_CONFIRMATION_SENT;
			break;
		case STATE_TRANSACTION_CONFIRMATION_SENT:
			s = new String(bytes);
			Log.i(TAG, "STATE_TRANSACTION_CONFIRMATION_SENT: " + s);
			state = State.STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT;
			break;
		case STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT:
			s = new String(bytes);
			Log.i(TAG, "STATE_WAIT_FOR_TRANSACTION_ACKNOWLEDGEMENT: " + s);
			
			bluetoothModule.getHandler().obtainMessage(Messages.P2P_PROTOCOL_FINISHED).sendToTarget();
			state = State.STATE_END;
			Log.i(TAG, "STATE_END");
			break;
		case STATE_END:
			//do nothing, since never entered
			break;
		}
	}

}
