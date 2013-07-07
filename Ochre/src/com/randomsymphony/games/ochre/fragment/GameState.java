package com.randomsymphony.games.ochre.fragment;

import java.util.ArrayList;

import com.randomsymphony.games.ochre.logic.DeckOfCards;
import com.randomsymphony.games.ochre.logic.PlayerFactory;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.model.Round;

import android.support.v4.app.Fragment;
import android.util.Log;

public class GameState extends Fragment {

	private Player[] mPlayers = new Player[4];
	private PlayerFactory mPlayerSource;
	private DeckOfCards mDeck;
	private int mCurrentTrump;
	private ArrayList<Round> mRounds = new ArrayList<Round>();
	private int mDealerOffset = -1;
	
	public GameState(PlayerFactory playerFactory) {
		mPlayerSource = playerFactory;
		initPlayers();
		mDeck = new DeckOfCards();
	}

	public Player[] getPlayers() {
		return new Player[] {mPlayers[0], mPlayers[1], mPlayers[2], mPlayers[3]};
	}
	
	public DeckOfCards getDeck() {
		return mDeck;
	}
	
	/**
	 * Should be one of the suit constants from {@link Card}.
	 * @param trump
	 */
	public void setTrump(int trump) {
		mCurrentTrump = mCurrentTrump;
	}
	
	public Round createNewRound(Player dealer) {
		Log.d("JMATT", "Dealer for this round is: " + dealer.getName());
		mRounds.add(new Round(dealer));
		return getCurrentRound();
	}
	
	public Round createNewRound() {
		mDealerOffset++;
		Round round = createNewRound(mPlayers[mDealerOffset % mPlayers.length]);
		round.maker = mPlayers[(mDealerOffset + 1) % mPlayers.length];
		return round;
	}
	
	public Round getCurrentRound() {
		return mRounds.get(mRounds.size() - 1);
	}
	
	private void initPlayers() {
		for (int count = 0; count < mPlayers.length; count++) {
			mPlayers[count] = mPlayerSource.createPlayer();
		}
	}
}