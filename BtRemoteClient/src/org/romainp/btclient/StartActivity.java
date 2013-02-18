package org.romainp.btclient;

import java.util.ArrayList;
import java.util.HashMap;

import org.romainp.btclient.bluetooth.BtService;
import org.romainp.btclient.remote.BtRemoteActivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class StartActivity extends Activity {

	private ListView list;
	protected ProgressDialog progressDialog;
	protected Boolean connecting = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		IntentFilter filter = new IntentFilter(BtService.NO_BT);
		filter.addAction(BtService.START_BLUETOOTH);
		filter.addAction(BtService.NO_PAIRED);
		filter.addAction(BtService.LIST_PAIRED);
		filter.addAction(BtService.CONNECTED);
		filter.addAction(BtService.CONNECTION_FAILED);
		LocalBroadcastManager.getInstance(this).registerReceiver(this.btMessageReceiver, filter);
		
		this.bindService(new Intent(this, BtService.class), this.conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}
	
	@Override
	public void onDestroy() {
		this.unbindService(this.conn);
		super.onDestroy();
	}
	
	protected ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			LocalBroadcastManager.getInstance(StartActivity.this).sendBroadcast(new Intent(BtService.INIT));
		}

		public void onServiceDisconnected(ComponentName className) {
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BtService.REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BtService.GET_PAIRED));
				((Button) this.findViewById(R.id.startBtn)).setVisibility(Button.INVISIBLE);
			}
			else {
				((TextView) this.findViewById(R.id.txtv)).setText(this.getText(R.string.no_bt_started));
				((Button) this.findViewById(R.id.startBtn)).setVisibility(Button.VISIBLE);
			}
		}
	}

	private BroadcastReceiver btMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Intent data = null;
			if(intent.hasExtra("data"))
				data = (Intent) intent.getExtras().get("data");

			if(action.equals(BtService.NO_BT)) {
				((TextView) findViewById(R.id.txtv)).setText(getText(R.string.no_bt));
			}
			else if(action.equals(BtService.START_BLUETOOTH)) {
				startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BtService.REQUEST_ENABLE_BT);
			}
			else if(action.equals(BtService.NO_PAIRED)) {
				((TextView)findViewById(R.id.txtv)).setText(getText(R.string.no_paired));
			}
			else if(action.equals(BtService.LIST_PAIRED)) {
				((TextView) findViewById(R.id.txtv)).setText(getText(R.string.select_bt));
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, String>> devices = (ArrayList<HashMap<String, String>>) data.getExtras().get("devices");
				SimpleAdapter adapter = new SimpleAdapter (StartActivity.this, devices, R.layout.list_devices,
						new String[] {"name", "address"}, new int[] {R.id.name, R.id.address});
				list = (ListView) findViewById(R.id.listv);
				list.setAdapter(adapter);
				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adv, View v, int position, long id) {
						@SuppressWarnings("unchecked")
						HashMap<String, String> map = (HashMap<String, String>) list.getItemAtPosition(position);
						
						progressDialog = ProgressDialog.show(StartActivity.this, getString(R.string.connecting_window), getString(R.string.please_wait));
						connecting = true;
						
						Intent i = new Intent(BtService.CONNECT);
						i.putExtra("address", map.get("address"));
						LocalBroadcastManager.getInstance(StartActivity.this).sendBroadcast(i);
					}
				});
			}
			else if(action.equals(BtService.CONNECTED)) {
				connecting = false;
				StartActivity.this.progressDialog.dismiss();
				
				Intent remoteSelectorActivity = new Intent(StartActivity.this, BtRemoteActivity.class);
				startActivity(remoteSelectorActivity);
			}
			else if(action.equals(BtService.CONNECTION_FAILED)) {
				connecting = false;
				StartActivity.this.progressDialog.dismiss();
				
				Toast.makeText(StartActivity.this, getString(R.string.conenction_failed), Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * binds the start_bluetooth button
	 */
	public void startBluetooth(View view) {
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BtService.START_BLUETOOTH));
	}

}
