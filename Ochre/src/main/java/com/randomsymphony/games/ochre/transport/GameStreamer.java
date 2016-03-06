package com.randomsymphony.games.ochre.transport;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * This class manages an event loop that does a periodic poll of a server for
 * game state. It also will transport updates to the server, taking care not to
 * pull state updates will a push is in progress.
 *
 * Created by cyngus on 1/5/16.
 */
public class GameStreamer {

    public static final int POLL_INTERVAL_DEFAULT = 1000;
    private static final int MSG_READ = 1;
    private static final int MSG_WRITE = 2;
    private static final Charset CHAR_SET_REQUEST = Charset.forName("UTF-8");
    private static final String FORM_FIELD = "data=";

    public interface GameUpdateListener {
        public void onNewState(String jsonState);
    }

    private final Uri mGameUri;
    private final String mGameUriStr;
    private int mPollInterval = POLL_INTERVAL_DEFAULT;
    private GameUpdateListener mListener = null;
    private HandlerThread mWorker = null;
    private Handler mWorkerHandler = null;
    private ArrayList<String> mStatePushes = new ArrayList<String>();
    private final Object mStateLock = new Object();
    private OkHttpClient mHttpClient = new OkHttpClient();
    private Request.Builder mBuilderBase;
    private Handler mCallbackHandle = null;
    private boolean mIsCanceled = false;
    private final Object mCancelLock = new Object();


    public GameStreamer(Uri urlBase, UUID gameId) {
        mGameUri = urlBase.buildUpon().appendPath(gameId.toString()).build();
        mGameUriStr = mGameUri.toString();
        mBuilderBase = new Request.Builder().url(mGameUriStr);
    }

    public void setPollInterval(int interval) {
        mPollInterval = interval;
    }

    /**
     * Start polling for game state.
     * @param listener
     */
    public void startPolling(GameUpdateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("State listener can not be null!");
        } else {
            mListener = listener;
        }

        startWorker();
    }

    /**
     * Upload a new state. This will be done ASAP, jumping ahead of any pending
     * poll operations.
     * @param jsonState
     */
    public void uploadState(String jsonState) {
        if (mWorker == null) {
            throw new IllegalStateException("Polling must be started first!");
        }

        synchronized (mStateLock) {
            mStatePushes.add(jsonState);
            mWorkerHandler.sendMessageAtFrontOfQueue(mWorkerHandler.obtainMessage(MSG_WRITE));
        }
    }

    /**
     * Shut down the polling thread.
     */
    public void stop() {
        synchronized (mCancelLock) {
            mIsCanceled = true;
        }
        mWorker.quit();
    }

    public void pause() {
        if (mWorkerHandler != null) {
            mWorkerHandler.removeMessages(MSG_READ);
        }
    }

    public void resume() {
        if (mWorkerHandler != null) {
            mWorkerHandler.removeMessages(MSG_READ);
            mWorkerHandler.sendEmptyMessage(MSG_READ);
        }
    }

    private void startWorker() {
        if (mWorker == null) {
            mWorker = new HandlerThread("GameStreamer");
            mWorker.start();
            mWorkerHandler = new Handler(mWorker.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_READ:
                            doRead();
                            mWorkerHandler.removeMessages(MSG_READ);
                            synchronized (mCancelLock) {
                                if (!mIsCanceled) {
                                    mWorkerHandler.sendEmptyMessageDelayed(MSG_READ, mPollInterval);
                                }
                            }
                            break;
                        case MSG_WRITE:
                            doWrite();
                            break;
                    }
                }
            };

            mCallbackHandle = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (mListener != null) {
                        synchronized (mCancelLock) {
                            if (!mIsCanceled) {
                                mListener.onNewState((String) msg.obj);
                            }
                        }
                    }
                }
            };
        }

        mWorkerHandler.sendEmptyMessageDelayed(MSG_READ, mPollInterval);
    }

    private void doRead() {
        Request request = mBuilderBase.method("GET", null).header("User-Agent", "gzip").build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            String state = null;
            if (response.code() == 200) {
                state = response.body().string();
            }

            response.body().close();

            if (state != null) {
                mCallbackHandle.obtainMessage(0, state).sendToTarget();
            }
        } catch (IOException e) {
            Log.e("JMATT", "Problem reading state.");
        }
    }

    private void doWrite() {
        ArrayList<String> newStates = null;
        synchronized (mStateLock) {
            mWorkerHandler.removeMessages(MSG_WRITE);
            newStates = new ArrayList<String>(mStatePushes.size());
            newStates.addAll(mStatePushes);
            mStatePushes.clear();
        }

        for (int ptr = 0, limit = newStates.size(); ptr < limit; ptr++) {
            String target = newStates.get(ptr);
            while (!sendStateUpdate(target)) {
                try {
                    Thread.sleep(mPollInterval);
                } catch (InterruptedException e) {
                }
                Log.d("JMATT", "Trying failed send again.");
            }
        }
    }

    private boolean sendStateUpdate(final String state) {
        Request request = mBuilderBase.method("POST", new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/x-www-form-urlencoded");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeString(FORM_FIELD, CHAR_SET_REQUEST);
                sink.writeString(state, CHAR_SET_REQUEST);
            }
        }).build();

        try {
            Response response = mHttpClient.newCall(request).execute();
            int code = response.code();
            response.body().close();
            if (code != 200) {
                Log.e("JMATT", "Non-200 response: " + response.code() + " message: " +
                        response.body().toString());
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            Log.e("JMATT", "Unexpected I/O exception uploading state.");
            return false;
        }
    }
}
