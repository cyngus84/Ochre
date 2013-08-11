package com.randomsymphony.games.ochre.transport.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;

import android.util.JsonReader;
import android.util.JsonToken;

public class PlayerConverter {

	private ConverterFactory mFactory;
	private static final String TAG_NAME = "name";
	private static final String TAG_ID = "id";
	private static final String TAG_CARDS = "cards";
	
	/**
	 * Gets converters for other types
	 * @param converterFactory
	 */
	public PlayerConverter(ConverterFactory converterFactory) {
		mFactory = converterFactory;
	}
	
	/**
	 * The JsonReader should be positioned such that it is about to begin the
	 * player object. Likely this means whoever is invoking this method used
	 * {@link JsonReader#peek()} to determine this call should be made.
	 */
	public Player readPlayer(JsonReader source) {
		String playerName = null;
		UUID playerId = null;
		ArrayList<Card> cards = null;
		
		try {
			JsonToken nextToken = source.peek();
			if (nextToken != JsonToken.BEGIN_OBJECT) {
				throw new IllegalArgumentException("Expected beginning of " +
						"object, instead got: " + nextToken.toString());
			}
			source.beginObject();
			
			while(source.peek() != JsonToken.END_OBJECT) {
				String nextProp = source.nextName();
				if (TAG_NAME.equals(nextProp)) {
					playerName = source.nextString();
				} else if (TAG_ID.equals(nextProp)) {
					playerId = UUID.fromString(source.nextString());
				} else if (TAG_CARDS.equals(nextProp)) {
					cards = readCardArray(source);
				} else {
					throw new IllegalArgumentException("Unknown property '" +
					nextProp + "' in player object.");
				}
			}
			source.endObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Player player;
		
		if (playerName != null) {
			if (cards != null) {
				Card[] cardsArray = new Card[cards.size()];
				cardsArray = cards.toArray(cardsArray);
				if (playerId != null) {
					player = new Player(playerName, cardsArray, playerId);
				} else {
					player = new Player(playerName, cardsArray);
				}
			} else {
				player = new Player(playerName);
			}
		} else {
			throw new IllegalArgumentException("Player has no name");
		}
		
		return player;
	}
	
	private ArrayList<Card> readCardArray(JsonReader source)
			throws IOException {
		ArrayList<Card> cards = new ArrayList<Card>(5);
		CardConverter cardConverter = (CardConverter) mFactory.getConverter(
				JsonConverterFactory.TYPE_CARD);
		try {
			if (source.peek() != JsonToken.BEGIN_ARRAY) {
				throw new IllegalArgumentException("Card property of player " +
						" is malformed.");
			}
			
			source.beginArray();
			while (source.peek() != JsonToken.END_ARRAY) {
				if (source.peek() != JsonToken.BEGIN_OBJECT) {
					throw new IllegalArgumentException("Card property of" +
							"  player is malformed.");
				}
				cards.add(cardConverter.readCard(source));
			}
			source.endArray();
		} catch (IOException e) {
			throw e;
		}
		
		return cards;
	}
}
