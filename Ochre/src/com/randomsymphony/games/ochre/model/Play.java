package com.randomsymphony.games.ochre.model;

/**
 * A play by a player, just a reference to the card and the player who laid it.
 */
public class Play {
	public final Player player;
	public final Card card;
	
	public Play(Player player, Card card) {
		this.player = player;
		this.card = card;
	}
}
