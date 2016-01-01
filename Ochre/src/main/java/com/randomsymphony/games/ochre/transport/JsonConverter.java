package com.randomsymphony.games.ochre.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.transport.json.GameStateConverter;
import com.randomsymphony.games.ochre.transport.json.JsonConverterFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.JsonWriter;

public class JsonConverter implements
        GameDeflator<OutputStream>, GameInflater<JsonReader> {
	
	private String mGameName;
	
	/**
	 * The name of the file to write the SharedPreferences to.
	 * @param context
	 * @param gameName
	 */
	public JsonConverter(Context context, String gameName) {
		mGameName = gameName;
	}
	
	@Override
	public GameState inflate(JsonReader source) {
		return new GameStateConverter(new JsonConverterFactory()).readGameState(source);
	}

	@Override
	public void deflateState(GameState state, OutputStream output) {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(output));
		try {
			new GameStateConverter(new JsonConverterFactory()).writeGameState(writer, state);
		} catch (IOException e) {

		}
	}
}
