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

	public static final String ARG_GAME_ENGINE_TAG = "game_engine";
	
	public static TrumpDisplay getInstance(String gameEngineTag) {
		Bundle args = new Bundle();
		args.putString(ARG_GAME_ENGINE_TAG, gameEngineTag);
		TrumpDisplay display = new TrumpDisplay();
		display.setArguments(args);
		return display;
	}
	
	private View mContent;
	private GameEngine mEngine;
	private Button mSetTrump;
	private Button mPass;
	private CheckBox mAlone;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mEngine = (GameEngine) getFragmentManager().findFragmentByTag(getArguments().getString(
				ARG_GAME_ENGINE_TAG));
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContent = inflater.inflate(R.layout.fragment_trump, null);
		initView();
		return mContent;
	}
	
	private void initView() {
		mSetTrump = (Button) mContent.findViewById(R.id.set_trump);
		mSetTrump.setOnClickListener(this);
		mPass = (Button) mContent.findViewById(R.id.pass);
		mPass.setOnClickListener(this);
		mAlone = (CheckBox) mContent.findViewById(R.id.go_alone);
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
	
	public void reset() {
		setToOrderUpMode();		
	}
}
