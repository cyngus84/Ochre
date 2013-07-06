package com.randomsymphony.games.ochre.logic;

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
	private List<Player> mPlayers;
	
	public void setPlayerDisplay(int player, PlayerDisplay display) {
		mPlayerDisplays.put(player, display);
	}
	
	public void setTableDisplay(CardTableActivity activity) {
		mCardTable = activity;
	}
	
	public void setGameState(GameState state) {
		mState = state;
	}
	
	public void setPlayers(List<Player> players) {
		mPlayers = players;
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
        
        redrawAllPlayers();
		
		Round newRound = mState.createNewRound();
		// TODO this should really only be set after we know if a player
		// is going alone or not.
		newRound.tricks.add(new Play[4]);
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
		
		if (currRound.totalPlays % currRound.getActivePlayers() == 0) {
			Play[] trick = currRound.tricks.get(currRound.tricks.size() - 1);
			Play winningPlay = trick[0];

			for (int ptr = 1; ptr < trick.length; ptr++) {
				if (GamePlayUtils.isGreater(trick[ptr].card, winningPlay.card,
						trick[0].card.getSuit(), currRound.trump)) {
					winningPlay = trick[ptr];
				}
			}
			Log.d("JMATT", "And the winner is: " + winningPlay.card.toString());

			if (currRound.totalPlays == currRound.getActivePlayers() * NUMBER_OF_TRICKS) {
				// time for a new round
				newRound();
			} else {
				// TODO we should actually only do this after trump is set and the
				// maker decides to go alone or not
				currRound.tricks.add(new Play[currRound.getActivePlayers()]);
			}
		}
	}
	
	private void redrawAllPlayers() {
		for (PlayerDisplay display : mPlayerDisplays.values()) {
			// this is wasteful, would be better to just redraw one player
			display.redraw();
		}
	}
}
