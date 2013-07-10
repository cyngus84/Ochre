package com.randomsymphony.games.ochre;

import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.logic.GamePlayUtils;
import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.logic.PlayerFactory;
import com.randomsymphony.games.ochre.model.Card;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.ui.PlayerDisplay;
import com.randomsymphony.games.ochre.ui.ScoreBoard;
import com.randomsymphony.games.ochre.ui.TableDisplay;
import com.randomsymphony.games.ochre.ui.TrumpDisplay;
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
    private static final String TAG_GAME_STATE = "com.randomsymphony.games.ochre.GAME_STATE";
	private PlayerDisplay[] mPlayerWidgets = new PlayerDisplay[4];
	private GameState mGameState;
	private GameEngine mEngine;
	private TableDisplay mTableDisplay;
	private TrumpDisplay mTrumpWidget;
	private ScoreBoard mScoreBoard;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_table);
        initScoreBoard();
        initGameState();
        initGameEngine();
        initTableDisplay();
        initTrump();
        initPlayers();
    }

    private void initScoreBoard() {
    	mScoreBoard = new ScoreBoard();
    	getSupportFragmentManager().beginTransaction().replace(R.id.score_board, mScoreBoard)
    	        .commit();
    }
    
    private void initGameState() {
        mGameState = new GameState(new PlayerFactory());
        mGameState.setRetainInstance(true);
        getSupportFragmentManager().beginTransaction().add(mGameState, TAG_GAME_STATE).commit();
    }
    
    private void initTableDisplay() {
    	mTableDisplay = TableDisplay.getInstance(4, TAG_GAME_ENGINE, TAG_GAME_STATE);
    	mEngine.setTableDisplay(mTableDisplay);
    	getSupportFragmentManager().beginTransaction().replace(R.id.table_display, mTableDisplay)
    	        .commit();
    }
    
    private void initTrump() {
    	mTrumpWidget = TrumpDisplay.getInstance(TAG_GAME_ENGINE);
    	getSupportFragmentManager().beginTransaction().replace(R.id.trump_controls, mTrumpWidget)
    	        .commit();
    	mEngine.setTrumpDisplay(mTrumpWidget);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card_table, menu);
        return true;
    }
    
    private void initGameEngine() {
    	mEngine = new GameEngine();
    	// TODO switch GameEngine to looking for the state by a tag
    	mEngine.setGameState(mGameState);
    	mEngine.setRetainInstance(true);
    	getSupportFragmentManager().beginTransaction().add(mEngine, TAG_GAME_ENGINE).commit();
    	mEngine.setScoreBoard(mScoreBoard);
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
        	mPlayerWidgets[count].setActive(false);
        	mEngine.setPlayerDisplay(count, mPlayerWidgets[count]);
        }
    }
}
