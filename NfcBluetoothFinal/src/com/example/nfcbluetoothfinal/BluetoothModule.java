package com.example.nfcbluetoothfinal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.example.nfcbluetoothfinal.util.BluetoothMessage;
import com.example.nfcbluetoothfinal.util.BluetoothSession;
import com.example.nfcbluetoothfinal.util.BluetoothSessionInfos;
import com.example.nfcbluetoothfinal.util.Messages;

public class BluetoothModule {
	private static final String TAG = "BluetoothModule";
	
	private Handler handler;
	private BluetoothAdapter adapter;
	private BluetoothState state;
	
	private AcceptThread acceptThread;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	
	private BluetoothSession session;
	
	private boolean connectionAbortedIntentionally = false;
	
	public enum BluetoothState {
		STATE_NONE, STATE_LISTENING, STATE_CONNECTING, STATE_CONNECTED
	}

	public BluetoothModule(BluetoothAdapter adapter, Handler handler) {
		this.adapter = adapter;
		this.handler = handler;
		state = BluetoothState.STATE_NONE;
	}
	
	public synchronized void proceedProtocol(byte[] bytes) {
		session.proceed(bytes, this);
	}
	
	private synchronized void createSession() {
		//TODO jeton: remove this
		if (adapter.getAddress().equals("98:D6:F7:D2:9F:02")) {
			// LG device
			session = new BluetoothSession(true);
			Log.i(TAG, "im the seller");
		} else {
			session = new BluetoothSession(false);
			Log.i(TAG, "im the buyer");
		}
	}
	
	public boolean isConnectionAbortedIntentionally() {
		return connectionAbortedIntentionally;
	}
	
	public void setConnectionAbortedIntentionally(boolean intended) {
		connectionAbortedIntentionally = intended;
	}
	
	public Handler getHandler() {
		return this.handler;
	}
	
	/**
	 * Returns the current bluetooth connection state
	 * 
	 * @return the current BluetoothState
	 */
	public synchronized BluetoothState getSate() {
		return state;
	}
	
	/**
	 * Set the current state of the bluetooth connection
	 * 
	 * @param state
	 *            one of the possible enum BluetoothStates
	 */
	public synchronized void setState(BluetoothState state) {
		Log.i(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;

        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(Messages.BLUETOOTH_STATE_CHANGED, state).sendToTarget();
	}
	
	/**
	 * Start listening for incoming bluetooth connections.
	 */
	public synchronized void startListening() {
        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
        	connectThread.cancel();
        	connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
        	connectedThread.cancel();
        	connectedThread = null;
        }

        setState(BluetoothState.STATE_LISTENING);
        
        createSession();

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
	}
	
	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param infos
	 *            The session infos object containing e.g. initiator mac
	 *            address. Infos may not be null!
	 */
	public synchronized void connect(BluetoothSessionInfos infos) {
        // Cancel any thread attempting to make a connection
        if (state == BluetoothState.STATE_CONNECTING) {
            if (connectThread != null) {
            	connectThread.cancel();
            	connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
        	connectedThread.cancel();
        	connectedThread = null;
        }
        
        createSession();
        session.setInfos(infos);

        // Start the thread to connect with the given device
        BluetoothDevice remoteDevice = adapter.getRemoteDevice(session.getSessionInfos().getInitiatorDeviceAddress());
        connectThread = new ConnectThread(remoteDevice);
        connectThread.start();
        setState(BluetoothState.STATE_CONNECTING);
	}
	
	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (connectThread != null) {
        	connectThread.cancel();
        	connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
        	connectedThread.cancel();
        	connectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        handler.obtainMessage(Messages.BLUETOOTH_CONNECTION_ESTABLISHED).sendToTarget();

        setState(BluetoothState.STATE_CONNECTED);
	}
	
	/**
	 * stop all threads
	 */
	public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        
        session = null;

        setState(BluetoothState.STATE_NONE);
	}
	
	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (state != BluetoothState.STATE_CONNECTED)
            	return;
            
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
	
    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
     
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothSessionInfos.getServiceName(), BluetoothSessionInfos.getServiceUUID());
            } catch (IOException e) {
            	Log.e(TAG, "listen() failed", e);
            }
            serverSocket = tmp;
        }
     
        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (state != BluetoothState.STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                } catch (Exception e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                	synchronized (BluetoothModule.this) {
                		switch (state) {
	                		case STATE_NONE:
	                		case STATE_CONNECTED:
	                			// Either not ready or already connected. Terminate new socket.
	                            try {
	                                socket.close();
	                            } catch (IOException e) {
	                                Log.e(TAG, "Could not close unwanted socket", e);
	                            }
	                            break;
	                		case STATE_LISTENING:
	                		case STATE_CONNECTING:
	                			// Situation normal. Start the connected thread.
	                            connected(socket, socket.getRemoteDevice());
	                            break;
                		}
					}
                }
            }
        }
     
		/**
		 * Will cancel the listening socket, and cause the thread to finish
		 */
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
    
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
     
        public ConnectThread(BluetoothDevice device) {
        	this.device = device;
            BluetoothSocket tmp = null;
            
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(BluetoothSessionInfos.getServiceUUID());
            } catch (IOException e) { 
            	Log.e(TAG, "create() failed", e);
            }
            
            socket = tmp;
        }
     
        public void run() {
        	if (adapter.isDiscovering())
        		adapter.cancelDiscovery();
     
            try {
            	// This is a blocking call and will only return on a
                // successful connection or an exception
                socket.connect();
            } catch (IOException e) {
                // Unable to connect; close the socket and get out
                try {
                    socket.close();
                } catch (IOException e2) {
                	Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                handler.obtainMessage(Messages.BLUETOOTH_CONNECTION_FAILED).sendToTarget();
                return;
            }
            
            synchronized (BluetoothModule.this) {
            	connectThread = null;
			}
     
            // Do work to manage the connection (in a separate thread)
            connected(socket, device);
        }
     
        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            	Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
		private final BluetoothSocket socket;
		private final InputStream inputStream;
		private final OutputStream outputStream;

		public ConnectedThread(BluetoothSocket socket) {
			this.socket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				 Log.e(TAG, "temp sockets not created", e);
			}

			inputStream = tmpIn;
			outputStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[BluetoothMessage.MAX_MSG_SIZE];
			int bytes;

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = inputStream.read(buffer);
					// Send the obtained bytes to the UI activity
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					baos.write(buffer, 0, bytes);
					
					handler.obtainMessage(Messages.P2P_PROTOCOL_MESSAGE, baos.toByteArray()).sendToTarget();
				} catch (IOException e) {
					handler.obtainMessage(Messages.BLUETOOTH_CONNECTION_LOST).sendToTarget();
					break;
				}
			}
		}

		/**
		 * Call this from the main activity to send data to the remote device
		 */
		public void write(byte[] bytes) {
			if (bytes.length > BluetoothMessage.MAX_MSG_SIZE) {
				Log.e(TAG, "Message too large!!");
				handler.obtainMessage(Messages.P2P_PROTOCOL_ERROR, Messages.P2P_PROTOCOL_ERROR_MESSAGE_TOO_LARGE, 0).sendToTarget();
			} else {
				try {
					outputStream.write(bytes);
					
					// Share the sent message back to the UI Activity
					handler.obtainMessage(Messages.P2P_PROTOCOL_MESSAGE, bytes).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "Exception during write", e);
				}
			}
		}

		/**
		 * Call this from the main activity to shutdown the connection
		 */
		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

}
