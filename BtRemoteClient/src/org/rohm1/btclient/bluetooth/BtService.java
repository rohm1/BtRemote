package org.rohm1.btclient.bluetooth;

import java.io.IOException;
import java.io.InputStream;
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
	protected InputStream receiveStream = null;
	protected OutputStream sendStream = null;
	protected Boolean connected = false;
	
	protected ScreenThread screenThread;

	public static final int REQUEST_ENABLE_BT = 1;

	protected final String serverUUID = "58cf9ce0-1a16-11e2-892e-0800200c9a66";

	/*
	 * Input actions
	 */
	public static final String INIT = "org.rohm1.btclient.bluetooth.BtService.INIT";
	public static final String GET_PAIRED = "org.rohm1.btclient.bluetooth.BtService.GET_PAIRED";
	public static final String CONNECT = "org.rohm1.btclient.bluetooth.BtService.CONNECT";
	public static final String SEND_DATA = "org.rohm1.btclient.bluetooth.BtService.SEND_DATA";
	public static final String START_SCREEN = "org.rohm1.btclient.bluetooth.BtService.START_SCREEN";
	public static final String STOP_SCREEN = "org.rohm1.btclient.bluetooth.BtService.STOP_SCREEN";

	/*
	 * Return actions
	 */
	public static final String NO_BT = "org.rohm1.btclient.bluetooth.BtService.NO_BT";
	public static final String START_BLUETOOTH = "org.rohm1.btclient.bluetooth.BtService.START_BLUETOOTH";
	public static final String NO_BT_STARTED = "org.rohm1.btclient.bluetooth.BtService.NO_BT_STARTED";
	public static final String NO_PAIRED = "org.rohm1.btclient.bluetooth.BtService.NO_PAIRED";
	public static final String LIST_PAIRED = "org.rohm1.btclient.bluetooth.BtService.LIST_PAIRED";
	public static final String CONNECTED = "org.rohm1.btclient.bluetooth.BtService.CONNECTED";
	public static final String CONNECTION_FAILED = "org.rohm1.btclient.bluetooth.BtService.CONNECTION_FAILED";
	public static final String CONNECTION_LOST = "org.rohm1.btclient.bluetooth.BtService.CONNECTION_LOST";
	public static final String SET_SCREEN = "org.rohm1.btclient.bluetooth.BtService.SET_SCREEN";

	/**
	 * Life cycle
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Register broadcasts from activities
		IntentFilter filter = new IntentFilter(BtService.INIT);
        filter.addAction(BtService.GET_PAIRED);
        filter.addAction(BtService.CONNECT);
        filter.addAction(BtService.SEND_DATA);
        filter.addAction(BtService.START_SCREEN);
        filter.addAction(BtService.STOP_SCREEN);
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
		this.closeSocket();
		
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
	    	if(action.equals(BtService.GET_PAIRED)) {
	    		listPaired();
	    	}
	    	else if(action.equals(BtService.CONNECT)) {
		    	if(bunble != null && bunble.getString("address") != null) {
		    		address = bunble.getString("address");
		    		connect();
		    	}
		    	else
		    		sendLocalBroadcast(BtService.CONNECTED);
	    	}
	    	else if(action.equals(BtService.SEND_DATA)) {
	    		if(bunble != null && bunble.getString("sendData") != null)
					sendData(bunble.getString("sendData"));
	    	}
	    	else if(action.equals(BtService.START_SCREEN)) {
	    		if(screenThread != null)
	    			screenThread._stop();
	    		screenThread = new ScreenThread();
	    		screenThread.start();
	    	}
	    	else if(action.equals(BtService.STOP_SCREEN)) {
	    		if(screenThread != null) {
	    			screenThread._stop();
	    			screenThread = null;
	    		}
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
	
	protected void connect() {
		if(!this.connected) {
			this.closeSocket();
			try {
				BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.address);
				try {
					this.socket = device.createRfcommSocketToServiceRecord(UUID.fromString(this.serverUUID));
					this.receiveStream = socket.getInputStream();
					this.sendStream = this.socket.getOutputStream();

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

				} catch (IOException e) {
					sendLocalBroadcast(BtService.CONNECTION_FAILED);
					e.printStackTrace();
				}
			} catch (IllegalArgumentException e) {
				sendLocalBroadcast(BtService.CONNECTION_FAILED);
				e.printStackTrace();
			}
		}
		else
			this.sendLocalBroadcast(BtService.CONNECTED);
	}
	
	protected void closeSocket() {
		try {
			this.connected = false;
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {}
	}
	
	protected void conenctionLost() {
		this.connected = false;
    	this.closeSocket();
    	this.sendLocalBroadcast(BtService.CONNECTION_LOST);
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
				} catch (Exception e) {
					conenctionLost();
					e.printStackTrace();
				}
			}
		}
	}

	protected void sendData(String data) {
		new SendThread(data).start();
	}
	
	protected class ScreenThread extends Thread {
		byte[] buffer;
		int bytes_read;
		String received;
		Boolean blinker = true;
		
		public void _stop() {
			this.blinker = false;
		}
		
		public void run() {
			while(this.blinker) {
				
				try {
					buffer = new byte[16];
					bytes_read = receiveStream.read(buffer, 0, 16);
					received = new String(buffer, 0, bytes_read);
					String[] _received = received.split("s:");
					
					if(_received[0].equals("")) {
						int l = Integer.parseInt(_received[1]);
						int _l = 0;
						byte[] bytes = new byte[l];
						
						while(_l < l && this.blinker) {
							buffer = new byte[2048];
							bytes_read = receiveStream.read(bytes, _l, l-_l);
							_l += bytes_read;
						}
						
						if(this.blinker) {
							Intent i = new Intent();
							i.putExtra("bytes", bytes);
							i.putExtra("length", l);
							sendLocalBroadcast(BtService.SET_SCREEN, i);
						}
					}
				} catch (Exception e) {}
			}
			
		}
	}

}
