package com.randomsymphony.games.ochre.model;

import java.util.ArrayList;
import java.util.UUID;

public class Player {

	private String mName;
	private ArrayList<Card> mCards = new ArrayList<Card>();
	private final UUID mInstanceId;
	
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
	
	public void discardHand() {
		mCards.clear();
	}
	
	public void discardCard(Card card) {
		mCards.remove(card);
	}
	
	public String getId() {
		return mInstanceId.toString();
	}
}
