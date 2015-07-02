package com.randomsymphony.games.ochre.logic;

import com.randomsymphony.games.ochre.model.Player;

public class PlayerFactory {

	private static String[] NAMES = new String[] {"Bob", "Agnes", "Lucy", "George"};
	private int mOffset = -1;
	
	public PlayerFactory() {
	}

	public Player createPlayer() {
		mOffset++;
		return new Player(NAMES[mOffset]);
	}
}
