package org.rohm1.btclient.remote;

import org.rohm1.btclient.bluetooth.BtService;
import org.rohm1.btclient.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class BtRemoteActivity extends Activity implements OnClickListener {

	protected String[] remotes = {"Mouse", "Keyboard", "Presentation", "Media"};
	protected String[] frames = {"MouseFrame", "KeyboardFrame", "PresentationFrame", "MediaFrame"};
	protected MyFrame frame = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Set up bluetooth
//		IntentFilter filter = new IntentFilter(BtService.CONNECTED);
		IntentFilter filter = new IntentFilter();
		filter.addAction(BtService.SET_SCREEN);
		LocalBroadcastManager.getInstance(this).registerReceiver(this.btMessageReceiver, filter); //ondestroy onpause onresume

		// Set up layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bt_remote);

		ToggleButton b;
		for(String remote : this.remotes) {
			b = new ToggleButton(this);
			b.setTextOn(remote);
			b.setTextOff(remote);
			b.setTextColor(0xffffffff);
			b.setBackgroundResource(R.drawable.button);
			b.setOnClickListener(this);
			((LinearLayout) this.findViewById(R.id.remoteBtns)).addView(b);
		}

		// Set up Fragment
		this.setupFragment("Mouse");
	}

	@Override
	protected void onDestroy() {
		this.frame.stop();
		super.onDestroy();
	}

	private void setupFragment(String remote) {
		for(int i = 0 ; i < this.remotes.length ; i++) {
			if(remote.equals(this.remotes[i])) {
				if(this.frame == null || !this.frame.getClass().toString().equals("class org.rohm1.btclient.remote." + this.frames[i])) {
					((ToggleButton) ((LinearLayout) this.findViewById(R.id.remoteBtns)).getChildAt(i)).setChecked(true);
					if(this.frame != null)
						this.frame.stop();
					try {
						Class<?> _class = Class.forName("org.rohm1.btclient.remote." + this.frames[i]);
					    java.lang.reflect.Constructor<?> constructor = _class.getConstructor(new Class<?>[] {Class.forName("org.rohm1.btclient.remote.BtRemoteActivity")});
					    this.frame = (MyFrame) constructor.newInstance(new Object [] {BtRemoteActivity.this});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
				((ToggleButton) ((LinearLayout) this.findViewById(R.id.remoteBtns)).getChildAt(i)).setChecked(false);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_bt_remote, menu);
		return true;
	}

	/**
	 *
	 */
//	protected void setupLayout(int layout, int btn) {
//		LinearLayout remoteViewport = (LinearLayout) findViewById(R.id.remoteViewport);
//		remoteViewport.removeAllViews();
//		remoteViewport.addView(LayoutInflater.from(getBaseContext()).inflate(layout, null));
//		((Button) findViewById(btn)).setSelected(true);
//	}
//
//	public void launchMouse(View view) {
//		this.startRemote(MouseActivity.class);
//	}
//
//	public void launchKeyboard(View view) {
//		this.startRemote(KeyboardActivity.class);
//	}
//
//	protected void startRemote(Class c) {
////		if(!this.crtActivity.equals(action)) {
//			Intent i = new Intent(this, c);
//			i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//			startActivity(i);
//			finish();
////		}
//	}
//
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    if (keyCode == KeyEvent.KEYCODE_BACK) {
//	    	finish();
//	        return true;
//	    }
//	    return super.onKeyDown(keyCode, event);
//	}

	/**
	 * BtService's broadcasts
	 */

	protected void broadcastToService(Intent i) {
		LocalBroadcastManager.getInstance(this).sendBroadcast(i);
	}

	protected void broadcastToService(String action) {
		this.broadcastToService(new Intent(action));
	}

	protected void broadcastData(String data) {
		Intent i = new Intent(BtService.SEND_DATA);
		i.putExtra("sendData", data);
		this.broadcastToService(i);
	}

	protected BroadcastReceiver btMessageReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	if(action.equals(BtService.SET_SCREEN)) {
	    		Bundle b = ((Intent) intent.getExtras().get("data")).getExtras();
	    		byte[] buffer = (byte[]) b.get("bytes");
	    		int length = b.getInt("length");
	    		Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, length);

	    		try {
		    		ImageView trackpad = (ImageView) findViewById(R.id.trackpad);
		    		trackpad.setImageBitmap(bm);
	    		} catch(Exception e) {}
	    	}
//	    	else if(action.equals(BtService.CONNECTED)) {
//	    		connecting = false;
//	    		BtRemoteActivity.this.progressDialog.dismiss();
//	    	}
//	    	else if(action.equals(BtService.CONNECTION_FAILED)) {
//	    		connecting = false;
//	    		BtRemoteActivity.this.progressDialog.dismiss();
//				Toast.makeText(BtRemoteActivity.this, getString(R.string.conenction_failed), Toast.LENGTH_SHORT).show();
//				try {
//					onBackPressed();
//				} catch(Exception e) {}
//	    	}
	    }
	};

	@Override
	public void onClick(View v) {
		this.setupFragment(((Button) v).getText().toString());
	}

}
