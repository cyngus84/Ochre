package com.randomsymphony.games.ochre.transport.json;

import java.io.IOException;
import java.util.ArrayList;

import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Play;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.model.Round;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

/**
 * @author cyngus
 *
 * A RoundConverter deals with JSON serialization and deserialization of
 * Rounds.
 */
public class RoundConverter {

	private static final String TAG_TRUMP = "trump_suit";
	private static final String TAG_TRICKS = "played_tricks";
	private static final String TAG_ALONE = "going_alone";
	private static final String TAG_DEALER = "round_dealer";
	private static final String TAG_MAKER = "round_maker";
	private static final String TAG_TOTAL_PLAYS = "total_plays";
	private static final String TAG_TRUMP_PASSES = "trump_passes";
	private static final String TAG_CAPTURED_TRICKS = "player_trick_count";
	private static final String TAG_VERSION = "version";
	private static final int CURRENT_VERSION = 1;
	
	private ConverterFactory mConvFactory;
	private CardConverter mCardConverter = null;
	private PlayConverter mPlayConverter = null;
	
	public RoundConverter(ConverterFactory converterFactory) {
		mConvFactory = converterFactory;
	}
	
	public Round readRound(JsonReader source) {
		return readRound(source, new Player[0]);
	}
	
	private boolean isCompatible(int version) {
		return CURRENT_VERSION == version;
	}
	
