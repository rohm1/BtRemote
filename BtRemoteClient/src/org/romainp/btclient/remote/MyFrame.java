package org.romainp.btclient.remote;

import org.romainp.btclient.R;

import android.view.LayoutInflater;
import android.widget.LinearLayout;

public abstract class MyFrame {
	
	protected BtRemoteActivity activity;
	
	public MyFrame(BtRemoteActivity activity) {
		this.activity = activity;
	}
	
	protected BtRemoteActivity getActivity() {
		return this.activity;
	}
	
	public void setActivity(BtRemoteActivity btRemoteActivity) {
		this.activity = btRemoteActivity;
	}
	
	protected void setLayout(int layout) {
		LinearLayout remoteViewport = (LinearLayout) this.activity.findViewById(R.id.remoteViewport);
		remoteViewport.removeAllViews();
		remoteViewport.addView(LayoutInflater.from(this.activity.getBaseContext()).inflate(layout, null));
	}
	
	public abstract void stop();

}
