package com.kelsos.mbrc.net;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kelsos.mbrc.BuildConfig;
import com.kelsos.mbrc.constants.EventType;
import com.kelsos.mbrc.enums.SocketAction;
import com.kelsos.mbrc.events.Events;
import com.kelsos.mbrc.events.Message;
import com.kelsos.mbrc.util.DelayTimer;
import com.kelsos.mbrc.util.SettingsManager;
import com.koushikdutta.async.http.AsyncHttpClient;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.util.Ln;
import rx.schedulers.Schedulers;

@Singleton public class SocketService {
  public static final int MAX_RETRIES = 3;
  public static final int BUFFER_SIZE = 4096;
  public static final int DELAY = 3;
  public static final int SUB_START = 26;
  private static int numOfRetries;
  private SettingsManager settingsManager;
  private ObjectMapper mapper;
  private boolean shouldStop;
  private Socket clSocket;
  private PrintWriter output;
  private DelayTimer cTimer;
  private Thread mThread;

  @Inject public SocketService(SettingsManager settingsManager, ObjectMapper mapper) {
    this.settingsManager = settingsManager;
    this.mapper = mapper;

    cTimer = new DelayTimer(DELAY, () -> {

    });
    numOfRetries = 0;
    shouldStop = false;
    socketManager(SocketAction.START);
    subscribeToEvents();
  }

  private void subscribeToEvents() {
    Events.messages.subscribeOn(Schedulers.io())
        .filter(msg -> EventType.START_CONNECTION.equals(msg.getType()))
        .subscribe(event -> socketManager(SocketAction.START));
  }

  public void socketManager(SocketAction action) {
    switch (action) {
      case RESET:
        reset();
        break;
      case START:
        start();
        break;
      case RETRY:
        retry();
        break;
      case STOP:
        shouldStop = true;
        break;
      default:
        break;
    }
  }

  private void stopThread() {
    if (mThread != null && mThread.isAlive()) {
      mThread.interrupt();
      mThread = null;
    }
  }

  private void retry() {
    cleanupSocket();
    stopThread();
    if (shouldStop) {
      shouldStop = false;
      numOfRetries = 0;
      return;
    }
    cTimer.start();
  }

  private void reset() {
    cleanupSocket();
    stopThread();
    shouldStop = false;
    numOfRetries = 0;
    cTimer.start();
  }

  private void start() {
    if (!sIsConnected()) {
      cTimer.start();
    }
    startWebSocket();
  }

  /**
   * Returns true if the socket is not null and it is connected, false in any other case.
   *
   * @return Boolean
   */
  private boolean sIsConnected() {
    return false;
  }

  private void cleanupSocket() {
    if (!sIsConnected()) {
      return;
    }
    try {
      if (output != null) {
        output.flush();
        output.close();
        output = null;
      }
      clSocket.close();
      clSocket = null;
    } catch (IOException e) {
      if (BuildConfig.DEBUG) {
        Ln.e(e, "io exception on socket cleanup");
      }
    }
  }

  public void tryProcessIncoming(final String incoming) {
    try {
      processIncoming(incoming);
    } catch (IOException e) {
      if (BuildConfig.DEBUG) {
        Ln.e(e, "Incoming message pre-processor");
      }
    }
  }

  private void startWebSocket() {
    AsyncHttpClient.getDefaultInstance()
        .websocket("ws://development.lan:3000", "mbrc-pro", (ex, webSocket) -> {
          if (ex != null) {
            Ln.d(ex);
            return;
          }

          webSocket.setStringCallback(this::tryProcessIncoming);
        });
  }

  private void processIncoming(String incoming) throws IOException {
    final String[] replies = incoming.split("\r\n");
    for (String reply : replies) {

      JsonNode node = mapper.readValue(reply, JsonNode.class);
      String context = node.path("message").getTextValue();

      if (context.contains(Notification.CLIENT_NOT_ALLOWED)) {
        return;
      }

      Events.messages.onNext(new Message(context));
    }
  }
}
