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
		
		for (int ptr = 0; ptr < players.length * 2; ptr++) {
			dealToPlayer(cards, players[ptr % players.length]);
		}
	}
	
	public static boolean isTrump(int trump, Card card) {
		return card.getSuit() == trump || isLeftBauer(trump, card);
	}
	
	public static boolean isRightBauer(int trump, Card card) {
		return card.getValue() == Card.VALUE_JACK && card.getSuit() == trump;
	}
	
	public static boolean isLeftBauer(int trump, Card card) {
		return card.getValue() == Card.VALUE_JACK && card.getSuit() != trump &&
				((trump == Card.SUIT_DIAMONDS && card.getSuit() == Card.SUIT_HEARTS) ||
						(trump == Card.SUIT_HEARTS && card.getSuit() == Card.SUIT_DIAMONDS) ||
						(trump == Card.SUIT_CLUBS && card.getSuit() == Card.SUIT_SPADES) ||
						(trump == Card.SUIT_SPADES && card.getSuit() == Card.SUIT_CLUBS));
	}
	
	public static boolean isGreater(Card left, Card right, int leadSuit, int trump) {
		boolean rightIsTrump = isTrump(trump, right);
		boolean leftIsTrump = isTrump(trump, left);
		
		if (rightIsTrump || leftIsTrump) {
			// special rules for trump
			if (rightIsTrump != leftIsTrump) {
				// only one card is trump
				return leftIsTrump;
			} else {
				// they're both trump
				if (left.getValue() == Card.VALUE_JACK || right.getValue() == Card.VALUE_JACK) {
					// one of the cards is a jack, special rules apply
					if (isRightBauer(trump, left)) {
						// right bauer always wins
						return true;
					} else if (isRightBauer(trump, right)) {
						// left loses to the right bauer on the right
						return false;
					} else {
						// neither is the right bauer, only one card is a jack,
						// whichever is the left bauer wins
						return isLeftBauer(trump, left);
					}
				} else {
					// no jacks, this is easy
					return left.getValue() > right.getValue();
				}
			}
		} else {
			// no trump, easier scoring
			
			// if suits are the same, whichever is greater
			if (left.getSuit() == right.getSuit()) {
				return left.getValue() > right.getValue();
			} else {
				// suits are different
				if (left.getSuit() == leadSuit || right.getSuit() == leadSuit) {
					return left.getSuit() == leadSuit;
				} else {
					// neither is the lead suit, scoring doesn't really matter
					// because whoever played the lead card will beat both of
					// these cards
					return true;
				}
			}
		}
	}
}
