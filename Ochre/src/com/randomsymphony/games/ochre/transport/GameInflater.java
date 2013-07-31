package com.randomsymphony.games.ochre.transport;

import com.randomsymphony.games.ochre.logic.GameState;

/**
 * Give a source, create a {@link GameState} object
 * @author cyngus
 */
public interface GameInflater<T> {
	public GameState inflate(T source);
}
