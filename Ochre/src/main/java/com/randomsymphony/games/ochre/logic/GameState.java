package com.randomsymphony.games.ochre.logic;

import java.util.ArrayList;
import java.util.UUID;

import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Play;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.model.Round;

import android.support.v4.app.Fragment;
import android.util.Log;

public class GameState extends Fragment {

	// TODO enums are inefficient, convert to static constants
	public static enum Phase {
		/**
		 * State right after dealing, players can choose to order up the card
		 * in the middle of the table and thus set trump.
		 */
		ORDER_UP,
		/**
		 * All players have rejected the dealt trump card, they may now pick
		 * any suit but the dealt trump as trump
		 */
		PICK_TRUMP,
		
		DEALER_DISCARD,
		/**
		 * Players are just playing tricks normally.
		 */
		PLAY,
		/**
		 * Play has not yet started, nothing has been dealt.
		 */
		NONE
	}
	
	private Player[] mPlayers = new Player[4];
	private int[] mScores = new int[] {0, 0, 0, 0};
	private PlayerFactory mPlayerSource;
	private DeckOfCards mDeck;
	private ArrayList<Round> mRounds = new ArrayList<Round>();
	private int mDealerOffset = -1;
	private Phase mGamePhase = Phase.NONE;
	private StateListener mStateListener;
	private UUID mGameId;

	public GameState() {
        super();
		initPlayers();
		mDeck = new DeckOfCards();
	}

    public void setPlayerFactory(PlayerFactory source) {
        mPlayerSource = source;
    }

	public Player[] getPlayers() {
		return new Player[] {mPlayers[0], mPlayers[1], mPlayers[2], mPlayers[3]};
	}
	
	public DeckOfCards getDeck() {
		return mDeck;
	}

	/**
	 * Create a new round, assigning the next player as the dealer.
	 * @return
	 */
	public Round createNewRound() {
		// create a new round, assigning the next dealer as the dealer
		int newDealer = mDealerOffset + 1;
		Round newRound = new Round(mPlayers[newDealer % mPlayers.length]);
		// set the default maker to the player to the right of the dealer
		newRound.maker = mPlayers[(newDealer + 1) % mPlayers.length];
		addRound(newRound);
		return newRound;
	}
	
	/**
	 * Add a round to the game state. Note that this will advance the dealer by
	 * one player.
	 * @param round The round to add
	 */
	public void addRound(Round round) {
		mDealerOffset++;
		Log.d("JMATT", "Dealer for this round is: " + round.dealer.getName());
		mRounds.add(round);
	}
	
	public Round getCurrentRound() {
		return mRounds.get(mRounds.size() - 1);
	}
	
	public Round[] getRounds() {
		Round[] rounds = new Round[mRounds.size()];
		mRounds.toArray(rounds);
		return rounds;
	}
	
	public Phase getGamePhase() {
		return mGamePhase;
	}

	public void setGamePhase(Phase gamePhase) {
		mGamePhase = gamePhase;
		if (mStateListener != null) {
			mStateListener.onStateChange(mGamePhase);
		}
	}
	
	public void setPhaseListener(StateListener listener) {
		mStateListener = listener;
	}
	
	public void addPoints(Player player, int numPoints) {
		Log.d("JMATT", "Adding " + numPoints + " to " + player.getName() + "'s team");
		for (int ptr = 0; ptr < mScores.length; ptr++) {
			if (player.getId().equals(mPlayers[ptr].getId())) {
				mScores[ptr] += numPoints;
				Log.d("JMATT", "Total points: " + mScores[ptr]);
				break;
			}
		}
	}
	
	public int getPointsForPlayer(Player player) {
		for (int ptr = 0; ptr < mScores.length; ptr++) {
			if (player.getId().equals(mPlayers[ptr].getId())) {
				return mScores[ptr];
			}
		}
		return -1;
	}

	public UUID getGameId() {
		return mGameId;
	}

	public void setGameId(UUID gameId) {
		mGameId = gameId;
	}
	
	private void initPlayers() {
		for (int count = 0; count < mPlayers.length; count++) {
			mPlayers[count] = mPlayerSource.createPlayer();
		}
	}
}