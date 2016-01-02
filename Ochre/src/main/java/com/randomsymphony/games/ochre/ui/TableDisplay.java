package com.randomsymphony.games.ochre.ui;

import com.randomsymphony.games.ochre.R;
import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TableDisplay extends Fragment {

	public static final String ARG_NUMBER_OF_PLAYER = "num_players";
	public static final String ARG_GAME_STATE = "game_state";

	/**
	 * @param numberOfPlayers Note that this parameter is currently ignored by
	 * the fragment. It may turn out not to be useful, if so, we'll remove later.
	 * @param gameStateTag The tag that the {@link GameEngine} can be found at.
	 * @return
	 */
	public static TableDisplay getInstance(int numberOfPlayers, String gameStateTag) {
		Bundle args = new Bundle();
		args.putInt(ARG_NUMBER_OF_PLAYER, numberOfPlayers);
		args.putString(ARG_GAME_STATE, gameStateTag);
		
		TableDisplay instance = new TableDisplay();
		instance.setArguments(args);
		return instance;
	}
	
	private View mContent;
	private Button[] mPlayedCards;
	private Button mTrumpCard;
	private GameState mGameState;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPlayedCards = new Button[4];
		mGameState = (GameState) getFragmentManager().findFragmentByTag(
				getArguments().getString(ARG_GAME_STATE));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContent = inflater.inflate(R.layout.fragment_card_table, null);
		initPlayedCards();
        mTrumpCard = (Button) mContent.findViewById(R.id.candidate_trump);
		return mContent;
	}

    public void playCard(Card card, Player player) {
    	Player[] players = mGameState.getPlayers();
    	for (int ptr = 0; ptr < players.length; ptr++) {
            // TODO maybe this should be comparing IDs instead of object
            // equality
    		if (players[ptr] == player) {
    			Button playedCard = mPlayedCards[ptr];
    			Card.formatButtonAsCard(playedCard, card, getResources());
				playedCard.setVisibility(View.VISIBLE);
    			break;
    		}
    	}
    }
    
    public void setTrumpCard(Card card) {
    	Card.formatButtonAsCard(mTrumpCard, card, getResources());
    	mTrumpCard.setVisibility(View.VISIBLE);
    }
    
    public void setTrumpSuit(int suit) {
    	switch (suit) {
    	    case Card.SUIT_CLUBS:
    		    mTrumpCard.setText("\u2663");
    		    break;
    	    case Card.SUIT_HEARTS:
    	        mTrumpCard.setText("\u2764");
    	        break;
    	    case Card.SUIT_DIAMONDS:
    	        mTrumpCard.setText("\u2666");
    	        break;
    	    case Card.SUIT_SPADES:
    	        mTrumpCard.setText("\u2660");
    	        break;
    	}
    	
		if (suit == Card.SUIT_DIAMONDS || suit == Card.SUIT_HEARTS) {
			mTrumpCard.setBackgroundColor(getResources().getColor(R.color.red_card));
		} else {
			mTrumpCard.setBackgroundColor(getResources().getColor(R.color.black_card));
		}
		mTrumpCard.setVisibility(View.VISIBLE);
    }

    /**
     * Hide the card displayed on the card table that is the possible trump suit.
     */
    public void hideTrump() {
    	mTrumpCard.setVisibility(View.INVISIBLE);
    }
    
    public void clearPlayedCards() {
    	for (int ptr = 0; ptr < mPlayedCards.length; ptr++) {
    		mPlayedCards[ptr].setVisibility(View.INVISIBLE);
    	}
    }

	public void setGameState(GameState state) {
		mGameState = state;
	}
	
    private void initPlayedCards() {
    	mPlayedCards[0] = (Button) mContent.findViewById(R.id.player0_card);
    	mPlayedCards[1] = (Button) mContent.findViewById(R.id.player1_card);
    	mPlayedCards[2] = (Button) mContent.findViewById(R.id.player2_card);
    	mPlayedCards[3] = (Button) mContent.findViewById(R.id.player3_card);
    }
}
