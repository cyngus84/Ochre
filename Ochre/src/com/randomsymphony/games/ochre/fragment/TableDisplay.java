package com.randomsymphony.games.ochre.fragment;

import com.randomsymphony.games.ochre.R;
import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class TableDisplay extends Fragment {

	public static final String ARG_NUMBER_OF_PLAYER = "num_players";
	public static final String ARG_GAME_ENGINE = "game_engine";
	public static final String ARG_GAME_STATE = "game_state";
	
	private View mContent;
	private Button[] mPlayedCards;
	private GameEngine mEngine;
	private Button mTrumpCard;
	private GameState mGameState;
	
	/**
	 * @param numberOfPlayers Note that this parameter is currently ignored by
	 * the fragment. It may turn out not to be useful, if so, we'll remove later.
	 * @param gameEngineTag The tag that the {@link GameEngine} can be found at.
	 * @return
	 */
	public static TableDisplay getInstance(int numberOfPlayers, String gameEngineTag,
			String gameStateTag) {
		Bundle args = new Bundle();
		args.putInt(ARG_NUMBER_OF_PLAYER, numberOfPlayers);
		args.putString(ARG_GAME_ENGINE, gameEngineTag);
		args.putString(ARG_GAME_STATE, gameStateTag);
		
		TableDisplay instance = new TableDisplay();
		instance.setArguments(args);
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPlayedCards = new Button[4];
		mEngine = (GameEngine) getFragmentManager().findFragmentByTag(
				getArguments().getString(ARG_GAME_ENGINE));
		mGameState = (GameState) getFragmentManager().findFragmentByTag(
				getArguments().getString(ARG_GAME_STATE));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContent = inflater.inflate(R.layout.fragment_card_table, null);
		initPlayedCards();

		((ImageView) mContent.findViewById(R.id.table_felt)).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEngine.startGame();
					}
				});
        mTrumpCard = (Button) mContent.findViewById(R.id.candidate_trump);

		return mContent;
	}

    public void playCard(Card card, Player player) {
    	Player[] players = mGameState.getPlayers();
    	for (int ptr = 0; ptr < players.length; ptr++) {
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
    
    /**
     * Hide the card displayed on the card table that is the possible trump suit.
     */
    public void hideTrump() {
    	mTrumpCard.setVisibility(View.INVISIBLE);
    }
	
    private void initPlayedCards() {
    	mPlayedCards[0] = (Button) mContent.findViewById(R.id.player0_card);
    	mPlayedCards[1] = (Button) mContent.findViewById(R.id.player1_card);
    	mPlayedCards[2] = (Button) mContent.findViewById(R.id.player2_card);
    	mPlayedCards[3] = (Button) mContent.findViewById(R.id.player3_card);
    }
	
}
