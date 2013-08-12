package com.randomsymphony.games.ochre.transport.json;

import java.io.IOException;

import com.randomsymphony.games.ochre.model.Card;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

public class CardConverter {
	
	private static final String TAG_SUIT = "suit";
	private static final String TAG_VALUE = "value";
	private static final String TAG_VISIBLE = "visible";
	
	public CardConverter(ConverterFactory converters) {
		
	}
	
	public Card readCard(JsonReader source) {
		int value = -1;
		int suit = -1;
		boolean visible = false;
		
		Card card = null;
		
		try {
			JsonToken nextToken = source.peek();
			if (nextToken != JsonToken.BEGIN_OBJECT) {
				throw new IllegalArgumentException("Expected beginning of " +
						"object, instead got: " + nextToken.toString());
			}
			source.beginObject();
			while(source.peek() != JsonToken.END_OBJECT) {
				String nextProp = source.nextName();
				if (TAG_SUIT.endsWith(nextProp)) {
					suit = source.nextInt();
				} else if (TAG_VALUE.equals(nextProp)) {
					value = source.nextInt();
				} else if (TAG_VISIBLE.equals(nextProp)) {
					visible = source.nextBoolean();
				} else {
					throw new IllegalArgumentException("Unknown property '" + 
					        nextProp + "' in card object.");
				}
			}
			source.endObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		// validate that required properties were found, be lenient about
		// presence of the visibility tag, card visibility is mostly going
		// to be controlled by local logic
		if (value == -1 || suit == -1) {
			throw new IllegalArgumentException("Required fields not present," +
					" deserialization of card failed.");
		}
		card = new Card(suit, value, visible);
		return card;
	}
}
