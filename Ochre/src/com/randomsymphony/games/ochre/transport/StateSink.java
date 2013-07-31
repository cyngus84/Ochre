package com.randomsymphony.games.ochre.transport;

import java.io.InputStream;

/**
 * A destination that new state updates can be pushed to.
 * @author cyngus
 */
public interface StateSink {
	public void pushState(byte[] state);
	public void pushState(InputStream state);
}
