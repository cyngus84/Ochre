package com.randomsymphony.games.ochre.model;

import android.util.Log;
import java.util.ArrayList;

public class Round {
	public int trump;
	public ArrayList<Play[]> tricks;
	public boolean alone;
	public final Player dealer;
	public Player maker;
	public int totalPlays;
	public int trumpPasses;
	
	private static final int[] TRUMPS = new int[] {Card.SUIT_CLUBS, Card.SUIT_SPADES, Card.SUIT_DIAMONDS, Card.SUIT_HEARTS};
	private static int sTrumpOffset = 0;
	
	public Round(Player dealer) {
		totalPlays = 0;
		tricks = new ArrayList<Play[]>();
		alone = false;
		// trump = TRUMPS[sTrumpOffset % TRUMPS.length];
		// trump++;
		// Log.d("JMATT", "Trump is: " + new Card(TRUMPS[sTrumpOffset], Card.VALUE_ACE).toString());
		this.dealer = dealer;
		trumpPasses = 0;
	}
	
	public void addPlay(Play play) {
		// TODO check if this player has already played and if so, throw
		Play[] currentTrick = tricks.get(tricks.size() - 1);
		currentTrick[totalPlays % currentTrick.length] = play;
		totalPlays++;
	}
	
	public int getActivePlayers() {
		return alone ? 3 : 4;
	}
	
	public int getCompletedTricks() {
		if (tricks.size() == 0) {
			return 0;
		}
		
		// check if current trick is completed
		Play[] currentTrick = tricks.get(tricks.size() - 1);
		if (currentTrick.length == getActivePlayers()) {
			// new trick is starting
			return tricks.size();
		} else {
			// trick is in progress
			return tricks.size() - 1;
		}
	}
	
	public Play[] getCurrentTrick() {
		if (tricks.size() == 0) {
			return null;
		} else {
			return tricks.get(tricks.size() - 1);
		}
	}
	
	/**
	 * @return The last completed trick or null if there is none
	 */
	public Play[] getLastCompletedTrick() {
		if (totalPlays < getActivePlayers()) {
			return null;
		} else {
			// for example, 11 total plays, 4 players means we want the second
			// completed trick, so (11 - 11 % 4) / 4 - 1 == (11 - 3) / 4 -1 ==
			// 8 / 4 - 1 == 1
			int trickOffset = (totalPlays - totalPlays % getActivePlayers()) / 
					getActivePlayers() - 1;
			return tricks.get(trickOffset);
		}
	}
	
	/**
	 * @return true if there are no tricks or if the current one has reached
	 * {@link #getActivePlayers()} number of cards.
	 */
	public boolean isCurrentTrickComplete() {
		Play[] currentTrick = getCurrentTrick();
		if (currentTrick == null) {
			return true;
		} else {
			return totalPlays % getActivePlayers() == 0;
		}
	}
}
