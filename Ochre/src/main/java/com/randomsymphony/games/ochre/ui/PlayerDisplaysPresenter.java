package com.randomsymphony.games.ochre.ui;

import com.randomsymphony.games.ochre.model.Player;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by justinm on 2/12/16.
 */
public class PlayerDisplaysPresenter implements PlayerDisplay.SeatedChangeListener {

    private LinkedHashMap<Player, PlayerDisplay> mPlayerDisplayMap =
            new LinkedHashMap<Player, PlayerDisplay>();
    private LinkedHashSet<Player> mSeatedPlayers = new LinkedHashSet<Player>();


    public PlayerDisplaysPresenter(List<PlayerDisplay> displays) {
        for (PlayerDisplay display : displays) {
            mPlayerDisplayMap.put(display.getPlayer(), display);
        }
    }

    private Player mCurrentPlayer = null;

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
}
