package org.romainp.btclient.remote;

import org.romainp.btclient.R;
import org.romainp.btclient.bluetooth.BtCodes;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class MouseFrame extends MyFrame implements OnTouchListener {
	
	protected TextView trackpad;
	protected String mousemode;
	protected long lastclick;
	protected int lastaction;

	public MouseFrame(BtRemoteActivity activity) {
		super(activity);
		this.setLayout(R.layout.frame_mouse);
		
		this.trackpad = (TextView) this.getActivity().findViewById(R.id.trackpad);
		this.trackpad.setOnTouchListener(this);
		this.mousemode = BtCodes.MOUSE_REL;
		this.lastclick = 0;
		this.lastaction = 0;
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.mouse_abs:
//			this.mousemode = BtCodes.MOUSE_ABS;
//			return true;
//		case R.id.mouse_rel:
//			this.mousemode = BtCodes.MOUSE_REL;
//			return true;
//		}
//		return false;
//	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		String sendData = "";
		long i = SystemClock.uptimeMillis();
		Boolean send = true;

		if(action == MotionEvent.ACTION_DOWN && event.getPressure() > 0.52 && this.lastaction == MotionEvent.ACTION_UP) {
			sendData = BtCodes.MOUSE_LEFT_CLC + "|";
			this.lastclick = i;
		}
		else if(action == MotionEvent.ACTION_MOVE && i-this.lastclick > 400) {
			sendData = this.mousemode + "|" + event.getX() + "|" + event.getY() + "|" + this.trackpad.getMeasuredWidth() + "|" + this.trackpad.getMeasuredHeight() + "|";
		}
		else 
			send = false;

		this.lastaction = action;

		if(send)
			this.getActivity().broadcastToService(sendData);

		return true;
	}

	@Override
	public void stop() {
	}
}
