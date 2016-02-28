package com.randomsymphony.games.ochre;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.urlshortener.Urlshortener;
import com.google.api.services.urlshortener.model.Url;
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
import com.randomsymphony.games.ochre.ui.JoinDialog;
import com.randomsymphony.games.ochre.ui.PlayerDisplay;
import com.randomsymphony.games.ochre.ui.PlayerDisplaysPresenter;
import com.randomsymphony.games.ochre.ui.ScoreBoard;
import com.randomsymphony.games.ochre.ui.TableDisplay;
import com.randomsymphony.games.ochre.ui.TrumpDisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class CardTableActivity extends FragmentActivity {
	
    private static final String TAG_GAME_ENGINE = "com.randomsymphony.games.ochre.GAME_ENGINE";
    private static final String TAG_GAME_STATE = "com.randomsymphony.games.ochre.GAME_STATE";
    private static final String TAG_TRUMP_DISPLAY = "com.randomsymphony.games.ochre.TRUMP_DISPLAY";
    private static final String TAG_SCORE_BOARD = "com.randomsymphony.games.ochre.SCORE_DISPLAY";
    private static final String TAG_TABLE_DISPLAY = "com.randomsymphony.games.ochre.TABLE_DISPLAY";
	private static final File FILE_STATE_SOURCE = new File("/sdcard/ochre/state.txt");
	private static final File FILE_OUTPUT = new File("/sdcard/ochre/state-new.txt");
    private static final String URL_BASE = null;
    private static final String PARAM_API_KEY = "key";
    private static final String PREFS_NAME = "ochre_prefs";
    private static final String KEY_GAME_ID = "game_id";

    public static GameState fromReader(Reader reader) {
        JsonReader jsonReader = new JsonReader(reader);
        GameStateConverter converter =
                (GameStateConverter) new JsonConverterFactory().getConverter(
                        JsonConverterFactory.TYPE_GAME_STATE);
        return converter.readGameState(jsonReader);
    }

    private class LoadAndUpdateState extends AsyncTask<String, Void, GameState> {
        @Override
        protected GameState doInBackground(String... params) {
            return readStateFromUrl(Uri.parse(params[0]));
        }

        @Override
        protected void onPostExecute(GameState gameState) {
            mEngine.setGameState(gameState);
            mGameState = gameState;
            saveCurrentGameId();
        }
    }
    
    private class ShortUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return createShortUrl(params[0]);
        }

        @Override
        protected void onPostExecute(String shortUrl) {
            if (!TextUtils.isEmpty(shortUrl)) {
                updateShortUrl(shortUrl);
            } else {
                updateShortUrl(getResources().getString(R.string.no_api_key));
            }
        }
    }

    private PlayerDisplay[] mPlayerWidgets = new PlayerDisplay[4];
	private GameState mGameState;
	private GameEngine mEngine;
	private TableDisplay mTableDisplay;
	private TrumpDisplay mTrumpWidget;
	private ScoreBoard mScoreBoard;
	private Button mStart;
    private TextView mShortUrl;
    private Button mJoin;
    private PlayerDisplaysPresenter mDisplaysPresenter;
    private Button mResumeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean landscape = getResources().getBoolean(R.bool.landscape);
        setRequestedOrientation(landscape ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_table);
        initGameState();
        initScoreBoard();
        initTableDisplay();
        initTrump();
        initPlayers();
        initGameEngine();
        initPlayerDisplaysPresenter();
        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEngine.startGame();
                testEncoder();
                new ShortUrlTask().execute(URL_BASE + mGameState.getGameId().toString());
                saveCurrentGameId();
            }
        });
        Button testButton = (Button) findViewById(R.id.save);
        testButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// encode, decode, and encode again, if they look the same, we win!
				// String stateEncoded = testRoundEncoder(mGameState.getCurrentRound());
				// Round decoded = testRoundDecoder(stateEncoded, mGameState.getPlayers());
				// testRoundEncoder(decoded);

				final String state = testGameStateEncoder(mGameState);
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        writeStateToUrl(mGameState.getGameId().toString(), state);
                        Log.d("JMATT", "State write should be complete.");
                        return null;
                    }
                }.execute();
			}
		});

        Button loadButton = (Button) findViewById(R.id.load);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameStateFromServer(mGameState.getGameId().toString());
            }
        });
        mShortUrl = (TextView) findViewById(R.id.short_url);
        mJoin = (Button) findViewById(R.id.join);
        mJoin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinDialog();
            }
        });

        // set up the resume button to join the last game we were playing
        mResumeButton = (Button) findViewById(R.id.resume);
        mResumeButton.setEnabled(!TextUtils.isEmpty(URL_BASE));
        mResumeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String gameId = prefs.getString(KEY_GAME_ID, null);
                allowNewGame(false);
                if (!TextUtils.isEmpty(gameId)) {
                    new ShortUrlTask() {
                        @Override
                        protected void onPostExecute(String shortUrl) {
                            super.onPostExecute(shortUrl);
                            joinGame(shortUrl);
                        }
                    }.execute(URL_BASE + gameId);
                }
            }
        });

        testConverter();
    }

    public void joinGame(String gameUrl) {
        mShortUrl.setText(gameUrl);
        new LoadAndUpdateState().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gameUrl);
    }

    /**
     * Create a short URL for the provided one
     * @param myurl the URL to shorten
     * @return The shortened URL or null if it could not be shortened either
     * due to request error or lack of shortener API key
     */
    private String createShortUrl(String myurl) {
        final String apikey = getResources().getString(R.string.shortener_api_key);

        if (TextUtils.isEmpty(apikey)) {
            return null;
        }

        Urlshortener.Builder builder = new Urlshortener.Builder(new NetHttpTransport(),
                AndroidJsonFactory.getDefaultInstance(), null);
        builder.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
            @Override
            public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                request.put(PARAM_API_KEY, apikey);
            }
        });
        Urlshortener urlshortener = builder.build();

        com.google.api.services.urlshortener.model.Url url = new Url();
        url.setLongUrl(myurl);
        try {
            url = urlshortener.url().insert(url).execute();
            return url.getId();
        } catch (IOException e) {
            Log.d("JMATT", "Exception getting short URL", e);
        }
        return null;
    }

    private void updateShortUrl(String shortUrl) {
        mShortUrl.setText(shortUrl);
    }

    private void writeStateToFile(String state) {
        if (!FILE_OUTPUT.exists()) {
            try {
                FILE_OUTPUT.getParentFile().mkdirs();
                FILE_OUTPUT.createNewFile();
            } catch (IOException e) {
                Log.e("JMATT", "Couldn't create output file.");
                return;
            }
        }

        FileOutputStream writeStream = null;
        try {
            writeStream = new FileOutputStream(FILE_OUTPUT);
            byte[] data = state.getBytes();
            writeStream.write(data);
        } catch (FileNotFoundException e) {
            Log.e("JMATT", "Error opening file for output.");
        } catch (IOException e) {
            Log.e("JMATT", "Error writing contents to file.");
        }

        if (writeStream != null) {
            try {
                writeStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void writeStateToUrl(String gameId, final String state) {
        Log.d("JMATT", "Uploading data for game id: " + gameId);

        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(URL_BASE + gameId)
                .method("POST", new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/x-www-form-urlencoded");
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        String formData = "data=" + state;
                        sink.writeString(formData, Charset.forName("UTF-8"));
                    }
                }).build();
        try {
            Response resp = client.newCall(req).execute();
            int code = resp.code();
            String body = resp.body().toString();

            Log.d("JMATT", "State write got response code: " + code);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setGameStateFromServer(String gameId) {
        joinGame(URL_BASE + gameId);
    }

    private void showJoinDialog() {
        DialogFragment fragment = new JoinDialog();
        fragment.show(getSupportFragmentManager(), "join");
    }

    private GameState readStateFromUrl(Uri source) {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(source.toString()).method("GET", null).build();
        try {
            Response resp = client.newCall(req).execute();
            String body = resp.body().string();
            Log.d("JMATT", "State read got response: " + body);
            GameState state = fromReader(new StringReader(body));
            return state;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
    			TAG_TABLE_DISPLAY, URL_BASE == null ? null : Uri.parse(URL_BASE));
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
        boolean useWide = getResources().getBoolean(R.bool.use_wide);

    	mPlayerWidgets[0] = PlayerDisplay.getInstance(false);
    	mPlayerWidgets[1] = PlayerDisplay.getInstance(useWide);
    	mPlayerWidgets[2] = PlayerDisplay.getInstance(false);
    	mPlayerWidgets[3] = PlayerDisplay.getInstance(useWide);
    	
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

    private void initPlayerDisplaysPresenter() {
        mDisplaysPresenter =
                new PlayerDisplaysPresenter(Arrays.asList(mPlayerWidgets), mTrumpWidget);
        for (int ptr = 0, limit = mPlayerWidgets.length; ptr < limit; ptr++) {
            mPlayerWidgets[ptr].setSeatChangeListener(mDisplaysPresenter);
        }
        mEngine.setPlayerDisplaysPresenter(mDisplaysPresenter);
    }

    private void saveCurrentGameId() {
        if (mGameState == null) {
            Log.w("JMATT", "Game ID save requested, but no valid game state.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_GAME_ID, mGameState.getGameId().toString()).apply();
    }
}
