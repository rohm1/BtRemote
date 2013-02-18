package org.romainp.btclient.remote;

import org.romainp.btclient.R;
import org.romainp.btclient.bluetooth.BtCodes;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PresentationFrame extends MyFrame {

	protected class TimerThread extends Thread {
		protected Boolean blink = true;

		public void _stop() {
			this.blink = false;
		}

		@Override
		public void run() {
			long time = SystemClock.uptimeMillis();
			while(this.blink) {
				int elapsed = (int) ((SystemClock.uptimeMillis() - time) / 1000);

				int min = elapsed / 60;
				int sec = elapsed - min * 60;

				Message msg = new Message();
				msg.obj = (min < 10 ? "0" : "") + String.valueOf(min) + ":" + (sec < 10 ? "0" : "") + String.valueOf(sec);
				mHandler.sendMessage(msg);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		}
	}

	static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (String) msg.obj;
			timerView.setText(text);
		}
	};

	protected TimerThread tt = null;
	static TextView timerView;
	protected TextView trackpad;

	public PresentationFrame(BtRemoteActivity activity) {
		super(activity);
		this.setLayout(R.layout.frame_presentation);

		timerView = ((TextView) activity.findViewById(R.id.timer));
		((Button) this.activity.findViewById(R.id.timerbtn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(tt == null) {
					tt = new TimerThread();
					tt.start();
					((Button) getActivity().findViewById(R.id.timerbtn)).setText(getActivity().getString(R.string.stop));
				}
				else {
					tt._stop();
					tt = null;
					((Button) getActivity().findViewById(R.id.timerbtn)).setText(getActivity().getString(R.string.start));
				}
			}

		});
		
		((Button) this.activity.findViewById(R.id.prevbtn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().broadcastToService(BtCodes.PREZ_PREV + "|");
			}

		});
		
		((Button) this.activity.findViewById(R.id.nextbtn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().broadcastToService(BtCodes.PREZ_NEXT + "|");
			}

		});
		
		((Button) this.activity.findViewById(R.id.fullscreenoffbtn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().broadcastToService(BtCodes.PREZ_FULL0 + "|");
			}

		});
		
		((Button) this.activity.findViewById(R.id.fullscreenonbtn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().broadcastToService(BtCodes.PREZ_FULL1 + "|");
			}

		});
		
		int h = ((LinearLayout) this.getActivity().findViewById(R.id.remoteViewport)).getHeight()-390;
		this.trackpad = ((TextView) this.getActivity().findViewById(R.id.trackpad));
		this.trackpad.setHeight(h);
		this.trackpad.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				
				if(action == MotionEvent.ACTION_UP)
					getActivity().broadcastToService(BtCodes.PREZ_POINTER_RELEASE + "|");
				else {
					if(event.getY() > 0)
						getActivity().broadcastToService(BtCodes.PREZ_POINTER + "|" + event.getX() + "|" + event.getY() + "|" + trackpad.getMeasuredWidth() + "|" + trackpad.getMeasuredHeight() + "|");
				}
				
				return true;
			}
			
		});
	}

	@Override
	public void stop() {
		if(tt != null) {
			tt._stop();
			tt = null;
		}
	}



}
