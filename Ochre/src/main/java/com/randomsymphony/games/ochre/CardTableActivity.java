package com.randomsymphony.games.ochre;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.UUID;

import com.randomsymphony.games.ochre.logic.GameEngine;
import com.randomsymphony.games.ochre.logic.GameState;
import com.randomsymphony.games.ochre.logic.PlayerFactory;
import com.randomsymphony.games.ochre.model.Player;
import com.randomsymphony.games.ochre.model.Round;
import com.randomsymphony.games.ochre.transport.json.GameStateConverter;
import com.randomsymphony.games.ochre.transport.json.JsonConverterFactory;
import com.randomsymphony.games.ochre.transport.json.PlayerConverter;
import com.randomsymphony.games.ochre.transport.json.RoundConverter;
import com.randomsymphony.games.ochre.transport.json.TestValues;
import com.randomsymphony.games.ochre.ui.PlayerDisplay;
import com.randomsymphony.games.ochre.ui.ScoreBoard;
import com.randomsymphony.games.ochre.ui.TableDisplay;
import com.randomsymphony.games.ochre.ui.TrumpDisplay;
import com.randomsymphony.games.ochre.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class CardTableActivity extends FragmentActivity {
	
    private static final String TAG_GAME_ENGINE = "com.randomsymphony.games.ochre.GAME_ENGINE";
    private static final String TAG_GAME_STATE = "com.randomsymphony.games.ochre.GAME_STATE";
    private static final String TAG_TRUMP_DISPLAY = "com.randomsymphony.games.ochre.TRUMP_DISPLAY";
    private static final String TAG_SCORE_BOARD = "com.randomsymphony.games.ochre.SCORE_DISPLAY";
    private static final String TAG_TABLE_DISPLAY = "com.randomsymphony.games.ochre.TABLE_DISPLAY";
	private static final File FILE_STATE_SOURCE = new File("/sdcard/ochre/state.txt");

    private PlayerDisplay[] mPlayerWidgets = new PlayerDisplay[4];
	private GameState mGameState;
	private GameEngine mEngine;
	private TableDisplay mTableDisplay;
	private TrumpDisplay mTrumpWidget;
	private ScoreBoard mScoreBoard;
	private Button mStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_table);
        initGameState();
        initScoreBoard();
        initTableDisplay();
        initTrump();
        initPlayers();
        initGameEngine();
        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEngine.startGame();
				testEncoder();
			}
		});
        Button testButton = (Button) findViewById(R.id.test);
        testButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// encode, decode, and encode again, if they look the same, we win!
//				String stateEncoded = testRoundEncoder(mGameState.getCurrentRound());
//				Round decoded = testRoundDecoder(stateEncoded, mGameState.getPlayers());
//				testRoundEncoder(decoded);


