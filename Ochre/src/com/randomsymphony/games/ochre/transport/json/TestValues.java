package com.randomsymphony.games.ochre.transport.json;

public class TestValues {
	public static final String PLAYER_NO_CARDS = "{\n " +
			"\"player\": {\n" +
			"\"version\": 1,\n" +
			"\"name\": \"John\",\n" +
			"\"id\": \"8e2a08c4-02c0-11e3-a03f-f23c91aec05e\"\n" +
			"}";
	public static final String PLAYER_WITH_CARDS = "{\n " +
			"\"player\": {\n" +
			"\"version\": 1,\n" +
			"\"name\": \"John\",\n" +
			"\"cards\" : [\n" +
			"{\n" +
			"\"version\": 1,\n" +
			"\"suit\" : 2,\n" +
			"\"value\" : 3\n" +
			"},\n" +
			"{\n" + 
			"\"version\": 1,\n" +
			"\"suit\" : 2,\n" + 
			"\"value\" : 4\n" +
			"},\n" +
			"{\n" +
			"\"version\": 1,\n" +
			"\"suit\" : 0,\n" +
			"\"value\" : 5\n" +
			"}\n" +
			"],\n" +
			"\"id\": \"8e2a08c4-02c0-11e3-a03f-f23c91aec05e\"\n" +
			"}";
}
