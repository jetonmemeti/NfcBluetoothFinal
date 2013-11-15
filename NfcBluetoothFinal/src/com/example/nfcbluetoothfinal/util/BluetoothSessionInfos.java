package com.example.nfcbluetoothfinal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

public class BluetoothSessionInfos implements Serializable {
	private static final long serialVersionUID = -8495777804523810312L;

	private static final String SERVICE_NAME = "com.example.nfcbluetoothfinal";
	private static final UUID SERVICE_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
	
	private String initiatorDeviceAddress = null;
	private String initiatorDeviceName = null;
	
	public BluetoothSessionInfos() { }
	
	public BluetoothSessionInfos(String initiatorDeviceAddress, String initiatorDeviceName) {
		this.initiatorDeviceAddress = initiatorDeviceAddress;
		this.initiatorDeviceName = initiatorDeviceName;
	}
	
	public static UUID getServiceUUID() {
		return SERVICE_UUID;
	}
	
	public static String getServiceName() {
		return SERVICE_NAME;
	}
	
	public String getInitiatorDeviceAddress() {
		return initiatorDeviceAddress;
	}
	
	public String getInitiatorDeviceName() {
		return initiatorDeviceName;
	}
	
	public static byte[] serialize(BluetoothSessionInfos session) throws IOException {
		byte[] bytes = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;

		oos = new ObjectOutputStream(baos);
		oos.writeObject(session);
		bytes = baos.toByteArray();

		oos.close();
		baos.close();

		return bytes;
	}

	public static BluetoothSessionInfos deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		BluetoothSessionInfos msg = null;

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		ois = new ObjectInputStream(bais);
		msg = (BluetoothSessionInfos) ois.readObject();

		bais.close();
		ois.close();

		return msg;
	}
	
}
