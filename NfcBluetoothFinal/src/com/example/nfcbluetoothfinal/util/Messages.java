package com.example.nfcbluetoothfinal.util;

public class Messages {
	public static final String ERROR_NO_BLUETOOTH = "Your device does not support bluetooth. Turning off.";
	public static final String TURNED_BLUETOOTH_OFF = "Please turn bluetooth on to continue.";
	public static final String ERROR_BLUETOOTH_CONNECTION_FAILED = "Bluetooth connection could not be established. Please try again by holding devices together.";
	public static final String ERROR_BLUETOOTH_CONNECTION_LOST = "Bluetooth connection was lost. Please try again by holding devices together.";
	public static final String ERROR_NO_NFC = "Your device does not support nfc. Turning off.";
	public static final String ERROR_NFC = "NFC error occured. Please try again by holding devices together.";
	public static final String ACTIVATE_NFC = "Please activate NFC and press Back to return to the application!";
	
	public static final int BLUETOOTH_ENABLED = 1;
	public static final int NFC_INTENT_PROCESSED = 2;
	public static final int NFC_PUSH_COMPLETE = 3;
	
	public static final int BLUETOOTH_CONNECTION_LOST = 6;
	public static final int BLUETOOTH_CONNECTION_FAILED = 7;
	public static final int BLUETOOTH_STATE_CHANGED = 8;
	public static final int BLUETOOTH_CONNECTION_ESTABLISHED = 9;
	public static final int NFC_ERROR_PROCESSING_INFOS = 10;
	public static final int BLUETOOTH_TURNED_OFF = 11;
	
	public static final int P2P_PROTOCOL_MESSAGE = 12;
	
	
}
