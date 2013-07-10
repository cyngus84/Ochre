package com.randomsymphony.games.ochre.logic;

import java.awt.font.NumericShaper;
import java.util.ArrayList;
import java.util.HashMap;

import com.randomsymphony.games.ochre.logic.GameState.Phase;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Play;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.model.Round;
import com.randomsymphony.games.ochre.ui.PlayerDisplay;
import com.randomsymphony.games.ochre.ui.ScoreBoard;
import com.randomsymphony.games.ochre.ui.TableDisplay;
import com.randomsymphony.games.ochre.ui.TrumpDisplay;

import android.support.v4.app.Fragment;
import android.util.Log;

public class GameEngine extends Fragment implements StateListener {

	public static final int NUMBER_OF_TRICKS = 5;
	private static final int WIN_THRESHOLD = 3;
	// number of points for making the round normally
	private static final int NUM_POINTS_MAKE = 1;
	private static final int NUM_POINTS_ALL_5 = 2;
	private static final int NUM_POINTS_ALL_5_ALONE = 4;
	private static final int NUM_POINTS_SET = 2;
	
	private TableDisplay mCardTable;
	/**
	 * Did I want this to map from view id to playerdisplay?
	 */
	private HashMap<Integer, PlayerDisplay> mPlayerDisplays = new HashMap<Integer, PlayerDisplay>();
	private GameState mState;
	private TrumpDisplay mTrumpDisplay;
	private ScoreBoard mScoreBoard;
	private ArrayList<StateListener> mStateListeners = new ArrayList<StateListener>();

	public void registerStateListener(StateListener listener) {
		mStateListeners.add(listener);
	}
	
	public void unregisterStateListener(StateListener listener) {
		mStateListeners.remove(listener);
	}
	
	public void setPlayerDisplay(int player, PlayerDisplay display) {
		mPlayerDisplays.put(player, display);
	}
	
	public void setTableDisplay(TableDisplay activity) {
		mCardTable = activity;
	}
	
	public void setGameState(GameState state) {
		mState = state;
		mState.setPhaseListener(this);
	}
	
	public void setTrumpDisplay(TrumpDisplay display) {
		mTrumpDisplay = display;
	}
	
	public void startGame() {
    	Player[] players = mState.getPlayers();
    	mScoreBoard.setTeamOneName(players[0].getName() + " & " + players[2].getName());
    	mScoreBoard.setTeamTwoName(players[1].getName() + " & " + players[3].getName());
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
		for (PlayerDisplay display : mPlayerDisplays.values()) {
			display.setDealer(false);
			display.setMaker(false);
		}
		
		getPlayerDisplay(newRound.dealer).setDealer(true);
		// speculatively set the trump
		newRound.trump = possibleTrump;
		mState.setGamePhase(GameState.Phase.ORDER_UP);
		
		setPlayerDisplayEnabled(getNextPlayer(), true);
		
		mTrumpDisplay.setToOrderUpMode();
		mCardTable.clearPlayedCards();
	}
	
