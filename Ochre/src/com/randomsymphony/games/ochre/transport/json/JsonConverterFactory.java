package com.randomsymphony.games.ochre.transport.json;

public class JsonConverterFactory implements ConverterFactory {

	public static final String TYPE_CARD = "card";
	public static final String TYPE_PLAYER = "player";
	public static final String TYPE_PLAY = "play";
	public static final String TYPE_ROUND = "round";
	
	@Override
	public Object getConverter(String forType) {
		if (TYPE_PLAYER.equals(forType)) {
			return new PlayerConverter(this);
		} else if (TYPE_CARD.equals(forType)) {
			return new CardConverter(this);
		} else if (TYPE_PLAY.equals(forType)) {
			return new PlayConverter(this);
		} else if (TYPE_ROUND.equals(forType)) {
			return new RoundConverter(this);
		} else {
			throw new RuntimeException("Type unknown");
		}
	}
}
