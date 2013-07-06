package com.randomsymphony.games.ochre.logic;

import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;

public class GamePlayUtils {

	public static final int MAX_CARDS = 5;
	
	/**
	 * Do not instantiate!
	 */
	private GamePlayUtils() {
		
	}
	
	public static void dealToPlayer(DeckOfCards cards, Player player) {
		int numCards = player.getCurrentCards().length;
		Card[] newCards;
		if (numCards == 0) {
			// no cards dealt yet
			boolean twoCards = Math.round(Math.random()) == 0;
			newCards = cards.deal(twoCards ? 2 : 3);
		} else {
			newCards = cards.deal(MAX_CARDS - numCards);
		}
		for (int ptr = 0; ptr < newCards.length; ptr++) {
			player.addCard(newCards[ptr]);
		}
	}
	
	public static void dealHand(DeckOfCards cards, Player[] players) {
		cards.shuffle();
		
		for (int ptr = 0; ptr < 8; ptr++) {
			dealToPlayer(cards, players[ptr % players.length]);
		}
	}
	
}
