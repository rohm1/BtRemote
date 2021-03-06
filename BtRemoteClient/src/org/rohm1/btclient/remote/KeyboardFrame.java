package org.rohm1.btclient.remote;

import org.rohm1.btclient.bluetooth.BtCodes;
import org.rohm1.btclient.R;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardFrame extends MyFrame implements 
TextWatcher, 
InputFilter {
	
	EditText keyboard;

	public KeyboardFrame(BtRemoteActivity activity) {
		super(activity);
		this.setLayout(R.layout.frame_keyboard);
		
		this.keyboard = (EditText) this.getActivity().findViewById(R.id.keyboard);
      	this.keyboard.addTextChangedListener(this);
      	this.keyboard.setFilters(new InputFilter[]{this});
      	((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
      			.showSoftInput(this.keyboard, InputMethodManager.SHOW_IMPLICIT);
	}

	@Override
	public void stop() {
		this.keyboard.removeTextChangedListener(this);
		((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(this.keyboard.getWindowToken(), 0);
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
			sendData = BtCodes.SPECIAL_KEY + "|" + BtCodes.KEY_BACK_SP + "|";
		else {
			if(s.length() != 0)
				sendData = BtCodes.KEY + "|" + (int) s.charAt(s.length()-1) + "|";
			else
				send = false;
		}
		
		if(send)
			this.getActivity().broadcastData(sendData);
	}

	@Override
	public CharSequence filter(CharSequence soucr, int start, int end, Spanned dest, int dstart, int dend) {
//		for(int i=0;i<end-start;i++)
//			Log.i("aaaa", soucr.charAt(i)+"");
//		if(end == 0 && end > 0 && dest.length() == 0 ) {
//	    }
		return null;
	}
	
}