	public void playCard(Player player, Card card) {
		Round currRound = mState.getCurrentRound();
		currRound.addPlay(new Play(player, card));
		Log.d("JMATT", player.getName() + " played " + card.toString());

		player.discardCard(card);
		// this is wasteful, would be better to just redraw one player
		redrawAllPlayers();
		
		if (currRound.totalPlays % currRound.getActivePlayers() == 1) {
			mCardTable.clearPlayedCards();
		}
		
		mCardTable.playCard(card, player);
		
		//oops, need to add the card first. this logic is a little wrong
		
		if (mState.getCurrentRound().totalPlays > 0 && 
				mState.getCurrentRound().isCurrentTrickComplete()) {
			Play winningPlay = scoreTrick(currRound.getLastCompletedTrick(),
					currRound.trump.getSuit());
			int totalTricks = currRound.addCapturedTrick(winningPlay.player);
			getPlayerDisplay(winningPlay.player).setTrickCount(totalTricks);
			Log.d("JMATT", "And the winner is: " + winningPlay.card.toString());

			if (currRound.totalPlays == currRound.getActivePlayers() * NUMBER_OF_TRICKS) {
				scoreRound();
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
	
	public void setCurrentCandidateTrump(Card card) {
		// do nothing unless we're in "pick trump" phase
		if (mState.getGamePhase() != GameState.Phase.PICK_TRUMP) {
			return;
		}
		// card is ignored for now
		mTrumpDisplay.enableSetTrump();
	}
	
	/**
	 * A player passed on setting trump.
	 */
	public void pass() {
		if (mState.getGamePhase() != GameState.Phase.ORDER_UP &&
				mState.getGamePhase() != GameState.Phase.PICK_TRUMP) {
			throw new IllegalStateException("State is invalid for this operation.");
		}
		
		Round currentRound = mState.getCurrentRound();
		currentRound.trumpPasses++;
		
		// check if enough people have passed that we changed phases
		if (currentRound.trumpPasses == currentRound.getActivePlayers()) {
			if (mState.getGamePhase() != GameState.Phase.ORDER_UP) {
				throw new IllegalStateException("In the wrong phase to tranisition to PICK_TRUMP");
			}
			
			mState.setGamePhase(GameState.Phase.PICK_TRUMP);
			mTrumpDisplay.setToPickMode();
		} else if (currentRound.trumpPasses == currentRound.getActivePlayers() * 2 - 1) {
			// disable the pass button if the next player is the dealer and
			// this is the 7th pass
			mTrumpDisplay.disablePass();
		}
		
		Player currentPlayer = getNthPlayerInTrick(currentRound.dealer,
				currentRound.trumpPasses, currentRound);
		Log.d("JMATT", "Current player: " + currentPlayer.getName());
		
		// the next player is trumpPasses + 1 positions from the dealer because
		// the first passer is one position left of the dealer
		int positionFromDealer = currentRound.trumpPasses + 1;
		Player nextPlayer = getNthPlayerInTrick(currentRound.dealer, positionFromDealer,
				currentRound);
		Log.d("JMATT", "Next player: " + nextPlayer.getName());

		Log.d("JMATT", "Disabling current player.");
		// disable current player
		setPlayerDisplayEnabled(currentPlayer, false);
		
		Log.d("JMATT", "Enabling next player.");
		// activate the next player
		setPlayerDisplayEnabled(nextPlayer, true);
		redrawAllPlayers();
	}
	
	/**
	 * A player selected to set trump.
	 * @param alone Maker is going alone.
	 */
	public void setTrump(boolean alone) {
		if (mState.getGamePhase() != GameState.Phase.ORDER_UP &&
				mState.getGamePhase() != GameState.Phase.PICK_TRUMP) {
			throw new IllegalStateException("State is invalid for this operation.");
		}

		Round currentRound = mState.getCurrentRound();

		// the trump setters position from the dealer, the first person to have
		// an option to set trump is one position from the dealer
		int positionFromDealer = currentRound.trumpPasses + 1;
		Player maker = getNthPlayerInTrick(currentRound.dealer, positionFromDealer,
				currentRound);
		
		// set alone-ness after we compute which player set trump
		currentRound.alone = alone;
		currentRound.tricks.add(new Play[currentRound.getActivePlayers()]);
		
		// the first player in a round is always the player to the left of the dealer
		Player roundStarter = getNthPlayerInTrick(currentRound.dealer, 1, currentRound);
		
		// set trump
		if (mState.getGamePhase() == GameState.Phase.ORDER_UP) {
			// disable display of order-er
			setPlayerDisplayEnabled(maker, false);
			
			// enable display of dealer to pick a discard card
			currentRound.dealer.addCard(currentRound.trump);
			PlayerDisplay display = getPlayerDisplay(currentRound.dealer);
			display.showDiscardCard();
			setPlayerDisplayEnabled(currentRound.dealer, true);
		} else {
			currentRound.trump = getPlayerDisplay(maker).getSelectedCard();
			Log.d("JMATT", "trump is " + getPlayerDisplay(maker).getSelectedCard().toString());
			mState.setGamePhase(GameState.Phase.PLAY);
			setPlayerDisplayEnabled(maker, false);
			startRound();
		}
		
		// set maker
		currentRound.maker = maker;
		getPlayerDisplay(currentRound.maker).setMaker(true);
		Log.d("JMATT", "Maker is: " + maker.getName());
		
		// disable trump display
		mTrumpDisplay.setToPlayMode();
		
		redrawAllPlayers();
		mCardTable.setTrumpSuit(currentRound.trump.getSuit());
	}
	
	public void discardCard(Card card) {
		Round currentRound = mState.getCurrentRound();
		currentRound.dealer.removeCard(card);
		PlayerDisplay dealer = getPlayerDisplay(currentRound.dealer);
		dealer.hideDiscardCard();
		dealer.redraw();
		
		setPlayerDisplayEnabled(currentRound.dealer, false);
		startRound();
	}
	
	@Override
	public void onStateChange(Phase newPhase) {
		for (int ptr = 0, limit = mStateListeners.size(); ptr < limit; ptr++) {
			mStateListeners.get(ptr).onStateChange(newPhase);
		}
	}
	
	private void startRound() {
		Round currentRound = mState.getCurrentRound();
		Player roundStarter = getNthPlayerInTrick(currentRound.dealer, 1, currentRound);
		setPlayerDisplayEnabled(roundStarter, true);
		mState.setGamePhase(GameState.Phase.PLAY);
	}
	
	private PlayerDisplay getPlayerDisplay(Player forPlayer) {
		for (PlayerDisplay playerDisplay : mPlayerDisplays.values()) {
			if (playerDisplay.getPlayer() == forPlayer) {
				return playerDisplay;
			}
		}
		
		throw new RuntimeException("We shouldn't have gotten here.");
	}
	
	private void setPlayerDisplayEnabled(Player player, boolean enabled) {
		getPlayerDisplay(player).setActive(enabled);
	}
	
	private Player getNextPlayer() {
		Round currentRound = mState.getCurrentRound();
		if (currentRound.totalPlays == 0) {
			return getNthPlayerInTrick(currentRound.dealer, 1, currentRound);
		}
		
		if (currentRound.isCurrentTrickComplete()) {
			// the next player is the winner of the last trick
			return scoreTrick(currentRound.getLastCompletedTrick(),
					mState.getCurrentRound().trump.getSuit()).player;
		} else {
			// next player is the winner of the previous trick, plus number of
			// plays in this one
			if (currentRound.totalPlays < currentRound.getActivePlayers()) {
				// we're still in the first trick, offset is from dealer's left
				return getNthPlayerInTrick(currentRound.dealer, currentRound.totalPlays + 1,
						currentRound);
			} else {
				Play lastTrickWinner = scoreTrick(currentRound.getLastCompletedTrick(),
						currentRound.trump.getSuit());
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
	
	private void scoreRound() {
		// TODO the round is actually tracking the number of tricks captured
		// by each player, look at that which will allow us to do less work here
		Round finishedRound = mState.getCurrentRound();
		
		HashMap<Player, Integer> winCount = new HashMap<Player, Integer>();
		// for all the tricks in the round, determine the winner
		for (int ptr = 0, limit = finishedRound.tricks.size(); ptr < limit; ptr++) {
			Play[] trick = finishedRound.tricks.get(ptr);
			Play winningPlay = scoreTrick(trick, finishedRound.trump.getSuit());
			if (winCount.containsKey(winningPlay.player)) {
				winCount.put(winningPlay.player, winCount.get(winningPlay.player) + 1);
			} else {
				winCount.put(winningPlay.player, 1);
			}
		}

		// we've got the totals, add them up for the partners
		Player[] players = mState.getPlayers();
		int makerPosition = -1;
		int[] scores = new int[players.length / 2];
		for (int ptr = 0; ptr < players.length; ptr++) {
			if (winCount.containsKey(players[ptr])) {
				scores[ptr % 2] += winCount.get(players[ptr]);
			}
			if (players[ptr] == finishedRound.maker) {
				makerPosition = ptr;
			}
		}

		if (makerPosition == -1) {
			throw new RuntimeException("Scoring error, maker not found.");
		}
		
		// determine position of the maker
		boolean wasSet = false;
		if (scores[makerPosition % 2] < WIN_THRESHOLD) {
			wasSet = true;
		}
		
		if (!wasSet) {
			int numberOfPoints;
			// check how many points the winning team got
			if (scores[makerPosition % 2] == NUMBER_OF_TRICKS) {
				if (finishedRound.alone) {
					numberOfPoints = NUM_POINTS_ALL_5_ALONE;
				} else {
					numberOfPoints = NUM_POINTS_ALL_5;
				}
			} else {
				numberOfPoints = NUM_POINTS_MAKE;
			}
			mState.addPoints(players[makerPosition % 2], numberOfPoints);
		} else {
			mState.addPoints(players[(makerPosition + 1) % 2], NUM_POINTS_SET);
		}
		
		mScoreBoard.setTeamOneScore(mState.getPointsForPlayer(players[0]));
		mScoreBoard.setTeamTwoScore(mState.getPointsForPlayer(players[1]));
	}
	
	private void redrawAllPlayers() {
		for (PlayerDisplay display : mPlayerDisplays.values()) {
			// this is wasteful, would be better to just redraw one player
			display.redraw();
		}
	}

	public void setScoreBoard(ScoreBoard scoreBoard) {
		mScoreBoard = scoreBoard;
	}
}
