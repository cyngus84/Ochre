package com.randomsymphony.games.ochre.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.randomsymphony.games.ochre.logic.GameState;

/**
 * @author cyngus
 * A Round is composed of a series of tricks and metadata about that series
 * of tricks such as who set trump, who dealt, whether the trump setter (the
 * "maker") is going alone, etc.
 */
public class Round {
	public Card trump;
	public ArrayList<Play[]> tricks;
	public boolean alone;
	public final Player dealer;
	public Player maker;
	/**
	 * Tracks the total number of plays in this round. This is needed mainly
	 * because whenever a new trick starts we allocate a new array containing
	 * enough space to hold the entire trick.
	 */
	public int totalPlays;
	/**
	 * During the {@link GameState.Phase#ORDER_UP} and
	 * {@link GameState.Phase#PICK_TRUMP} phases this tracks the number of
	 * players that have passed on setting trump. This allows us to track
	 * certain game state transitions.
	 */
	public int trumpPasses;
	/**
	 * The number of tricks that a given player has taken.
	 */
	public HashMap<Player, Integer> mCapturedTrickCount = new HashMap<Player, Integer>();
	
	public Round(Player dealer) {
		totalPlays = 0;
		tricks = new ArrayList<Play[]>();
		alone = false;
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
	
	/**
	 * Adds a captured trick for this player.
	 * @return The total number of tricks captured by this player
	 */
	public int addCapturedTrick(Player player) {
		if (mCapturedTrickCount.containsKey(player)) {
			mCapturedTrickCount.put(player, mCapturedTrickCount.get(player) + 1);
		} else {
			mCapturedTrickCount.put(player, 1);
		}
		
		return mCapturedTrickCount.get(player);
	}
}
