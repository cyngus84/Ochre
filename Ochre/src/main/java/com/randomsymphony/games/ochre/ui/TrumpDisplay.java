package com.randomsymphony.games.ochre.ui;

import com.randomsymphony.games.ochre.R;
import com.randomsymphony.games.ochre.logic.GameEngine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

public class TrumpDisplay extends Fragment implements View.OnClickListener {

	public static TrumpDisplay getInstance() {
		TrumpDisplay display = new TrumpDisplay();
		return display;
	}
	
	private View mContent;
	private GameEngine mEngine;
	private Button mSetTrump;
	private Button mPass;
	private CheckBox mAlone;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContent = inflater.inflate(R.layout.fragment_trump, null);
		initView();
		return mContent;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.set_trump:
				mEngine.setTrump(mAlone.isChecked());
				break;
			case R.id.pass:
				mEngine.pass();
				break;
		}
	}
	
	public void disablePass() {
		mPass.setEnabled(false);
	}
	
	public void setToPickMode() {
		mPass.setEnabled(true);
		mSetTrump.setText(R.string.button_set_trump);
		mSetTrump.setEnabled(false);
		mAlone.setEnabled(false);
		mAlone.setChecked(false);
	}
	
	public void enableSetTrump() {
		mSetTrump.setEnabled(true);
		mAlone.setEnabled(true);
	}
	
	public void setToOrderUpMode() {
		mPass.setEnabled(true);
		mSetTrump.setEnabled(true);
		mSetTrump.setText(R.string.button_order_up);
		mAlone.setEnabled(true);
		mAlone.setChecked(false);
	}
	
	public void setToPlayMode() {
		mPass.setEnabled(false);
		mSetTrump.setEnabled(false);
		mSetTrump.setText(R.string.button_order_up);
		mAlone.setEnabled(false);
	}

	public void setEnabled(boolean enabled) {
		mPass.setEnabled(enabled);
		mSetTrump.setEnabled(enabled);
		mSetTrump.setEnabled(enabled);
		mAlone.setEnabled(enabled);
	}
	
	public void reset() {
		setToOrderUpMode();		
	}
	
	public void setGameEngine(GameEngine engine) {
		mEngine = engine;
	}

	private void initView() {
		mSetTrump = (Button) mContent.findViewById(R.id.set_trump);
		mSetTrump.setOnClickListener(this);
		mPass = (Button) mContent.findViewById(R.id.pass);
		mPass.setOnClickListener(this);
		mAlone = (CheckBox) mContent.findViewById(R.id.go_alone);
	}
}
