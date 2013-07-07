package com.randomsymphony.games.ochre.ui;

import com.randomsymphony.games.ochre.R;
import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PlayerDisplay extends Fragment implements View.OnClickListener {

	private static final String KEY_GAME_ENGINE_TAG = "game_engine";
	private static final int DISCARD_SLOT = 5;

	public static PlayerDisplay getInstance(String tag) {
		PlayerDisplay display = new PlayerDisplay();
		Bundle args = new Bundle();
		args.putString(KEY_GAME_ENGINE_TAG, tag);
		display.setArguments(args);
		return display;
	}
	
	private Player mPlayer;
	private TextView mPlayerLabel;
	private Button[] mCards = new Button[6];
	private GameEngine mGameEngine;
	private boolean mIsActive = false;
	private boolean mExtraCardVisible = false;
	private View mContent;

	public PlayerDisplay() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGameEngine = (GameEngine) getActivity().getSupportFragmentManager()
				.findFragmentByTag(getArguments().getString(KEY_GAME_ENGINE_TAG));
	}
	
	/**
	 * Set the player, this will automatically cause a redraw and therefore
	 * should only be called from the main Thread.
	 */
	public void setPlayer(Player player) {
		mPlayer = player;
		redraw();
	}
	
	public Player getPlayer() {
		return mPlayer;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContent = (ViewGroup) inflater.inflate(R.layout.fragment_play_display, null);
		initViewReferences(mContent);
		redraw();
		return mContent;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d("JMATT", "Resuming.");
		redraw();
	}
	
	public void setExtraCardVisibility(boolean isVisible) {
		if (isVisible != mExtraCardVisible) {
			mExtraCardVisible = isVisible;
		}
		redraw();
	}
	
	private void initViewReferences(View content) {
		mPlayerLabel = (TextView) content.findViewById(R.id.player_name);
		mCards[DISCARD_SLOT] = (Button) content.findViewById(R.id.extra_card);
		mCards[0] = (Button) content.findViewById(R.id.card1);
		mCards[1] = (Button) content.findViewById(R.id.card2);
		mCards[2] = (Button) content.findViewById(R.id.card3);
		mCards[3] = (Button) content.findViewById(R.id.card4);
		mCards[4] = (Button) content.findViewById(R.id.card5);
		
		for (int ptr = 0; ptr < mCards.length; ptr++) {
			mCards[ptr].setOnClickListener(this);
		}
	}
	
	/**
	 * Set whether or not the current player is the active one. Only the active
	 * player will have their controls enabled.
	 */
	public void setActive(boolean isActive) {
		if (mIsActive != isActive) {
			mIsActive = isActive;
			redraw();
		}
	}
	
	/**
	 * Cause the player's view to be updated based on the {@link Player}. Should
	 * only be called from the main thread since it will touch the views.
	 */
	public void redraw() {
		if(!isResumed()) {
			Log.d("JMATT", "Not resumed, skipping redraw.");
			return;
		}
		
		String cardList = "";
		if (mPlayer != null) {
			Card[] playerCards = mPlayer.getCurrentCards();
			for (int ptr = 0; ptr < mCards.length; ptr++) {

				if (mIsActive) {
					// does this slot contain a card?
					if (ptr < playerCards.length) {
						Card target = playerCards[ptr];

						// if there is no card, disable this button, otherwise, set
						// it to the activation status of this player
						if (target == null) {
							mCards[ptr].setClickable(false);
						} else {
							mCards[ptr].setClickable(true);
						}

						Card.formatButtonAsCard(mCards[ptr], target, getResources());
					} else {
						mCards[ptr].setClickable(false);
						mCards[ptr].setBackgroundColor(Color.GREEN);
						mCards[ptr].setText("");
					}
				} else {
					mCards[ptr].setText("*");
					mCards[ptr].setBackgroundColor(getResources().getColor(R.color.disabled_card));
					mCards[ptr].setClickable(false);
				}
			}
		} else {
			throw new RuntimeException();
		}
		
		// set visibility of extra card
		mCards[DISCARD_SLOT].setVisibility(View.VISIBLE);

		// set label of player
		mPlayerLabel.setText("Hi, I'm " + (mPlayer != null ? mPlayer.getName() : "EMPTY") + "\n" + cardList);
		mContent.invalidate();
		
	}

	private void cardClicked(int cardIndex) {
		mGameEngine.playCard(mPlayer, mPlayer.getCurrentCards()[cardIndex]);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		    case R.id.card1:
		    	cardClicked(0);
			    break;
		    case R.id.card2:
		    	cardClicked(1);
			    break;
		    case R.id.card3:
		    	cardClicked(2);
			    break;
		    case R.id.card4:
		    	cardClicked(3);
			    break;
		    case R.id.card5:
		    	cardClicked(4);
			    break;
		    case R.id.extra_card:
		    	cardClicked(DISCARD_SLOT);
			    break;
		}
	}
}
