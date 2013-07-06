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
	
	private static final int[] TRUMPS = new int[] {Card.SUIT_CLUBS, Card.SUIT_SPADES, Card.SUIT_DIAMONDS, Card.SUIT_HEARTS};
	private static int sTrumpOffset = 0;
	
	public Round(Player dealer) {
		totalPlays = 0;
		tricks = new ArrayList<Play[]>();
		alone = false;
		trump = TRUMPS[sTrumpOffset % TRUMPS.length];
		trump++;
		Log.d("JMATT", "Trump is: " + new Card(TRUMPS[sTrumpOffset], Card.VALUE_ACE).toString());
		this.dealer = dealer;
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
}
