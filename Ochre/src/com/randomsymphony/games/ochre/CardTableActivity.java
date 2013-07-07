package com.randomsymphony.games.ochre;

import com.randomsymphony.games.ochre.fragment.GameState;
import com.randomsymphony.games.ochre.fragment.PlayerDisplay;
import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.logic.GamePlayUtils;
import com.randomsymphony.games.ochre.logic.PlayerFactory;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class CardTableActivity extends FragmentActivity {
	
    private static final String TAG_GAME_ENGINE = "com.randomsymphony.games.ochre.GAME_ENGINE";
	private PlayerDisplay[] mPlayerWidgets = new PlayerDisplay[4];
	private GameState mGameState;
	private GameEngine mEngine;
	private Button[] mPlayedCards = new Button[4];
	private Button mTrumpCard;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_table);
        mGameState = new GameState(new PlayerFactory());
        initGameEngine();
        initPlayers();
        initPlayedCards();
        mTrumpCard = (Button) findViewById(R.id.candidate_trump);
        
        ((ImageView) findViewById(R.id.table_felt)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEngine.startGame();
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card_table, menu);
        return true;
    }
    
    public void playCard(Card card, Player player) {
    	Player[] players = mGameState.getPlayers();
    	for (int ptr = 0; ptr < players.length; ptr++) {
    		if (players[ptr] == player) {
    			Button playedCard = mPlayedCards[ptr];
    			Card.formatButtonAsCard(playedCard, card, getResources());
				playedCard.setVisibility(View.VISIBLE);
    			break;
    		}
    	}
    }
    
    public void setTrumpCard(Card card) {
    	Card.formatButtonAsCard(mTrumpCard, card, getResources());
    	mTrumpCard.setVisibility(View.VISIBLE);
    }
    
    /**
     * Hide the card displayed on the card table that is the possible trump suit.
     */
    public void hideTrump() {
    	mTrumpCard.setVisibility(View.INVISIBLE);
    }
    
    private void initPlayedCards() {
    	mPlayedCards[0] = (Button) findViewById(R.id.player0_card);
    	mPlayedCards[1] = (Button) findViewById(R.id.player1_card);
    	mPlayedCards[2] = (Button) findViewById(R.id.player2_card);
    	mPlayedCards[3] = (Button) findViewById(R.id.player3_card);
    }
    
    private void initGameEngine() {
    	mEngine = new GameEngine();
    	mEngine.setGameState(mGameState);
    	mEngine.setTableDisplay(this);
    	mEngine.setRetainInstance(true);
    	getSupportFragmentManager().beginTransaction().add(mEngine, TAG_GAME_ENGINE).commit();
    }
    
    private void initPlayers() {
    	mPlayerWidgets[0] = PlayerDisplay.getInstance(TAG_GAME_ENGINE);
    	mPlayerWidgets[1] = PlayerDisplay.getInstance(TAG_GAME_ENGINE);
    	mPlayerWidgets[2] = PlayerDisplay.getInstance(TAG_GAME_ENGINE);
    	mPlayerWidgets[3] = PlayerDisplay.getInstance(TAG_GAME_ENGINE);
    	
    	getSupportFragmentManager().beginTransaction().replace(R.id.player0, mPlayerWidgets[0]).commit();
    	getSupportFragmentManager().beginTransaction().replace(R.id.player1, mPlayerWidgets[1]).commit();
    	getSupportFragmentManager().beginTransaction().replace(R.id.player2, mPlayerWidgets[2]).commit();
    	getSupportFragmentManager().beginTransaction().replace(R.id.player3, mPlayerWidgets[3]).commit();
    	
        Player[] players = mGameState.getPlayers();
        for (int count = 0; count < mPlayerWidgets.length; count++) {
        	mPlayerWidgets[count].setPlayer(players[count]);
        	mPlayerWidgets[count].setActive(true);
        	mEngine.setPlayerDisplay(count, mPlayerWidgets[count]);
        }
    }
}
