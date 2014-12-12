package pavlik.john.dungeoncrawl.persistence;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.ResourceBundle;

import pavlik.john.dungeoncrawl.controller.Controller;

/**
 * A Multiplayer Server Thread. Takes a reference to the Controller for whatever world is currently
 * running. After thread.start() is executed this thread will continually listen for new clients
 * attempting to connect to the world, and then spin up ClientHandlerThreads for each new client who
 * connects.
 *
 * @author John
 *
 */
public class MultiplayerServerThread extends Thread {

  /**
   * Get the default port for a Multiplayer Server
   *
   * @return an int of the default port
   */
  public static int getDefaultPort() {
    return f_defaultPort;
  }

  private final static int                      f_defaultPort     = 5867;
  /**
   * Connection string to send to this server thread in order to establish a connection successfully
   */
  public static final String                    CONNECTION_STRING = "DUNGEON_CRAWL_1.0";
  private final int                             f_portNumber;
  private final Controller                      f_worldController;
  private final ResourceBundle                  f_messages;
  private boolean                               f_listening       = true;

  private final Collection<ClientHandlerThread> childThreads      = new HashSet<>();

  /**
   * Public constructor
   *
   * @param port
   *          The port to listen on
   * @param worldController
   *          The Controller of the current world.
   * @param messages
   *          The ResourceBundle of messages created by the host.
   */
  public MultiplayerServerThread(int port, Controller worldController, ResourceBundle messages) {
    super("Multiplayer Server Thread");
    f_portNumber = port;
    f_worldController = Objects.requireNonNull(worldController, "worldController cannot be null");
    f_messages = Objects.requireNonNull(messages, "messages cannot be null");
  }

  /**
   * Constructor that uses the default port [5867] to begin listening.
   *
   * @param worldController
   *          The Controller of the current world.
   * @param messages
   *          The ResourceBundle of messages created by the host.
   */
  public MultiplayerServerThread(Controller worldController, ResourceBundle messages) {
    this(f_defaultPort, worldController, messages);
  }

  private void enableConnection() {
    try (ServerSocket serverSocket = new ServerSocket(f_portNumber)) {
      // Listen for 500ms at a time before stopping to check if we should still be listening
      serverSocket.setSoTimeout(500);
      while (f_listening) {
        try {
          final ClientHandlerThread thread = new ClientHandlerThread(f_worldController, f_messages, serverSocket
              .accept());
          childThreads.add(thread);
          thread.start();
        } catch (final SocketTimeoutException timeoutException) {
          // Do nothing, just let the while loop continue
        }
      }
    } catch (final IOException e) {
      System.err.println("Could not listen on port " + f_portNumber);
      System.exit(-1);
    }
  }

  /**
   * Stop listening, and notify all client threads to shutdown. MultiplayerServerThread will
   * terminate in 500ms or less.
   */
  public void finish() {
    f_listening = false;
    for (final ClientHandlerThread child : childThreads) {
      child.finish();
    }
    for (final ClientHandlerThread child : childThreads) {
      try {
        child.join(1000);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Check the current port
   *
   * @return the current port the server is listening on
   */
  public int getPort() {
    return f_portNumber;
  }

  @Override
  public void run() {
    enableConnection();
  }
}
