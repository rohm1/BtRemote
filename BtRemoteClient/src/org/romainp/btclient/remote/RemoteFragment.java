package org.romainp.btclient.remote;

import org.romainp.btclient.bluetooth.BtService;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

public class RemoteFragment extends Fragment {
	
	public RemoteFragment() {
		super();
	}
	
	protected void broadcastToService(String sendData) {
		Intent i = new Intent(BtService.SEND_DATA);
		i.putExtra("sendData", sendData);
		LocalBroadcastManager.getInstance(this.getActivity()).sendBroadcast(i);
	}
}
