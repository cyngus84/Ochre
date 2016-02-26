package com.randomsymphony.games.ochre.model;

import java.util.ArrayList;
import java.util.UUID;

public class Player {

	public interface ChangeListener {
		public void onNameChange(String newName);
	}

	private String mName;
	private ArrayList<Card> mCards = new ArrayList<Card>();
	private final UUID mInstanceId;
	private ArrayList<ChangeListener> mListeners = new ArrayList<ChangeListener>();

	public Player(String name) {
		this(name, new Card[0]);
	}
	
	public Player(String name, Card[] cards) {
		this(name, cards, UUID.randomUUID());
	}
	
	public Player(String name, Card[] cards, UUID playerId) {
		mName = name;
		for (int ptr = 0, limit = cards.length; ptr < limit; ptr++) {
			mCards.add(cards[ptr]);
		}
		mInstanceId = UUID.fromString(playerId.toString());
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
		notifyListeners();
	}
	
	public Card[] getCurrentCards() {
		Card[] cards = new Card[mCards.size()];
		for (int ptr = 0, limit = mCards.size(); ptr < limit; ptr++) {
			cards[ptr] = mCards.get(ptr);
		}
		return cards;
	}
	
	public void removeCard(Card card) {
		mCards.remove(card);
	}
	
	public void addCard(Card card) {
		mCards.add(card);
	}
	
	public void hideCards() {
		for (int ptr = 0, limit = mCards.size(); ptr < limit; ptr++) {
			mCards.get(ptr).setVisible(false);
		}
	}

	public void addListener(ChangeListener listener) {
		mListeners.add(listener);
	}

	public void removeListener(ChangeListener listener) {
		mListeners.remove(listener);
	}

	public void discardHand() {
		mCards.clear();
	}
	
	public void discardCard(Card card) {
		mCards.remove(card);
	}
	
	public String getId() {
		return mInstanceId.toString();
	}

	@Override
	public int hashCode() {
		return mInstanceId.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player)) {
			return false;
		}

		Player that = (Player) o;
		return that.hashCode() == this.hashCode();
	}

	private void notifyListeners() {
		for (int ptr = 0, limit = mListeners.size(); ptr < limit; ptr++) {
			mListeners.get(ptr).onNameChange(mName);
		}
	}
}
