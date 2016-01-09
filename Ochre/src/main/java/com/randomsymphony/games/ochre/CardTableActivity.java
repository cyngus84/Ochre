package com.randomsymphony.games.ochre;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.UUID;

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
import com.randomsymphony.games.ochre.ui.PlayerDisplay;
import com.randomsymphony.games.ochre.ui.ScoreBoard;
import com.randomsymphony.games.ochre.ui.TableDisplay;
import com.randomsymphony.games.ochre.ui.TrumpDisplay;
import com.randomsymphony.games.ochre.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

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
    private static final String URL_BASE = "http://ochre-bucket-store.appspot.com/game_data/";

    public static GameState fromReader(Reader reader) {
        JsonReader jsonReader = new JsonReader(reader);
        GameStateConverter converter =
                (GameStateConverter) new JsonConverterFactory().getConverter(
                        JsonConverterFactory.TYPE_GAME_STATE);
        return converter.readGameState(jsonReader);
    }

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
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        createShortUrl(URL_BASE + mGameState.getGameId().toString());
                        return null;
                    }
                }.execute();
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
//                GameState newState = readGameStateFromFileTest();

                new AsyncTask<Void, Void, GameState>() {

                    @Override
                    protected GameState doInBackground(Void... params) {
                        return readStateFromUrl(mGameState.getGameId().toString());
                    }

                    @Override
                    protected void onPostExecute(GameState newState) {
                        mEngine.setGameState(newState);
                        mGameState = newState;                    }
                }.execute();

            }
        });

        testConverter();
    }

    private static final String ADDRESS_URL_SHORTENER =
            "https://www.googleapis.com/urlshortener/v1/url";
    private static final String TAG_URL_LONG = "longUrl";
    private static final String TAG_URL_SHORT = "id";

    private void createShortUrl(String myurl) {


        Urlshortener.Builder builder = new Urlshortener.Builder (AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
        Urlshortener urlshortener = builder.build();

        com.google.api.services.urlshortener.model.Url url = new Url();
        url.setLongUrl(myurl);
        try {
            url = urlshortener.url().insert(url).execute();
//            return url.getId();
        } catch (IOException e) {
//            return null;
        }


        Uri shortenerAddress = Uri.parse(ADDRESS_URL_SHORTENER);
        Uri.Builder bob = shortenerAddress.buildUpon()
                .appendQueryParameter("key", getString(R.string.shortener));
        shortenerAddress = bob.build();
        Log.d("JMATT", "API address: " + shortenerAddress.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
        try {
            writer.beginObject();
            writer.name(TAG_URL_LONG).value(url);
            writer.endObject();
            writer.flush();
        } catch (IOException e) {
            Log.d("JMATT", "Error writing object.");
            return;
        }
        final String body = baos.toString();
        Log.d("JMATT", "JSON Body: " + body);

        Request.Builder bobForRequests = new Request.Builder();
        bobForRequests.url(shortenerAddress.toString())
                .method("POST", new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/json");
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        sink.writeString(body, Charset.forName("UTF-8"));
                    }
                });

        OkHttpClient client = new OkHttpClient();

        try {
            Response response = client.newCall(bobForRequests.build()).execute();
            Log.d("JMATT", "Got response: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private GameState readStateFromUrl(String gameId) {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(URL_BASE + gameId).method("GET", null).build();
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
    			TAG_TABLE_DISPLAY, Uri.parse(URL_BASE));
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