	/**
	 * Read the round from Json-encoded data
	 * @param source the json data source
	 * @param players the players to assign to captured tricks and plays.
	 * If a player id is read from the JSON which is not present in this array,
	 * an empty player with only the player ID assigned will be allocated,
	 * otherwise the passed-in instance will be used in creating the data
	 * model.
	 * @return An inflated round.
	 */
	public Round readRound(JsonReader source, Player[] players) {
		// in this method we have three major phases
		//   1) Allocate local members to hold read values
		//   2) Read values from JSON
		//   3) Assemble read values into a Round object
		
		Round round = null;
		int version = -1;
		Card trumpSuit = null;
		boolean alone = false;
		ArrayList<Play[]> roundPlays = new ArrayList<Play[]>();
		String dealerId = null;
		String makerId = null;
		int totalPlays = 0;
		int trumpPasses = 0;
		ArrayList<Object> capturedTricks = new ArrayList<Object>();
		
		try {
			JsonToken nextToken = source.peek();
			if (nextToken != JsonToken.BEGIN_OBJECT) {
				throw new IllegalArgumentException("Expected beginning of " +
						"object, instead got: " + nextToken.toString());
			}
			
			source.beginObject();
			if (TAG_VERSION.equals(source.nextName())) {
				version = source.nextInt();
				if (!isCompatible(version)) {
					throw new IllegalArgumentException("Version " + version + " is not " + 
							" compatible with this decoder.");
				}
			} else {
				throw new IllegalArgumentException("Round is malformed, " +
						"record must begin with a version number.");
			}
			
			while (source.peek() != JsonToken.END_OBJECT) {
				String name = source.nextName();
				if (TAG_TRUMP.equals(name)) {
					CardConverter cardConverter = getCardConverter();
					trumpSuit = cardConverter.readCard(source);
				} else if (TAG_TRICKS.equals(name)) {
					// the plays are an array of arrays. Each nested array
					// represents a trick.
					PlayConverter playConverter = getPlayConverter();
					source.beginArray();
					while (source.hasNext()) {
						source.beginArray();
						ArrayList<Play> plays = new ArrayList<Play>();
						
						// decode all the plays
						while (source.hasNext()) {
							Play play = playConverter.readPlay(source, players);
							plays.add(play);
						}
						
						Play[] copy = new Play[plays.size()];
						Log.d("JMATT", "Reading " + copy.length + " plays");
						
						// add the list as an array to the list of tricks
						roundPlays.add((Play[]) plays.toArray(copy));
						
						source.endArray();
					}
					source.endArray();
				} else if (TAG_ALONE.equals(name)) {
					alone = source.nextBoolean();
				} else if (TAG_DEALER.equals(name)) {
					dealerId = source.nextString();
				} else if (TAG_MAKER.equals(name)) {
					makerId = source.nextString();
				} else if (TAG_TOTAL_PLAYS.equals(name)) {
					totalPlays = source.nextInt();
				} else if (TAG_TRUMP_PASSES.equals(name)) {
					trumpPasses = source.nextInt();
				} else if (TAG_CAPTURED_TRICKS.equals(name)) {
					source.beginArray();
					
					while (source.hasNext()) {
						if (capturedTricks.size() % 2 == 0) {
							capturedTricks.add(source.nextString());
						} else {
							capturedTricks.add(Integer.valueOf(source.nextInt()));
						}
					}
					
					source.endArray();
				} else {
					throw new IllegalArgumentException("Unknown property, name: " + name);
				}
			}
			source.endObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// find the dealer
		Player dealer = null;
		Player maker = null;
		for (int ptr = 0; ptr < players.length; ptr++) {
			if (dealerId.equals(players[ptr].getId())) {
				dealer = players[ptr];
			}
			
			if (makerId.equals(players[ptr].getId())) {
				maker = players[ptr];
			}
			
			if (dealer != null && maker != null) {
				break;
			}
		}
		if (dealer == null) {
			Log.w("JMATT", "Dealer is null, creating player by ID for dealer");
			dealer = new Player(dealerId);
		}
		
		if (maker == null) {
			Log.w("JMATT", "Maker is null, creating player by ID for maker.");
			maker = new Player(makerId);
		}
		
		// assign simple properties
		round = new Round(dealer);
		round.maker = maker;
		round.alone = alone;
		round.totalPlays = totalPlays;
		round.trumpPasses = trumpPasses;
		round.trump = trumpSuit;
		round.maker = maker;
		round.tricks = roundPlays;

		// call methods to do more advanced modifications
		validateTricks(round);
		addCapturedTricks(round, players, capturedTricks);
		
		return round;
	}
	
	public void writeRound(JsonWriter writer, Round round) throws IOException {
		writer.beginObject();
		
		writer.name(TAG_VERSION).value(CURRENT_VERSION);
		
		// write the trump card
		writer.name(TAG_TRUMP);
		CardConverter cardCrusher = 
				(CardConverter) mConvFactory.getConverter(JsonConverterFactory.TYPE_CARD);
		cardCrusher.writeCard(writer, round.trump);
		
		// write the currently played tricks
		writer.name(TAG_TRICKS);
		writer.beginArray();
		PlayConverter playEncoder = 
				(PlayConverter) mConvFactory.getConverter(JsonConverterFactory.TYPE_PLAY);
		// track the number of plays we've written so we don't try to encode
		// empty plays
		int encodedPlayCount = 0;
		for (int ptr = 0; ptr < round.tricks.size(); ptr++) {
			Play[] plays = round.tricks.get(ptr);
			writer.beginArray();

			for (int playPtr = 0; playPtr < plays.length && encodedPlayCount < round.totalPlays;
					playPtr++, encodedPlayCount++) {
				Play target = plays[playPtr];
				Log.d("JMATTz", target.card.toString());
				Log.d("JMATTz", target.player.toString());
				playEncoder.writePlay(writer, target);
			}
			writer.endArray();
		}
		writer.endArray();
		
		writer.name(TAG_ALONE).value(round.alone);
		
		// only write the player id, during deserialization we'll pick from
		// a pool of players, or generate a stub player
		writer.name(TAG_DEALER).value(round.dealer.getId());
		
		// only write the player id, during deserialization we'll pick from
		// a pool of players, or generate a stub player
		writer.name(TAG_MAKER).value(round.maker.getId());
		
		writer.name(TAG_TOTAL_PLAYS).value(round.totalPlays);
		
		writer.name(TAG_TRUMP_PASSES).value(round.trumpPasses);
		
		writer.name(TAG_CAPTURED_TRICKS);
		// structure the captured tricks as an array where a player ID is
		// followed by her captured trick count, so there player IDs are
		// at positions where (position % 2) == 0 and the number of captured
		// tricks are where (position % 2) == 1
		writer.beginArray();
		for (Player player : round.mCapturedTrickCount.keySet()) {
			writer.value(player.getId()).value(round.mCapturedTrickCount.get(player));
		}
		writer.endArray();
		
		writer.endObject();
	}
	
	/**
	 * Given a {@link Round}, all the trick records look correct. The number
	 * of plays in a trick might be incorrect because we avoid encoding null
     * Plays when the round is serialized to JSON.
	 * @param round The round to validate and fix as needed
	 */
	private void validateTricks(Round round) {
        ArrayList<Play[]> tricks = round.tricks;
        int activePlayers = round.getActivePlayerCount();

        for (int ptr = 0, limit = tricks.size(); ptr < limit; ptr++) {
            Play[] currentTrick = tricks.get(ptr);

            // if the trick play length equals the number of players, we're
            // fine, otherwise we need to fix things up.
            if (currentTrick.length == activePlayers) {
                continue;
            }

            // first remove the current trick
            round.tricks.remove(round.tricks.size() - 1);

            // now add it back as the right size
            Play[] updatedTrick = new Play[activePlayers];
            round.tricks.add(updatedTrick);

            // replay now as though we added naturally
            round.totalPlays -= currentTrick.length;
            for (int ptr2 = 0; ptr2 < currentTrick.length && currentTrick[ptr2] != null; ptr2++) {
                round.addPlay(currentTrick[ptr2]);
            }
        }
    }
	
	
	/**
	 * Add the captured trick counts to the round by looking through the list
	 * of playerId-count pairs in the encodedTricks list.
	 * @param round The round to add trick counts to
	 * @param players The players in the round so that Player objects can be
	 * awarded tricks.
	 * @param encodedTricks The list of playerId-count pairs where playerIds
	 * are at even-indexed positions and counts are at odd-indexed positions,
	 * indices zero and two are considered "even".
	 */
	private void addCapturedTricks(Round round, Player[] players, 
			ArrayList<Object> encodedTricks) {
		if (encodedTricks.size() % 2 != 0) {
			Log.w("JMATT", "Captured tricks seems unbalanced, trying anyway.");
		}
		
		// go through the list of captured trick counts, removing as we go
		while (encodedTricks.size() > 1) {
			String playerId = (String) encodedTricks.remove(0);
			Integer count = (Integer) encodedTricks.remove(0);
			
			Player target = null;
			for (int ptr = 0; ptr < players.length; ptr++) {
				if (playerId.equals(players[ptr].getId())) {
					target = players[ptr];
					break;
				}
			}
			
			if (target != null) {
				round.mCapturedTrickCount.put(target, count);
			}
		}
	}
	
	private CardConverter getCardConverter() {
		if (mCardConverter == null) {
			mCardConverter = 
					(CardConverter) mConvFactory.getConverter(JsonConverterFactory.TYPE_CARD);
		}
		return mCardConverter;
	}
	
	private PlayConverter getPlayConverter() {
		if (mPlayConverter == null) {
			mPlayConverter =
					(PlayConverter) mConvFactory.getConverter(JsonConverterFactory.TYPE_PLAY);
		}
		
		return mPlayConverter;
	}
}
