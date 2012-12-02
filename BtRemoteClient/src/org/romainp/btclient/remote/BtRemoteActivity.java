package org.romainp.btclient.remote;

import org.romainp.btclient.R;
import org.romainp.btclient.bluetooth.BtService;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.widget.Toast;

public class BtRemoteActivity extends FragmentActivity implements ActionBar.TabListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	protected static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	protected ProgressDialog progressDialog;
	protected Boolean connecting = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Bundle extras = getIntent().getExtras();
		
		// Set up bluetooth
		this.progressDialog = ProgressDialog.show(BtRemoteActivity.this, getString(R.string.connecting_window), getString(R.string.please_wait));
		Intent i = new Intent(BtService.CONNECT);
		this.connecting = true;
		i.putExtra("address", extras.getString("address"));
		LocalBroadcastManager.getInstance(this).sendBroadcast(i);
		
        IntentFilter filter = new IntentFilter(BtService.CONNECTED);
        filter.addAction(BtService.CONNECTION_FAILED);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.btMessageReceiver, filter);

        // Restore instance
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bt_remote);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_activity_mouse).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_activity_keyboard).setTabListener(this));
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_bt_remote, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		Fragment fragment = null;
		
		switch(tab.getPosition()) {
		case 0:
			fragment = new MouseFragment();
			break;
		case 1:
			fragment = new KeyboardFragment();
			break;
		default:
			break;
		}

//		Bundle args = new Bundle();
//		args.putSerializable("ma", new Activity());
//		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	
	/**
	 * BtService's broadcasts receiver
	 */
	protected BroadcastReceiver btMessageReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	if(action.equals(BtService.CONNECTED)) {
	    		connecting = false;
	    		BtRemoteActivity.this.progressDialog.dismiss();
	    	}
	    	else if(action.equals(BtService.CONNECTION_FAILED)) {
	    		connecting = false;
	    		BtRemoteActivity.this.progressDialog.dismiss();
				Toast.makeText(BtRemoteActivity.this, getString(R.string.conenction_failed), Toast.LENGTH_SHORT).show();
				try {
					onBackPressed();
				} catch(Exception e) {}
	    	}
	    }
	};

}
