package com.randomsymphony.games.ochre.model;

import java.util.ArrayList;

public class Round {
	public int trump;
	public ArrayList<Play[]> tricks;
	public boolean alone;
	public final Player dealer;
	public Player maker;
	public int totalPlays;
	
	public Round(Player dealer) {
		totalPlays = 0;
		tricks = new ArrayList<Play[]>();
		alone = false;
		trump = -1;
		this.dealer = dealer;
	}
	
	public void addPlay(Play play) {
		// TODO check if this player has already played and if so, throw
		Play[] currentTrick = tricks.get(tricks.size() - 1);
		currentTrick[totalPlays % currentTrick.length] = play;
		totalPlays++;
	}
}
