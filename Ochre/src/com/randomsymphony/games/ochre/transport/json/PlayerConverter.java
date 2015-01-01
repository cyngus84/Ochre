package com.randomsymphony.games.ochre.transport.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

public class PlayerConverter {

	private ConverterFactory mFactory;
	private static final String TAG_NAME = "name";
	private static final String TAG_ID = "id";
	private static final String TAG_CARDS = "cards";
	private static final String TAG_VERSION = "version";
	private static final int CURRENT_VERSION = 1;
	
	/**
	 * Gets converters for other types
	 * @param converterFactory
	 */
	public PlayerConverter(ConverterFactory converterFactory) {
		mFactory = converterFactory;
	}
	
	public void writePlayer(JsonWriter writer, Player player) 
			throws IOException {
		writer.beginObject();

		writer.name(TAG_VERSION).value(CURRENT_VERSION);
		writer.name(TAG_NAME).value(player.getName());
		writer.name(TAG_ID).value(player.getId());

		writer.name(TAG_CARDS);
		writer.beginArray();
		CardConverter cardWriter = (CardConverter) mFactory.getConverter(
				JsonConverterFactory.TYPE_CARD);

		Card[] hand = player.getCurrentCards();
		for (int ptr = 0; ptr < hand.length; ptr++) {
			cardWriter.writeCard(writer, hand[ptr]);
		}
		writer.endArray();

		writer.endObject();
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
		int version = 0;
		
		try {
			JsonToken nextToken = source.peek();
			if (nextToken != JsonToken.BEGIN_OBJECT) {
				throw new IllegalArgumentException("Expected beginning of " +
						"object, instead got: " + nextToken.toString());
			}
			source.beginObject();
			
			String nextProp = source.nextName();
			if (TAG_VERSION.equals(nextProp)) {
				version = source.nextInt();
			} else {
				throw new IllegalArgumentException("Player is malformed, " +
						"record must begin with a version number");
			}
			
			while(source.peek() != JsonToken.END_OBJECT) {
				nextProp = source.nextName();
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
		
		if (CURRENT_VERSION != version) {
			throw new IllegalArgumentException("Version " + version + " is " +
					"not supported by this inflater.");
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
