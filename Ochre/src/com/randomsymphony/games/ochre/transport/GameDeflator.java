package com.randomsymphony.games.ochre.transport;

import java.io.OutputStream;

import com.randomsymphony.games.ochre.logic.GameState;

/**
 * Given a game state, serialize it into some representation.
 * @author cyngus
 */
public interface GameDeflator<T extends OutputStream> {
	public void deflateState(GameState state, T output);
}
