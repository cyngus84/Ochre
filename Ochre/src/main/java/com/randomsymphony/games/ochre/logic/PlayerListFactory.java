package com.randomsymphony.games.ochre.logic;

import java.util.List;

import com.randomsymphony.games.ochre.model.Player;

public class PlayerListFactory extends PlayerFactory {

	private List<Player> mPlayers;
	private int mOffset = -1;
	
	public PlayerListFactory(List<Player> players) {
		mPlayers = players;
	}
	
	/**
	 * @return The new {@link Player} or null if none is available.
	 */
	@Override
	public Player createPlayer() {
		mOffset++;
		if (mOffset >= mPlayers.size()) {
			return null;
		} else {
			return mPlayers.get(mOffset);
		}
	}
}
