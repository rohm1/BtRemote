package org.romainp.btclient.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class BtService extends Service {
	
	protected final IBinder mBinder = new MyBinder();
	
	protected BluetoothAdapter blueAdapter = null;
	protected String address = null;
	protected BluetoothSocket socket = null;
	//    protected InputStream receiveStream = null;
	protected OutputStream sendStream = null;
	protected Boolean connected = false;

	public static final int REQUEST_ENABLE_BT = 1;

	protected final String serverUUID = "58cf9ce0-1a16-11e2-892e-0800200c9a66";

	/*
	 * Input actions
	 */
	public static final String INIT = "org.romainp.btclient.bluetooth.BtService.INIT";
	public static final String GET_PAIRED = "org.romainp.btclient.bluetooth.BtService.GET_PAIRED";
	public static final String OPEN_SOCKET = "org.romainp.btclient.bluetooth.BtService.OPEN_SOCKET";
	public static final String CONNECT = "org.romainp.btclient.bluetooth.BtService.CONNECT";
	public static final String SEND_DATA = "org.romainp.btclient.bluetooth.BtService.SEND_DATA";

	/*
	 * Return actions
	 */
	public static final String NO_BT = "org.romainp.btclient.bluetooth.BtService.NO_BT";
	public static final String START_BLUETOOTH = "org.romainp.btclient.bluetooth.BtService.START_BLUETOOTH";
	public static final String NO_BT_STARTED = "org.romainp.btclient.bluetooth.BtService.NO_BT_STARTED";
	public static final String NO_PAIRED = "org.romainp.btclient.bluetooth.BtService.NO_PAIRED";
	public static final String LIST_PAIRED = "org.romainp.btclient.bluetooth.BtService.LIST_PAIRED";
	public static final String CONNECTED = "org.romainp.btclient.bluetooth.BtService.CONNECTED";
	public static final String CONNECTION_FAILED = "org.romainp.btclient.bluetooth.BtService.CONNECTION_FAILED";

	/**
	 * Life cycle
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Register broadcasts from activities
		IntentFilter filter = new IntentFilter(BtService.INIT);
        filter.addAction(BtService.GET_PAIRED);
        filter.addAction(BtService.OPEN_SOCKET);
        filter.addAction(BtService.CONNECT);
        filter.addAction(BtService.SEND_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.aMessageReceiver, filter);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class MyBinder extends Binder {
		BtService getService() {
			return BtService.this;
		}
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(aMessageReceiver);
		
		super.onDestroy();
	}
	
	/*
	 * Service interactions
	 */
	protected void sendLocalBroadcast(String action) {
		this.sendLocalBroadcast(action, null);
	}
	
	private void sendLocalBroadcast(String action, Intent data){
		Intent i = new Intent(action);
		i.putExtra("data", data);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
	}
	
	protected BroadcastReceiver aMessageReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	Bundle bunble = intent.getExtras();
	    	
	    	if(action.equals(BtService.INIT)) {
	    		init();
	    	}
	    	else if(action.equals(BtService.GET_PAIRED)) {
	    		listPaired();
	    	}
	    	else if(action.equals(BtService.OPEN_SOCKET)) {
	    		openSocket();
	    	}
	    	else if(action.equals(BtService.CONNECT)) {
		    	if(bunble != null && bunble.containsKey("address")) {
		    		if(!address.equals(bunble.getString("address"))) {
		    			closeSocket();
		    			address = bunble.getString("address");
		    		}
		    	}
		    	connect();
	    	}
	    	else if(action.equals(BtService.SEND_DATA)) {
	    		if(bunble != null && bunble.containsKey("sendData"))
					sendData(bunble.getString("sendData"));
	    	}
	    }
	};

	/*
	 * Service methods
	 */
	protected void init() {
		blueAdapter = BluetoothAdapter.getDefaultAdapter();
		if (blueAdapter != null) {
			if (!blueAdapter.isEnabled())
				this.sendLocalBroadcast(BtService.START_BLUETOOTH);
			else
				this.listPaired();
		}
		else
			this.sendLocalBroadcast(BtService.NO_BT);
	}

	protected void listPaired() {
		Set<BluetoothDevice> setpairedDevices = blueAdapter.getBondedDevices();
		BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);

		if(pairedDevices.length == 0)
			this.sendLocalBroadcast(BtService.NO_PAIRED);
		else {
			ArrayList<HashMap<String, String>> devices = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map;
			for(int i=0;i<pairedDevices.length;i++) {
				map = new HashMap<String, String>();
				map.put("name", pairedDevices[i].getName());
				map.put("address", pairedDevices[i].getAddress());
				devices.add(map);
			}

			Intent data = new Intent();
			data.putExtra("devices", devices);
			this.sendLocalBroadcast(BtService.LIST_PAIRED, data);	       
		}
	}
	
	protected void openSocket() {
		try {
			BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.address);
			try {
				this.socket = device.createRfcommSocketToServiceRecord(UUID.fromString(this.serverUUID));
	//			receiveStream = socket.getInputStream();
				this.sendStream = this.socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	protected void connect() {
		try {
			this.sendStream.write("test".getBytes());
			this.sendStream.flush();
			this.sendLocalBroadcast(BtService.CONNECTED);
		} catch(Exception e) {
			if(!this.connected) {
				this.openSocket();
				
				new Thread() {
					@Override
					public void run() {
						try {
							socket.connect();
							connected = true;
							sendLocalBroadcast(BtService.CONNECTED);
						} catch (IOException e) {
							sendLocalBroadcast(BtService.CONNECTION_FAILED);
							e.printStackTrace();
						}
					}
				}.start();
			}
			else
				this.sendLocalBroadcast(BtService.CONNECTED);
		}
	}
	
	protected void closeSocket() {
		try {
			this.socket.close();
			this.connected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void conenctionLost() {
		this.connected = false;
    	this.closeSocket();
    	this.sendLocalBroadcast(BtService.CONNECTION_FAILED);
	}
	
	class SendThread extends Thread {
		protected String data;
		
		public SendThread(String data) {
			this.data = data;
		}
		
		@Override
		public void run() {
			if(connected) {
				try {
					sendStream.write(this.data.getBytes());
					sendStream.flush();
				} catch (IOException e) {
					conenctionLost();
					e.printStackTrace();
				} catch(NullPointerException e) {
					conenctionLost();
					e.printStackTrace();
				}
			}
		}
	}

	protected void sendData(String data) {
		new SendThread(data).start();
	}

}
