package com.randomsymphony.games.ochre.ui;

import java.util.ArrayList;

import com.randomsymphony.games.ochre.R;
import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.logic.GameState.Phase;
import com.randomsymphony.games.ochre.logic.StateListener;
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
import android.widget.RadioButton;
import android.widget.TextView;

public class PlayerDisplay extends Fragment implements View.OnClickListener, StateListener {

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
	private RadioButton[] mCardSelectors = new RadioButton[5];
	private RadioButton mExtraCardSelector;
	private Button mDiscard;

	public PlayerDisplay() {
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGameEngine = (GameEngine) getActivity().getSupportFragmentManager()
				.findFragmentByTag(getArguments().getString(KEY_GAME_ENGINE_TAG));
		mGameEngine.registerStateListener(this);
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
		
		mCardSelectors[0] = (RadioButton) content.findViewById(R.id.cardSelect1);
		mCardSelectors[1] = (RadioButton) content.findViewById(R.id.cardSelect2);
		mCardSelectors[2] = (RadioButton) content.findViewById(R.id.cardSelect3);
		mCardSelectors[3] = (RadioButton) content.findViewById(R.id.cardSelect4);
		mCardSelectors[4] = (RadioButton) content.findViewById(R.id.cardSelect5);
		
		for (int ptr = 0; ptr < mCardSelectors.length; ptr++) {
			mCardSelectors[ptr].setOnClickListener(this);
		}
		
		mExtraCardSelector = (RadioButton) content.findViewById(R.id.extra_card_select);
		mExtraCardSelector.setOnClickListener(this);
		
		mDiscard = (Button) mContent.findViewById(R.id.discard);
		mDiscard.setOnClickListener(this);
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

						// if there is no card and we're active, its only
						// clickable in play mode
						if (mPhase == GameState.Phase.PLAY) {
							mCards[ptr].setClickable(true);
						} else {
							mCards[ptr].setClickable(false);
						}

						Card.formatButtonAsCard(mCards[ptr], target, getResources());
					} else {
						if (ptr < 6) {
							mCards[ptr].setClickable(false);
							mCards[ptr].setBackgroundColor(Color.GREEN);
							mCards[ptr].setText("");
						} else {
							mCards[ptr].setVisibility(View.GONE);
							mExtraCardSelector.setVisibility(View.GONE);
						}
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
		
		// only show the card selectors if we're active and radios are set to
		// present
		for (int ptr = 0; ptr < mCardSelectors.length; ptr++) {
			mCardSelectors[ptr].setVisibility(mRadiosPresent && mIsActive ? View.VISIBLE : View.GONE);
		}
		
		// only show the extra card selector if we're active and its set to visible
		mExtraCardSelector.setVisibility(mExtraCardVisible && mIsActive ? View.VISIBLE : View.GONE);
		
		// only show discard button if we're active and the extra card is visible
		// let other factors control its enablement
		mDiscard.setVisibility(mExtraCardVisible && mIsActive ? View.VISIBLE : View.GONE);
		
		// set visibility of extra card
		mCards[DISCARD_SLOT].setVisibility(mExtraCardVisible ? View.VISIBLE : View.INVISIBLE);

		// set label of player
		mPlayerLabel.setText("Hi, I'm " + (mPlayer != null ? mPlayer.getName() : "EMPTY") + "\n" + cardList);
		
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
		    case R.id.cardSelect1:
		    case R.id.cardSelect2:
		    case R.id.cardSelect3:
		    case R.id.cardSelect4:
		    case R.id.cardSelect5:
		    case R.id.extra_card_select:
		    	uncheckOtherRadios(v.getId());
				mDiscard.setEnabled(true);
		    	break;
		    case R.id.discard:
		    	discard();
		}
	}
	
	private void discard() {
		// find the selected card
		int offset = -1;
		for (int ptr = 0; ptr < mCardSelectors.length; ptr++) {
			if (mCardSelectors[ptr].isChecked()) {
				offset = ptr;
				break;
			}
		}
		
		Card[] currentCards = mPlayer.getCurrentCards();
		
		if (offset == -1) {
			if (mExtraCardSelector.isChecked()) {
				// TODO its dangerous to assume the extra card is here, fix
				mGameEngine.discardCard(currentCards[currentCards.length - 1]);
			} else {
				throw new RuntimeException("Nothing selected!");
			}
		} else {
			mGameEngine.discardCard(mPlayer.getCurrentCards()[offset]);
		}
	}
	
	private void uncheckOtherRadios(int selectedId) {
		for (int ptr = 0; ptr < mCardSelectors.length; ptr++) {
			if (mCardSelectors[ptr].getId() != selectedId) {
				mCardSelectors[ptr].setChecked(false);
			}
		}
		
		if (mExtraCardSelector.getId() != selectedId) {
			mExtraCardSelector.setChecked(false);
		}
	}

	private boolean mRadiosPresent = false;
	
	private void setRadioVisibility(boolean present) {
		mRadiosPresent = present;
	}
	
	private GameState.Phase mPhase = GameState.Phase.NONE;
	
	@Override
	public void onStateChange(Phase newPhase) {
		mPhase = newPhase;
		
		switch (newPhase) {
		    case PICK_TRUMP:
			   setRadioVisibility(true);
			   break;
		    case NONE:
		    case ORDER_UP:
		    case PLAY:
		    default:
		    	setRadioVisibility(false);
		    	break;
		}
		redraw();
	}
	
	
	public Card getSelectedCard() {
		for (int ptr = 0; ptr < mCardSelectors.length; ptr++) {
			if (mCardSelectors[ptr].isChecked()) {
				return mPlayer.getCurrentCards()[ptr];
			}
		}
		return null;
	}
	
	public void showDiscardCard() {
		setExtraCardVisibility(true);
		setRadioVisibility(true);
		redraw();
		mDiscard.setEnabled(false);
	}
	
	public void hideDiscardCard() {
		setRadioVisibility(false);
		mExtraCardVisible = false;
		redraw();
	}
}
