package com.randomsymphony.games.ochre.ui;

import com.randomsymphony.games.ochre.R;
import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.logic.GameState.Phase;
import com.randomsymphony.games.ochre.logic.StateListener;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

public class PlayerDisplay extends Fragment implements View.OnClickListener, StateListener {

	private static final String KEY_WIDE_DISPLAY = "wide_display";
	private static final int DISCARD_SLOT = 5;

	/**
	 * @param isWide Whether this should be a "wide" or "tall" display,
	 * will determine which layout is used when drawing the player display.
	 */
	public static PlayerDisplay getInstance(boolean isWide) {
		PlayerDisplay display = new PlayerDisplay();
		Bundle args = new Bundle();
		args.putBoolean(KEY_WIDE_DISPLAY, isWide);
		display.setArguments(args);
		return display;
	}

	public interface SeatedChangeListener {
		public void onSeatChange(PlayerDisplay display, boolean seated);
	}
	
	private int LAYOUT_ID;
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
	/**
	 * Controls whether the cards the player currently has should be shown or
	 * hidden.
	 */
	private boolean mRevealCards = false;
	private Button mShowHide;
	private boolean mIsMaker;
	private boolean mIsDealer;
	private boolean mRadiosPresent = false;
	private TextView mTrickText;
	private int mTrickCount = 0;
	private boolean mWideDisplay;
	private GameState.Phase mPhase = GameState.Phase.NONE;
	/**
	 * Controls whether or not the show/hide controls are enabled
	 */
	private CheckBox mEnabled;
    private boolean mShowDiscardButtonEnabled = false;

	/**
	 * Controls whether or not controls relating to making a play are enabled.
	 */
	private boolean mIsPlayersTurn = false;

	private SeatedChangeListener mListener = null;

	public PlayerDisplay() {
		
	}

