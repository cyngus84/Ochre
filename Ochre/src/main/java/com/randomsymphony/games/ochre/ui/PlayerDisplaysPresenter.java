package com.randomsymphony.games.ochre.ui;

import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.logic.StateListener;
import com.randomsymphony.games.ochre.model.Player;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by justinm on 2/12/16.
 */
public class PlayerDisplaysPresenter implements PlayerDisplay.SeatedChangeListener, StateListener {

    private LinkedHashMap<Player, PlayerDisplay> mPlayerDisplayMap =
            new LinkedHashMap<Player, PlayerDisplay>();
    private LinkedHashSet<Player> mSeatedPlayers = new LinkedHashSet<Player>();
    private Player mCurrentPlayer = null;
    private TrumpDisplay mTrumpDisplay = null;

    public PlayerDisplaysPresenter(List<PlayerDisplay> displays, TrumpDisplay trumpDisplay) {
        for (PlayerDisplay display : displays) {
            mPlayerDisplayMap.put(display.getPlayer(), display);
        }
        mTrumpDisplay = trumpDisplay;
    }

    public void setCurrentPlayer(Player player) {
        mCurrentPlayer = player;
        boolean isHotSeatMode = mSeatedPlayers.size() > 1;

        // it is important to never automatically change whether cards are
        // revealed since the act of a player sitting or leaving shouldn't
        // cause cards to be show automatically
        for (PlayerDisplay display : mPlayerDisplayMap.values()) {
            if (player.getId().equals(display.getPlayer().getId())) {
                // the current player's turn is set to true
                display.setIsPlayersTurn(true);
                display.setActive(true);
            } else {
                display.setIsPlayersTurn(false);
                // in hotseat mode, hide cards of any player whose turn it isn't
                if (isHotSeatMode) {
                    display.setRevealCards(false);
                }

                // if in hotseat, set players whose turn it is not to inactive
                // if not in hotseat mode, set the non-current player to active
                display.setActive(!isHotSeatMode);
            }
        }
        onStateChange(mPhase);
    }

    public void setPlayerSeated(Player player, boolean seated) {
        if (seated) {
            mSeatedPlayers.add(player);
        } else {
            mSeatedPlayers.remove(player);
        }

        // reassert the active player since the change in player count may
        // alter the display configuration we present
        setCurrentPlayer(mCurrentPlayer);
    }

    @Override
    public void onSeatChange(PlayerDisplay display, boolean seated) {
        setPlayerSeated(display.getPlayer(), seated);
    }

    public boolean isCurrentPlayerSeated() {
        return mSeatedPlayers.contains(mCurrentPlayer);
    }

    private GameState.Phase mPhase = GameState.Phase.NONE;

    @Override
    public void onStateChange(GameState.Phase newPhase) {
        if (isCurrentPlayerSeated()) {
            // configure trump display
            switch (newPhase) {
                case PICK_TRUMP:
                    mTrumpDisplay.setToPickMode();
                    break;
                case ORDER_UP:
                    mTrumpDisplay.setToOrderUpMode();
                    break;
                case DEALER_DISCARD:
                case PLAY:
                    mTrumpDisplay.setToPlayMode();
                    break;
            }
        } else {
            mTrumpDisplay.setEnabled(false);
        }

        for (Map.Entry<Player, PlayerDisplay> entry : mPlayerDisplayMap.entrySet()) {
            entry.getValue().setCheckboxEnabled(newPhase != GameState.Phase.NONE);
        }

        mPhase = newPhase;
    }
}
