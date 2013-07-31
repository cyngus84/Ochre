package com.randomsymphony.games.ochre.transport;

/**
 * A source of game state updates
 * @author cyngus
 */
public interface StateSource {
	public interface UpdateListener {
		/**
		 * An update is available, users are expected to call
		 * {@link StateSource#getLatestState()} to get the
		 * new state.
		 */
		public void updateAvailable();
	}
	public void registerUpdateListener(UpdateListener listener);
	public void removeUpdateListener(UpdateListener listener);
	public byte[] getLatestState();
}
