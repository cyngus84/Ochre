package com.randomsymphony.games.ochre.transport.json;

import java.io.IOException;
import java.util.ArrayList;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.logic.GameState.Phase;
import com.randomsymphony.games.ochre.logic.PlayerListFactory;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.model.Round;

public class GameStateConverter {
	private static final String TAG_VERSION = "version";
	private static final int CURRENT_VERSION = 1;
	private static final String TAG_PLAYERS = "game_players";
	private static final String TAG_SCORES = "player_scores";
	private static final String TAG_ROUNDS = "game_rounds";
	private static final String TAG_PHASE = "game_phase";
	
	private ConverterFactory mConvFactory;
	private PlayerConverter mPlayerConv;
	private RoundConverter mRoundConv;
	
	public GameStateConverter(ConverterFactory converterFactory) {
		mConvFactory = converterFactory;
	}

	public void writeGameState(JsonWriter writer, GameState gameState) throws IOException {
		initSubconverters();
		
		writer.beginObject();
		writer.name(TAG_VERSION).value(CURRENT_VERSION);
		
		// write players first since they will be used elsewhere during
		// deserialization
		writer.name(TAG_PLAYERS);
		writer.beginArray();
		Player[] players = gameState.getPlayers();
		for (int ptr = 0; ptr < players.length; ptr++) {
			mPlayerConv.writePlayer(writer, players[ptr]);
		}
		writer.endArray();
		
		// write scores as a series of playerId-score tuples in an array
		writer.name(TAG_SCORES);
		writer.beginArray();
		for (int ptr = 0; ptr < players.length; ptr++) {
			Player target = players[ptr];
			int score = gameState.getPointsForPlayer(target);
			writer.value(target.getId());
			writer.value(score);
		}
		writer.endArray();
		
		// write rounds
		writer.name(TAG_ROUNDS);
		writer.beginArray();
		Round[] rounds = gameState.getRounds();
		for (int ptr = 0; ptr < rounds.length; ptr++) {
			Round target = rounds[ptr];
			mRoundConv.writeRound(writer, target);
		}
		writer.endArray();
		
		writer.name(TAG_PHASE);
		writer.value(gameState.getGamePhase().ordinal());
		
		writer.endObject();
	}
	
	public GameState readGameState(JsonReader reader) {
		initSubconverters();
		GameState gameState;
		ArrayList<Round> gameRounds = new ArrayList<Round>();
		ArrayList<Object> scoreTuples = new ArrayList<Object>();
		ArrayList<Player> players = new ArrayList<Player>();
		Phase phase = Phase.NONE;
		int version = -1;
		
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
				throw new IllegalArgumentException("Game state is malformed, " +
						"record must begin with a version number");
			}
			
			if (version != CURRENT_VERSION) {
				throw new IllegalArgumentException("Version " + version + " is not supported.");
			}

			while (reader.peek() != JsonToken.END_OBJECT) {
				
				String nextTag = reader.nextName();
				if (TAG_PLAYERS.equals(nextTag)) {
					reader.beginArray();
					while(reader.hasNext()) {
						Player player = mPlayerConv.readPlayer(reader);
						players.add(player);
					}
					reader.endArray();
					
					Log.d("JMATT", "Read " + players.size() + " players.");
				} else if (TAG_ROUNDS.equals(nextTag)) {
					if (players.size() == 0) {
						Log.w("JMATT", "Players are not populated, things may get weird.");
					}
					
					reader.beginArray();
					Player[] playerArray = new Player[players.size()];
					players.toArray(playerArray);
					while (reader.hasNext()) {
						Round round = mRoundConv.readRound(reader, playerArray);
						gameRounds.add(round);
					}
					reader.endArray();
					
				} else if (TAG_SCORES.equals(nextTag)) {
					reader.beginArray();
					while (reader.hasNext()) {
						String playerId = reader.nextString();
						scoreTuples.add(playerId);
						if (!reader.hasNext()) {
							break;
						}
						Integer score = reader.nextInt();
						scoreTuples.add(score);
					}
					reader.endArray();
				} else if (TAG_PHASE.equals(nextTag)) {
					phase = Phase.values()[reader.nextInt()];
				}
				
			}
			
			reader.endObject();
		} catch (IOException e) {
			
		}
		
		
		// put the players into a player factory
		PlayerListFactory playerFactory = new PlayerListFactory(players);
		gameState = new GameState(playerFactory);
		gameState.setGamePhase(phase);

		// add the game rounds
		for (int ptr = 0; ptr < gameRounds.size(); ptr++) {
			Round target = gameRounds.get(ptr);
			gameState.addRound(target);
		}
		
		if (players.size() == 0) {
			Log.w("JMATT", "Players are not populated, scores will not be added.");
		}
		// process the score tuples and add player scores to the game state
		for (int ptr = 0; ptr + 2 <= scoreTuples.size(); ) {
			String playerId = (String) scoreTuples.get(ptr);
			ptr++;
			Integer score = (Integer) scoreTuples.get(ptr);
			ptr++;
			
			Player player = null;
			for (int pts = 0; pts < players.size(); pts++) {
				Player target = players.get(pts);
				if (players.get(pts).getId().equals(playerId)) {
					player = target;
					break;
				}
			}
			
			gameState.addPoints(player, score);
		}
		
		return gameState;
	}
	
	private void initSubconverters() {
		if (mPlayerConv == null) {
			mPlayerConv = 
					(PlayerConverter) mConvFactory.getConverter(JsonConverterFactory.TYPE_PLAYER);
		}
		
		if (mRoundConv == null) {
			mRoundConv = 
					(RoundConverter) mConvFactory.getConverter(JsonConverterFactory.TYPE_ROUND);
		}
	}
	
}
