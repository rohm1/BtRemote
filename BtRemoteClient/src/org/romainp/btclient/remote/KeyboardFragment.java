package org.romainp.btclient.remote;

import org.romainp.btclient.R;
import org.romainp.btclient.bluetooth.BtCodes;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardFragment extends RemoteFragment implements TextWatcher, InputFilter {
	
	EditText keyboard;
	
	public KeyboardFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_keyboard, container, false);
	}
	
	@Override
	public void onViewCreated (View view, Bundle savedInstanceState) {
		this.keyboard = (EditText) this.getActivity().findViewById(R.id.keyboard);
//      	this.keyboard.addTextChangedListener(this);
      	this.keyboard.setFilters(new InputFilter[]{this});
      	((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.keyboard, 0);
	}
	
	@Override
	public void onDestroy () {
		super.onDestroy();
	    ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.keyboard.getWindowToken(), 0);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String sendData = "";
		Boolean send = true;
		
		if(before > count)
			sendData = BtCodes.SPECIAL_KEY + "|" + BtCodes.KEY_DEL + "|";
		else {
			if(s.length() != 0)
				sendData = BtCodes.KEY + "|" + (int) s.charAt(s.length()-1) + "|";
			else
				send = false;
		}
		
		if(send)
			this.broadcastToService(sendData);
	}

	@Override
	public CharSequence filter(CharSequence soucr, int start, int end, Spanned dest, int dstart, int dend) {
//		if(end == 0 && ind > 0 && dest.length() == 0 ) {
//	    }
		return null;
	}
}
