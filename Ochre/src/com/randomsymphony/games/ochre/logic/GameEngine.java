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
		mState.getDeck().shuffle();
    	Player[] players = mState.getPlayers();
    	for (int ptr = 0; ptr < players.length; ptr++) {
    		players[ptr].discardHand();
    	}
    	
        GamePlayUtils.dealHand(mState.getDeck(), mState.getPlayers());
        
        redrawAllPlayers();
		
		Round newRound = mState.createNewRound(mState.getPlayers()[0]);
		// TODO this should really only be set after we know if a player
		// is going alone or not.
		newRound.tricks.add(new Play[4]);
	}
	
	public void playCard(Player player, Card card) {
		mState.getCurrentRound().addPlay(new Play(player, card));
		Log.d("JMATT", player.getName() + " played " + card.toString());
		if (mState.getCurrentRound().totalPlays % 4 == 0) {
			Log.d("JMATT", "Need to score round.");
		}
		
		player.discardCard(card);
		// this is wasteful, would be better to just redraw one player
		redrawAllPlayers();
		mCardTable.playCard(card, player);
	}
	
	private void redrawAllPlayers() {
		for (PlayerDisplay display : mPlayerDisplays.values()) {
			// this is wasteful, would be better to just redraw one player
			display.redraw();
		}
	}
}
