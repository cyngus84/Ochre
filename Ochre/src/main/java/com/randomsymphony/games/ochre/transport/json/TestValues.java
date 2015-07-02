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
	/*
	{
	    "version": 1,
	    "trump_suit": {
	        "version": 1,
	        "suit": 1,
	        "value": 0,
	        "visible": false
	    },
	    "played_tricks": [
	        [
	            {
	                "version": 1,
	                "play_card": {
	                    "version": 1,
	                    "suit": 1,
	                    "value": 5,
	                    "visible": false
	                },
	                "player_id": "5a826ba4-d657-439b-8247-404ac31c4141"
	            },
	            {
	                "version": 1,
	                "play_card": {
	                    "version": 1,
	                    "suit": 1,
	                    "value": 3,
	                    "visible": false
	                },
	                "player_id": "c00f6a51-ee1f-4978-834b-63aedf99f07a"
	            },
	            {
	                "version": 1,
	                "play_card": {
	                    "version": 1,
	                    "suit": 0,
	                    "value": 0,
	                    "visible": false
	                },
	                "player_id": "40aa1458-16a7-4e3c-8748-3cf1ecc31356"
	            },
	            {
	                "version": 1,
	                "play_card": {
	                    "version": 1,
	                    "suit": 1,
	                    "value": 0,
	                    "visible": false
	                },
	                "player_id": "dd17bc00-7fd2-4d6e-be8a-f21f66669ded"
	            }
	        ],
	        [
	            {
	                "version": 1,
	                "play_card": {
	                    "version": 1,
	                    "suit": 2,
	                    "value": 2,
	                    "visible": false
	                },
	                "player_id": "5a826ba4-d657-439b-8247-404ac31c4141"
	            },
	            {
	                "version": 1,
	                "play_card": {
	                    "version": 1,
	                    "suit": 3,
	                    "value": 0,
	                    "visible": false
	                },
	                "player_id": "c00f6a51-ee1f-4978-834b-63aedf99f07a"
	            }
	        ]
	    ],
	    "going_alone": false,
	    "round_dealer": "dd17bc00-7fd2-4d6e-be8a-f21f66669ded",
	    "round_maker": "5a826ba4-d657-439b-8247-404ac31c4141",
	    "total_plays": 6,
	    "trump_passes": 0,
	    "player_trick_count": [
	        "5a826ba4-d657-439b-8247-404ac31c4141",
	        1
	    ]
	} */
}
