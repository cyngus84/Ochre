package com.randomsymphony.games.ochre.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.randomsymphony.games.ochre.CardTableActivity;
import com.randomsymphony.games.ochre.fragment.GameState;
import com.randomsymphony.games.ochre.fragment.PlayerDisplay;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Play;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.model.Round;

import android.support.v4.app.Fragment;
import android.util.Log;

public class GameEngine extends Fragment {

	private CardTableActivity mCardTable;
	private HashMap<Integer, PlayerDisplay> mPlayerDisplays = new HashMap<Integer, PlayerDisplay>();
	private GameState mState;
	
	public void setPlayerDisplay(int player, PlayerDisplay display) {
		mPlayerDisplays.put(player, display);
	}
	
	public void setTableDisplay(CardTableActivity activity) {
		mCardTable = activity;
	}
	
	public void setGameState(GameState state) {
		mState = state;
	}
	
	public void startGame() {
		newRound();
	}
	
	public void newRound() {
		mState.getDeck().shuffle();
    	Player[] players = mState.getPlayers();
    	for (int ptr = 0; ptr < players.length; ptr++) {
    		players[ptr].discardHand();
    	}
    	
        GamePlayUtils.dealHand(mState.getDeck(), mState.getPlayers());
        
        // deal the possible trump card
        Card possibleTrump = mState.getDeck().deal(1)[0];
        mCardTable.setTrumpCard(possibleTrump);
        
        redrawAllPlayers();
		
		Round newRound = mState.createNewRound();
		// TODO this should really only be set after we know if a player
		// is going alone or not.
		newRound.tricks.add(new Play[4]);
		
		// speculatively set the trump
		newRound.trump = possibleTrump.getSuit();
		setPlayerDisplayEnabled(getNextPlayer(), true);
	}
	
	public static final int NUMBER_OF_TRICKS = 5;
	
	public void playCard(Player player, Card card) {
		Round currRound = mState.getCurrentRound();
		currRound.addPlay(new Play(player, card));
		Log.d("JMATT", player.getName() + " played " + card.toString());

		player.discardCard(card);
		// this is wasteful, would be better to just redraw one player
		redrawAllPlayers();
		mCardTable.playCard(card, player);
		
		//oops, need to add the card first. this logic is a little wrong
		
		if (mState.getCurrentRound().totalPlays > 0 && 
				mState.getCurrentRound().isCurrentTrickComplete()) {
			Play winningPlay = scoreTrick(currRound.getLastCompletedTrick(), currRound.trump);
			Log.d("JMATT", "And the winner is: " + winningPlay.card.toString());

			if (currRound.totalPlays == currRound.getActivePlayers() * NUMBER_OF_TRICKS) {
				// time for a new round
				newRound();
			} else {
				Log.d("JMATT", "Current trick is complete, starting new one.");
				// TODO we should actually only do this after trump is set and the
				// maker decides to go alone or not
				currRound.tricks.add(new Play[currRound.getActivePlayers()]);
			}
		}
		
		for (PlayerDisplay display : mPlayerDisplays.values()) {
			display.setActive(false);
		}
		
		setPlayerDisplayEnabled(getNextPlayer(), true);
	}
	
	private void setPlayerDisplayEnabled(Player player, boolean enabled) {
		Player nextUp = getNextPlayer();
		for (PlayerDisplay playerDisplay : mPlayerDisplays.values()) {
			if (playerDisplay.getPlayer() == nextUp) {
				playerDisplay.setActive(enabled);
				break;
			}
		}
	}
	
	private Player getNextPlayer() {
		Round currentRound = mState.getCurrentRound();
		if (currentRound.totalPlays == 0) {
			return getNthPlayerInTrick(currentRound.dealer, 1, currentRound);
		}
		
		if (currentRound.isCurrentTrickComplete()) {
			// the next player is the winner of the last trick
			return scoreTrick(currentRound.getLastCompletedTrick(),
					mState.getCurrentRound().trump).player;
		} else {
			// next player is the winner of the previous trick, plus number of
			// plays in this one
			if (currentRound.totalPlays < currentRound.getActivePlayers()) {
				// we're still in the first trick, offset is from dealer's left
				return getNthPlayerInTrick(currentRound.dealer, currentRound.totalPlays + 1,
						currentRound);
			} else {
				Play lastTrickWinner = scoreTrick(currentRound.getLastCompletedTrick(),
						currentRound.trump);
				return getNthPlayerInTrick(lastTrickWinner.player, 
						currentRound.totalPlays % currentRound.getActivePlayers(), currentRound);
			}
		}
	}
	
	/**
	 * Returns the nth player from the start Player in a round. In doing this
	 * we consider if any players are going alone.
	 * @param start The player started the Trick
	 * @param seatsLeft How many seats to the left the desired player sits
	 * @param round The active round.
	 * @return
	 */
	private Player getNthPlayerInTrick(Player start, int seatsLeft, Round round) {
		ArrayList<Player> activePlayers = new ArrayList<Player>();
		Player[] players = mState.getPlayers();
		for (int ptr = 0; ptr < players.length; ptr++) {
			activePlayers.add(players[ptr]);
		}
		
		if (round.alone) {
			for (int loneWolf = 0; loneWolf < activePlayers.size(); loneWolf++) {
				if (activePlayers.get(loneWolf) == round.maker) {
					activePlayers.remove((loneWolf + 2) % activePlayers.size());
					break;
				}
			}
		}
		
		// find offset of the starter
		for (int ptr = 0; ptr < activePlayers.size(); ptr++) {
			if (activePlayers.get(ptr) == start) {
				return activePlayers.get((ptr + seatsLeft) % activePlayers.size());
			}
		}
		
		throw new RuntimeException("We should never have gotten here.");
	}
	
	private Play scoreTrick(Play[] trick, int trump) {
		Play winningPlay = trick[0];
		for (int ptr = 1; ptr < trick.length; ptr++) {
			if (GamePlayUtils.isGreater(trick[ptr].card, winningPlay.card,
					trick[0].card.getSuit(), trump)) {
				winningPlay = trick[ptr];
			}
		}
		return winningPlay;
	}
	
	private void redrawAllPlayers() {
		for (PlayerDisplay display : mPlayerDisplays.values()) {
			// this is wasteful, would be better to just redraw one player
			display.redraw();
		}
	}
}