//				testGameStateEncoder(mGameState);

                GameState newState = readGameStateFromFileTest();
                mEngine.setGameState(newState);

			}
		});
        testConverter();
    }

    private void gameStateTest() {
        String gameState = testGameStateEncoder(mGameState);
        GameState decoded = testGameStateDecoder(gameState);
        testGameStateEncoder(decoded);
    }

    private GameState readGameStateFromFileTest() {
        try {
            FileInputStream fileInput = new FileInputStream(FILE_STATE_SOURCE);
            GameState decoded = fromReader(new InputStreamReader(fileInput));
            fileInput.close();
            return decoded;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GameState fromReader(Reader reader) {
        JsonReader jsonReader = new JsonReader(reader);
        GameStateConverter converter =
                (GameStateConverter) new JsonConverterFactory().getConverter(
                        JsonConverterFactory.TYPE_GAME_STATE);
        return converter.readGameState(jsonReader);
    }

    
    private String testGameStateEncoder(GameState state) {
    	GameStateConverter converter = 
    			(GameStateConverter) new JsonConverterFactory().getConverter(
    					JsonConverterFactory.TYPE_GAME_STATE);
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
		
		try {
			converter.writeGameState(writer, state);
			writer.flush();
			Log.d("JMATT", "Wrote current round, bytes written: " + baos.size());
			String encoded = baos.toString("UTF-8");
			Log.d("JMATT", encoded);
			return encoded;
		} catch (IOException e) {
			return null;
		}
    }
    
    private GameState testGameStateDecoder(String state) {
    	GameStateConverter converter = 
    			(GameStateConverter) new JsonConverterFactory().getConverter(
    					JsonConverterFactory.TYPE_GAME_STATE);
    	GameState decoded = converter.readGameState(new JsonReader(new StringReader(state)));
    	return decoded;
    }
    
    private Round testRoundDecoder(String state, Player[] players) {
		RoundConverter converter = 
				(RoundConverter) new JsonConverterFactory().getConverter(
						JsonConverterFactory.TYPE_ROUND);
		Round decoded = converter.readRound(new JsonReader(new StringReader(state)), players);
		return decoded;
    }
    
    private String testRoundEncoder(Round round) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
		RoundConverter converter = 
				(RoundConverter) new JsonConverterFactory().getConverter(
						JsonConverterFactory.TYPE_ROUND);
		try { 
			converter.writeRound(writer, round);
			writer.flush();
			Log.d("JMATT", "Wrote current round, bytes written: " + baos.size());
			String encoded = baos.toString("UTF-8");
			Log.d("JMATT", encoded);
			return encoded;
		} catch (IOException e) {
			
		}
		return null;
    }

    private void testEncoder() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
		PlayerConverter converter = (PlayerConverter)
				new JsonConverterFactory().getConverter(
						JsonConverterFactory.TYPE_PLAYER);
		try {
			converter.writePlayer(writer, mGameState.getPlayers()[0]);
			writer.flush();
			Log.d("JMATT", "Player is: " + mGameState.getPlayers()[0].getName() + 
					" bytes written: " + baos.size());
			baos.close();
			Log.d("JMATT", baos.toString("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void testConverter() {
    	ByteArrayInputStream input = new ByteArrayInputStream(
    			TestValues.PLAYER_WITH_CARDS.getBytes());
    	JsonReader reader = new JsonReader(new InputStreamReader(input));
    	try {
    		Log.d("JMATT", "First token type: " + reader.peek());
    		// start the outer open object
    		reader.beginObject();
    		String propName = reader.nextName();
    		Log.d("JMATT", "Property name is: " + propName);
    		PlayerConverter converter = (PlayerConverter)
    				new JsonConverterFactory().getConverter(
    						JsonConverterFactory.TYPE_PLAYER);
    		Player test = converter.readPlayer(reader);
    		Log.d("JMATT", "Test player: " + test.getId() + " -- " + 
    				test.getName());
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card_table, menu);
        return true;
    }

    /**
     * Whether or not it is legal to stat a new game now
     * @param allowed
     */
    public void allowNewGame(boolean allowed) {
    	mStart.setEnabled(allowed);
    }

    private void initScoreBoard() {
    	mScoreBoard = new ScoreBoard();
    	getSupportFragmentManager().beginTransaction().replace(R.id.score_board, mScoreBoard,
    			TAG_SCORE_BOARD).commit();
    }
    
    private void initGameState() {
        mGameState = new GameState();
		mGameState.setPlayerFactory(new PlayerFactory());
        mGameState.setRetainInstance(true);
        mGameState.setGameId(UUID.randomUUID());
        getSupportFragmentManager().beginTransaction().add(mGameState, TAG_GAME_STATE).commit();
    }
    
    private void initTableDisplay() {
    	mTableDisplay = TableDisplay.getInstance(4, TAG_GAME_STATE);
    	getSupportFragmentManager().beginTransaction().replace(R.id.table_display, mTableDisplay,
    			TAG_TABLE_DISPLAY).commit();
    }
    
    private void initTrump() {
    	mTrumpWidget = TrumpDisplay.getInstance();
    	getSupportFragmentManager().beginTransaction().replace(R.id.trump_controls, mTrumpWidget,
    			TAG_TRUMP_DISPLAY).commit();
    }
    
    private void initGameEngine() {
    	mEngine = GameEngine.getInstance(TAG_TRUMP_DISPLAY, TAG_GAME_STATE, TAG_SCORE_BOARD,
    			TAG_TABLE_DISPLAY);
    	mEngine.setRetainInstance(true);
    	getSupportFragmentManager().beginTransaction().add(mEngine, TAG_GAME_ENGINE).commit();

    	// wire the engine to the widgets that want to know about it
    	mTrumpWidget.setGameEngine(mEngine);
    	
    	// add the player displays to the game engine
        for (int count = 0; count < mPlayerWidgets.length; count++) {
        	mEngine.setPlayerDisplay(count, mPlayerWidgets[count]);
        }
    }
    
    private void initPlayers() {
    	mPlayerWidgets[0] = PlayerDisplay.getInstance(false);
    	mPlayerWidgets[1] = PlayerDisplay.getInstance(true);
    	mPlayerWidgets[2] = PlayerDisplay.getInstance(false);
    	mPlayerWidgets[3] = PlayerDisplay.getInstance(true);
    	
    	getSupportFragmentManager().beginTransaction().replace(R.id.player0, mPlayerWidgets[0]).commit();
    	getSupportFragmentManager().beginTransaction().replace(R.id.player1, mPlayerWidgets[1]).commit();
    	getSupportFragmentManager().beginTransaction().replace(R.id.player2, mPlayerWidgets[2]).commit();
    	getSupportFragmentManager().beginTransaction().replace(R.id.player3, mPlayerWidgets[3]).commit();
    	
        Player[] players = mGameState.getPlayers();
        for (int count = 0; count < mPlayerWidgets.length; count++) {
        	mPlayerWidgets[count].setPlayer(players[count]);
        	mPlayerWidgets[count].setActive(false);
        }
    }
}
