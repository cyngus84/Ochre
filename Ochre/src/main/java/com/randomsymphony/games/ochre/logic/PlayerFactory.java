package com.randomsymphony.games.ochre.logic;

import com.randomsymphony.games.ochre.model.Player;

public class PlayerFactory {

	private static String[] NAMES = new String[] {"Player 1", "Player 2", "Player 3", "Player 4"};
	private int mOffset = -1;
	
	public PlayerFactory() {
	}

	public Player createPlayer() {
		mOffset++;
		return new Player(NAMES[mOffset]);
	}
}
