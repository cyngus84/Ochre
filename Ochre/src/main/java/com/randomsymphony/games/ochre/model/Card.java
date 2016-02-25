package com.randomsymphony.games.ochre.model;

import com.randomsymphony.games.ochre.R;

import android.content.res.Resources;
import android.widget.Button;

public class Card {

	public static final int SUIT_HEARTS = 0;
	public static final int SUIT_SPADES = 1;
	public static final int SUIT_CLUBS = 2;
	public static final int SUIT_DIAMONDS = 3;
	
	public static final int VALUE_NINE = 0;
	public static final int VALUE_TEN = 1;
	public static final int VALUE_JACK = 2;
	public static final int VALUE_QUEEN = 3;
	public static final int VALUE_KING = 4;
	public static final int VALUE_ACE = 5;
	
	private final int mSuit;
	private final int mValue;
	private boolean mVisible = false;
	
	/**
	 * Create a card with the specified values which is not visible.
	 */
	public Card(int suit, int value) {
		mSuit = suit;
		mValue = value;
	}
	
	public Card(int suit, int value, boolean visible) {
		this(suit, value);
		mVisible = visible;
	}
	
	public int getSuit() {
		return mSuit;
	}
	
	public int getValue() {
		return mValue;
	}
	
	public boolean isVisible() {
		return mVisible;
	}
	
	public void setVisible(boolean isVisible) {
		mVisible = isVisible;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		switch (mValue) {
	        case VALUE_NINE:
		        sb.append("9");
		        break;
	        case VALUE_TEN:
		        sb.append("10");
		        break;
	        case VALUE_JACK:
		        sb.append("J");
		        break;
	        case VALUE_QUEEN:
		        sb.append("Q");
		        break;
	        case VALUE_KING:
		        sb.append("K");
		        break;
	        case VALUE_ACE:
		        sb.append("A");
		        break;
		}
		
		sb.append("  ");
		
		switch (mSuit) {
		    case SUIT_CLUBS:
			    sb.append("\u2663");
			    break;
		    case SUIT_HEARTS:
			    sb.append("\u2764");
			    break;
		    case SUIT_DIAMONDS:
			    sb.append("\u2666");
			    break;
		    case SUIT_SPADES:
			    sb.append("\u2660");
			    break;
		}
		
		return sb.toString();
	}

	/**
	 * Does not consider whether or not the card is visible.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mSuit;
		result = prime * result + mValue;
		return result;
	}

	/**
	 * Does not consider whether or not the card is visible.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		if (mSuit != other.mSuit)
			return false;
		if (mValue != other.mValue)
			return false;
		return true;
	}
	
	public static void formatButtonAsCard(Button button, Card card, Resources res) {
		// take the number of the card, leaving the suit behind
		button.setText(card.toString().split(" ")[0]);

		switch (card.getSuit()) {
			case Card.SUIT_CLUBS:
				button.setBackgroundResource(R.drawable.club);
				break;
			case Card.SUIT_DIAMONDS:
				button.setBackgroundResource(R.drawable.diamond);
				break;
			case Card.SUIT_HEARTS:
				button.setBackgroundResource(R.drawable.heart);
				break;
			case Card.SUIT_SPADES:
				button.setBackgroundResource(R.drawable.spade);
				break;
		}
	}
}
