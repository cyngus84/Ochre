package com.randomsymphony.games.ochre.model;

import java.util.ArrayList;

import android.util.Log;

public class Player {

	private String mName;
	private ArrayList<Card> mCards = new ArrayList<Card>();
	
	public Player(String name) {
		mName = name;
	}
	
	public Player(String name, Card[] cards) {
		this(name);
		for (int ptr = 0, limit = cards.length; ptr < limit; ptr++) {
			mCards.add(cards[ptr]);
		}
	}

	public String getName() {
		return mName;
	}
	
	public Card[] getCurrentCards() {
		Card[] cards = new Card[mCards.size()];
		for (int ptr = 0, limit = mCards.size(); ptr < limit; ptr++) {
			cards[ptr] = mCards.get(ptr);
		}
		Log.d("JMATT", mName + " has " + cards.length + " cards");
		return cards;
	}
	
	public void removeCard(Card card) {
		throw new RuntimeException();
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
}