	public void setCheckboxEnabled(boolean enabled) {
		mEnabled.setEnabled(enabled);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGameEngine.registerStateListener(this);
		mWideDisplay = getArguments().getBoolean(KEY_WIDE_DISPLAY, false);
		LAYOUT_ID =  mWideDisplay ? R.layout.fragment_play_display_wide : 
			    R.layout.fragment_play_display;
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
	
	public void setRevealCards(boolean reveal) {
		mRevealCards = reveal;
		redraw();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContent = (ViewGroup) inflater.inflate(LAYOUT_ID, null);
		initViewReferences(mContent);
		redraw();
        disableAll();
		return mContent;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		redraw();
	}
	
	public void setExtraCardVisibility(boolean isVisible) {
		if (isVisible != mExtraCardVisible) {
			mExtraCardVisible = isVisible;
		}
		redraw();
	}
	
	/**
	 * Set whether or not the current player is the active one. Only when the
	 * player is active will they be able to show/hide their cards
	 */
	public void setActive(boolean isActive) {
		if (!isActive) {
			mRevealCards = false;
		}
		
		if (mIsActive != isActive) {
			mIsActive = isActive;
			redraw();
		}
	}
	
	public void setTrickCount(int count) {
		mTrickCount = count;
		redraw();
	}
	
	public void setGameEngine(GameEngine engine) {
		mGameEngine = engine;
	}

    public void setSeatChangeListener(SeatedChangeListener listener) {
        mListener = listener;
    }

    public void setIsPlayersTurn(boolean isTurn) {
        if (mIsPlayersTurn == isTurn) {
            return;
        } else {
            mIsPlayersTurn = isTurn;
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

        boolean isSeated = mEnabled.isChecked();

		Resources res = getActivity().getResources();

		mContent.setBackgroundColor(mIsPlayersTurn ?
				res.getColor(R.color.player_display_active) :
				res.getColor(R.color.player_display_inactive));

		// set label of player
		StringBuilder playerLabel = new StringBuilder(mPlayer != null ? mPlayer.getName() : "EMPTY");
		if (mIsDealer) {
			playerLabel.append(" DEALER");
		}
		if (mIsMaker) {
			playerLabel.append(" MAKER");
		}

		mPlayerLabel.setText(playerLabel.toString());

        if (mTrickCount > 0) {
            mTrickText.setText("Tricks: " + mTrickCount);
        } else {
            mTrickText.setText("");
        }

        Card[] cards = mPlayer.getCurrentCards();

        if (cards != null) {
            mExtraCardVisible = cards.length == 6;
        }

		if (mIsActive && isSeated) {
			mShowHide.setEnabled(true);
			mShowHide.setVisibility(View.VISIBLE);

			if (mRevealCards) {
				mShowHide.setText(R.string.hide_cards);
			} else {
				mShowHide.setText(R.string.show_cards);
			}

		} else {
			mShowHide.setEnabled(false);
			mShowHide.setVisibility(View.GONE);
		}

		
		String cardList = "";
		if (mPlayer != null) {
			Card[] playerCards = mPlayer.getCurrentCards();
			for (int ptr = 0; ptr < mCards.length; ptr++) {
				Button cardButton = mCards[ptr];

				if (isSeated && mRevealCards) {
					if (ptr < playerCards.length) {
						Card target = playerCards[ptr];

						// if there is no card and we're active, its only
						// clickable in play mode
						if (mPhase == GameState.Phase.PLAY) {
							cardButton.setClickable(mIsPlayersTurn);
						} else {
							cardButton.setClickable(false);
						}

						Card.formatButtonAsCard(mCards[ptr], target, res);
					} else {
						if (ptr < 6) {
							cardButton.setClickable(false);
							cardButton.setBackgroundColor(Color.GREEN);
							cardButton.setText("");
						} else {
							cardButton.setVisibility(View.GONE);
							mExtraCardSelector.setVisibility(View.GONE);
						}
					}
				} else {
					cardButton.setText("");
					cardButton.setBackgroundColor(getResources().getColor(R.color.disabled_card));
					cardButton.setClickable(false);
				}
			}
		} else {
			throw new RuntimeException();
		}

        // true if it is both this player's turn and the player is seated
        boolean isSeatedAndTurn = isSeated && mIsPlayersTurn;

        // the controls below here should only be present/active if
        // isSeatedAndTurn is true AND the widget's other control bits are set
        // appropriately

		// only show the card selectors if we're active and radios are set to
		// present
		for (int ptr = 0; ptr < mCardSelectors.length; ptr++) {
			mCardSelectors[ptr].setVisibility(
                    isSeatedAndTurn && mRadiosPresent ? View.VISIBLE : View.GONE);
		}

		// only show the extra card selector if we're active and its set to visible
		mExtraCardSelector.setVisibility(
                isSeatedAndTurn && mExtraCardVisible ? View.VISIBLE : View.GONE);

		// only show discard button if we're active and the extra card is visible
		// let other factors control its enablement
		mDiscard.setVisibility(
                isSeatedAndTurn && mExtraCardVisible ? View.VISIBLE : View.GONE);

		mDiscard.setEnabled(
                isSeatedAndTurn && mShowDiscardButtonEnabled);

		// set visibility of extra card
		mCards[DISCARD_SLOT].setVisibility(
                isSeatedAndTurn && mExtraCardVisible ? View.VISIBLE : View.GONE);
	}
	
	public void setDealer(boolean isDealer) {
		mIsDealer = isDealer;
	}
	
	public void setMaker(boolean isMaker) {
		mIsMaker = isMaker;
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
				mShowDiscardButtonEnabled = true;
				redraw();
		    	break;
		    case R.id.discard:
		    	discard();
		    	break;
		    case R.id.show_hide:
		    	mRevealCards = !mRevealCards;
		    	redraw();
		    	break;
		}
		
		Card[] cards = mPlayer.getCurrentCards();
		switch (v.getId()) {
	        case R.id.cardSelect1:
	    	    mGameEngine.setCurrentCandidateTrump(cards[0]);
	    	    break;
	        case R.id.cardSelect2:
	    	    mGameEngine.setCurrentCandidateTrump(cards[1]);
	    	    break;
	        case R.id.cardSelect3:
	    	    mGameEngine.setCurrentCandidateTrump(cards[2]);
	    	    break;
	        case R.id.cardSelect4:
	    	    mGameEngine.setCurrentCandidateTrump(cards[3]);
	    	    break;
	        case R.id.cardSelect5:
	    	    mGameEngine.setCurrentCandidateTrump(cards[4]);
	    	    break;
		}
	}
	
	@Override
	public void onStateChange(Phase newPhase) {
		mPhase = newPhase;

        // strictly speaking we only care about radio presence when it is the
        // player's turn. However, we don't use that here to determine whether
        // we should set the value of radio visibility because we don't want to
        // impose the requirement that the turn updates come before state
        // change updates. Instead we'd use both the radio visibility value AND
        // whether it is currently the player's turn to determine whether to
        // show the radios
        switch (newPhase) {
		    case PICK_TRUMP:
			case DEALER_DISCARD:
			   setRadioVisibility(true);
			   break;
		    case NONE:
		    case ORDER_UP:
		    case PLAY:
		    default:
		    	setRadioVisibility(false);
		    	break;
		}
		
		// reset trick count
		switch (newPhase) {
		    case ORDER_UP:
			    mTrickCount = 0;
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
		mShowDiscardButtonEnabled = false;
		redraw();
	}
	
	public void hideDiscardCard() {
		setRadioVisibility(false);
		setExtraCardVisibility(false);
		mShowDiscardButtonEnabled = false;
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
		
		mShowHide = (Button) mContent.findViewById(R.id.show_hide);
		mShowHide.setOnClickListener(this);
		
		mTrickText = (TextView) mContent.findViewById(R.id.trick_text);

		mEnabled = (CheckBox) mContent.findViewById(R.id.display_enabled);
		mEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    disableAll();
                } else {
                    enableRename();
                }
                redraw();

				if (mListener != null) {
					mListener.onSeatChange(PlayerDisplay.this, isChecked);
				}
			}
		});
	}

    /**
     * Disable and/or hide everything in the UI, but doesn't alter the state of
     * what should be shown, so if {@link #redraw()} we will draw correctly
     * based on the enablement state of the various controls of the UI.
     */
    private void disableAll() {
        for (int ptr = 0, limit = mCards.length; ptr < limit; ptr++) {
            mCards[ptr].setClickable(false);
        }

        for (int ptr = 0, limit = mCardSelectors.length; ptr < limit; ptr++) {
            mCardSelectors[ptr].setVisibility(View.GONE);
        }

        mExtraCardSelector.setVisibility(View.GONE);
        mDiscard.setVisibility(View.GONE);
        mShowHide.setVisibility(View.GONE);
        mCards[DISCARD_SLOT].setVisibility(View.GONE);
        mPlayerLabel.setClickable(false);
        mRevealCards = false;
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

    private void setRadioVisibility(boolean present) {
        mRadiosPresent = present;
    }


    private void enableRename() {
        mPlayerLabel.setClickable(true);
        mPlayerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog();
            }
        });
    }

    private void showRenameDialog() {
        PlayerNameDialog dialog = PlayerNameDialog.create(mPlayerLabel.getText().toString());
        dialog.registerListener(new PlayerNameDialog.Listener() {
            @Override
            public void onNameSet(String name) {
                mPlayer.setName(name);
                redraw();
                mGameEngine.pushStateUpdate();
            }
        });
        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

}
