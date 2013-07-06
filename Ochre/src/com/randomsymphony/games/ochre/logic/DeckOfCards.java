package com.randomsymphony.games.ochre.logic;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;

import com.randomsymphony.games.ochre.model.Card;

public class DeckOfCards {

	private ArrayList<Card> mDeck = new ArrayList<Card>();
	private final Random mRandomizer = new Random();
	private Card[] ALL_CARDS = new Card[] {
		new Card(Card.SUIT_CLUBS, Card.VALUE_NINE),
		new Card(Card.SUIT_CLUBS, Card.VALUE_TEN),
		new Card(Card.SUIT_CLUBS, Card.VALUE_JACK),
		new Card(Card.SUIT_CLUBS, Card.VALUE_QUEEN),
		new Card(Card.SUIT_CLUBS, Card.VALUE_KING),
		new Card(Card.SUIT_CLUBS, Card.VALUE_ACE),
		
		new Card(Card.SUIT_HEARTS, Card.VALUE_NINE),
		new Card(Card.SUIT_HEARTS, Card.VALUE_TEN),
		new Card(Card.SUIT_HEARTS, Card.VALUE_JACK),
		new Card(Card.SUIT_HEARTS, Card.VALUE_QUEEN),
		new Card(Card.SUIT_HEARTS, Card.VALUE_KING),
		new Card(Card.SUIT_HEARTS, Card.VALUE_ACE),
		
		new Card(Card.SUIT_DIAMONDS, Card.VALUE_NINE),
		new Card(Card.SUIT_DIAMONDS, Card.VALUE_TEN),
		new Card(Card.SUIT_DIAMONDS, Card.VALUE_JACK),
		new Card(Card.SUIT_DIAMONDS, Card.VALUE_QUEEN),
		new Card(Card.SUIT_DIAMONDS, Card.VALUE_KING),
		new Card(Card.SUIT_DIAMONDS, Card.VALUE_ACE),
		
		new Card(Card.SUIT_SPADES, Card.VALUE_NINE),
		new Card(Card.SUIT_SPADES, Card.VALUE_TEN),
		new Card(Card.SUIT_SPADES, Card.VALUE_JACK),
		new Card(Card.SUIT_SPADES, Card.VALUE_QUEEN),
		new Card(Card.SUIT_SPADES, Card.VALUE_KING),
		new Card(Card.SUIT_SPADES, Card.VALUE_ACE),
	};
	
	public DeckOfCards() {
		shuffle();
	}
	
	public void shuffle() {
		mDeck.clear();
		for (int ptr = 0; ptr < ALL_CARDS.length; ptr++) {
			mDeck.add(ALL_CARDS[ptr]);
		}
		
		// randomize
		for (int ptr = 0, limit = mDeck.size(); ptr < limit; ptr++) {
			// the ptr position serves as the divider between the randomized
			// and unrandomized portion of the deck.
			int rand = mRandomizer.nextInt(limit - ptr);
			Card card = mDeck.remove(ptr + rand);
			mDeck.add(ptr, card);
		}
	}
	
	/**
	 * Deal a number of cards from the deck.
	 * @param howMany The number of cards you would like.
	 * @return An array of size equal to howMany or fewer if there are
	 * not that many cards remaining.
	 */
	public Card[] deal(int howMany) {
		Card[] dealtCards = new Card[howMany];
		
		for (int ptr = 0; ptr < howMany; ptr++) {
			dealtCards[ptr] = mDeck.remove(0);
		}
		
		return dealtCards;
	}
}
