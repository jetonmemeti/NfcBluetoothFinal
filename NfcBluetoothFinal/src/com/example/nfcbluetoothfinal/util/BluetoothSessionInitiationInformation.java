package com.example.nfcbluetoothfinal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

//TODO jeton: add crypto things here
public class BluetoothSessionInitiationInformation implements Serializable {
	private static final long serialVersionUID = 1245813126060795439L;

	private static final String SERVICE_NAME = "com.example.nfcbluetoothfinal";

	private UUID serviceUUID;
	private String initiatorDeviceAddress = null;
	private String initiatorDeviceName = null;
	
	public BluetoothSessionInitiationInformation() {
		serviceUUID = UUID.randomUUID();
	}
	
	public BluetoothSessionInitiationInformation(String initiatorDeviceAddress, String initiatorDeviceName) {
		serviceUUID = UUID.randomUUID();
		this.initiatorDeviceAddress = initiatorDeviceAddress;
		this.initiatorDeviceName = initiatorDeviceName;
	}
	
	public UUID getServiceUUID() {
		return serviceUUID;
	}
	
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	public String getInitiatorDeviceAddress() {
		return initiatorDeviceAddress;
	}
	
	public String getInitiatorDeviceName() {
		return initiatorDeviceName;
	}
	
	public static byte[] serialize(BluetoothSessionInitiationInformation session) throws IOException {
		byte[] bytes = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream ous = null;

		ous = new ObjectOutputStream(baos);
		ous.writeObject(session);
		bytes = baos.toByteArray();

		ous.close();
		baos.close();

		return bytes;
	}

	public static BluetoothSessionInitiationInformation deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		BluetoothSessionInitiationInformation msg = null;

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		ois = new ObjectInputStream(bais);
		msg = (BluetoothSessionInitiationInformation) ois.readObject();

		bais.close();
		ois.close();

		return msg;
	}

}
