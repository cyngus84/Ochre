package com.randomsymphony.games.ochre.transport;

import java.io.OutputStream;

import com.randomsymphony.games.ochre.logic.GameState;

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
		return null;
	}

	@Override
	public void deflateState(GameState state, OutputStream output) {
	}
}
