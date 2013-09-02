package com.randomsymphony.games.ochre.transport.json;

import java.io.IOException;
import java.util.UUID;

import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Play;
import com.randomsymphony.games.ochre.model.Player;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

public class PlayConverter {

	private static final String TAG_CARD = "play_card";
	private static final String TAG_PLAYER = "player_id";
	private static final String TAG_VERSION = "version";
	private static final int CURRENT_VERSION = 1;
	
	private ConverterFactory mFactory;
	
	public PlayConverter(ConverterFactory converters) {
		mFactory = converters;
	}
	
	public void writePlay(JsonWriter writer, Play play) throws IOException {
		writer.beginObject();
		
		writer.name(TAG_VERSION).value(CURRENT_VERSION);
		writer.name(TAG_CARD);
		((CardConverter) mFactory.getConverter(JsonConverterFactory.TYPE_CARD)).writeCard(
				writer, play.card);

		// only write the player id, during deserialization we'll pick from
		// a pool of players, or generate a stub player
		writer.name(TAG_PLAYER).value(play.player.getId());
		
		writer.endObject();
	}
	
	/**
	 * @param players Array of players which can be assigned to the play. If
	 * null or the array does not contain the player in question, a stub player
	 * with just {@link Player#getId()} set will be returned.
	 */
	public Play readPlay(JsonReader reader, Player[] players) {
		Play rtnValue = null;
		String playerId = null;
		Card card = null;
		int version;

		try {
			JsonToken nextToken = reader.peek();
			if (nextToken != JsonToken.BEGIN_OBJECT) {
				throw new IllegalArgumentException("Expected beginning of " +
						"object, instead got: " + nextToken.toString());
			}
			reader.beginObject();
			
			String nextProp = reader.nextName();
			if (TAG_VERSION.equals(nextProp)) {
				version = reader.nextInt();
			} else {
				throw new IllegalArgumentException("Play object must start with a version number" +
						" but instead starts with : " + nextProp);
			}
			
			while (reader.peek() != JsonToken.END_OBJECT) {
				nextProp = reader.nextName();
				
				if (TAG_CARD.equals(nextProp)) {
					card = ((CardConverter) mFactory.getConverter(JsonConverterFactory.TYPE_CARD))
							.readCard(reader);
				} else if (TAG_PLAYER.equals(nextProp)) {
					playerId = reader.nextString();
				} else {
					throw new IllegalArgumentException("Unknown property: " + nextProp + 
							" when reading play.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		if (playerId == null || card == null) {
			throw new IllegalArgumentException("Play record is missing properties.");
		}
		
		
		Player player = null;
		
		// see if the player identified in the record matches any of the
		// players in our list.
		if (players != null) {
			for (int ptr = 0; ptr < players.length; ptr++) {
				if (players[ptr].getId().equals(playerId)) {
					player = players[ptr];
				}
			}
		}
		
		if (player == null) {
			player = new Player(null, new Card[0], UUID.fromString(playerId));
		}
		
		rtnValue = new Play(player, card);
		return rtnValue;
	}
	
	public Play readPlay(JsonReader reader) {
		Player[] players = new Player[0];
		return readPlay(reader, players);
	}
}
