package com.randomsymphony.games.ochre.transport;

import com.randomsymphony.games.ochre.logic.GameState;

public interface GameDeflator<T> {
	public T deflateState(GameState state);
}
