package pavlik.john.dungeoncrawl.view;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pavlik.john.dungeoncrawl.exceptions.NetworkConnectionClosedException;
import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.IModelObserver;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Weapon;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.persistence.MultiplayerServerThread;
import pavlik.john.dungeoncrawl.view.parser.IDisplayNotifier;

/**
 * The main program for MainConsole with a simple graphical user interface. It has a window that lets
 * the user type commands and see game output in a text box. Thus, it is not that much different
 * from a text-only user interface.
 * <p>
 * This application uses the Swing graphical user interface library (part of the Java standard
 * library).
 *
 * @author T.J. Halloran
 * @author Robert Graham
 * @author Brian Woolley
 * @author John Pavlik
 * @version 1.2
 */
public final class NetworkClient extends Thread {

  private class ReadInputThread extends Thread {
    Socket f_socket;

    public ReadInputThread(Socket socket) {
      f_socket = Objects.requireNonNull(socket, "socket cannot be null");
    }

    @Override
    public void run() {
      try (ObjectInputStream networkInput = new ObjectInputStream(f_socket.getInputStream());) {
        while (!finished) {
          final String message = (String) networkInput.readObject();
          if (message != null) {
            switch (message) {
              case "broadcastMessage":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.broadcastMessage((Character) networkInput.readObject(), (String) networkInput
                    .readObject(), (String) networkInput.readObject());
                break;
              case "characterEquippedWeapon":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterEquippedWeapon((pavlik.john.dungeoncrawl.model.Character) networkInput
                    .readObject(), (Weapon) networkInput.readObject());
                break;
              case "characterHeal":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterHeal((Character) networkInput.readObject(), (int) networkInput.readObject());
                break;
              case "characterHitsCharacterFor":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterHitsCharacterFor((Character) networkInput.readObject(), (String) networkInput
                    .readObject(), (Character) networkInput.readObject(), (int) networkInput.readObject());
                break;
              case "characterMissedCharacter":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterMissedCharacter((Character) networkInput.readObject(), (String) networkInput
                    .readObject(), (Character) networkInput.readObject());
                break;
              case "characterMoneyChanged":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterMoneyChanged((Character) networkInput.readObject(), (int) networkInput
                    .readObject(), (String) networkInput.readObject());
                break;
              case "characterPutItemOnGround":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterPutItemOnGround((Character) networkInput.readObject(), (Item) networkInput
                    .readObject(), (long) networkInput.readObject());
                break;
              case "characterRespawnCountdown":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterRespawnCountdown((Character) networkInput.readObject(), (int) networkInput
                    .readObject());
                break;
              case "characterRespawned":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterRespawned((Character) networkInput.readObject(), (Place) networkInput
                    .readObject(), (Place) networkInput.readObject());
                break;
              case "characterTookMoney":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterTookMoney((Character) networkInput.readObject(), (Character) networkInput
                    .readObject(), (int) networkInput.readObject(), (String) networkInput.readObject());
                break;
              case "characterWokeUp":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterWokeUp((Character) networkInput.readObject());
                break;
              case "characterWonFight":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterWonFight((Character) networkInput.readObject(), (Character) networkInput
                    .readObject());
                break;
              case "gameOver":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.gameOver((Universe) networkInput.readObject());
                break;
              case "playerGainsItem":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver
                .playerGainsItem((Character) networkInput.readObject(), (Item) networkInput.readObject());
                break;
              case "playerJoined":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.playerJoined((Player) networkInput.readObject());
                break;
              case "playerLosesItem":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver
                .playerLosesItem((Character) networkInput.readObject(), (Item) networkInput.readObject());
                break;
              case "playerMoved":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.playerMoved((Player) networkInput.readObject(), (Place) networkInput.readObject(),
                    (Place) networkInput.readObject());
                break;
              case "playerPutItemInItem":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.playerPutItemInItem((Player) networkInput.readObject(), (Item) networkInput
                    .readObject(), (Item) networkInput.readObject());
                break;
              case "playerQuit":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.playerQuit((Player) networkInput.readObject());
                break;
              case "playerTalkedToNPC":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                final Player player = (Player) networkInput.readObject();
                final NonPlayerCharacter npc = (NonPlayerCharacter) networkInput.readObject();
                final SayTrigger[] triggers = (SayTrigger[]) networkInput.readObject();
                f_worldObserver.playerTalkedToNPC(player, npc, Arrays.asList(triggers));
                break;
              case "playerTookItemFromGround":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.playerTookItemFromGround((Player) networkInput.readObject(), (Item) networkInput
                    .readObject(), (Long) networkInput.readObject());
                break;
              case "playSound":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.playSound((Character) networkInput.readObject(), (String) networkInput.readObject());
                break;
              case "takeitemFromItem":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.takeitemFromItem((Player) networkInput.readObject(), (Item) networkInput.readObject(),
                    (Item) networkInput.readObject());
                break;
              case "worldLoaded":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.worldLoaded((Universe) networkInput.readObject(), (String) networkInput.readObject());
                break;
              case "quit":
                f_parserObserver.quit();
                break;
              case "setCurrentPlayer":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                break;
              case "setSound":
                f_parserObserver.setSound((boolean) networkInput.readObject());
                break;
              case "toggleSound":
                f_parserObserver.toggleSound();
                break;
              case "characterUnequippedWeapon":
                f_parserObserver.setCurrentPlayer((Player) networkInput.readObject());
                f_worldObserver.characterUnequippedWeapon((Character) networkInput.readObject());
                break;
              default:
                f_parserObserver.display(message);
            }
          } else {
            finish();
          }
        }
      } catch (final IOException | ClassNotFoundException e) {
        finish();
        f_parserObserver.display("Multiplay server shutdown: " + e.getMessage());
      }
    }
  }

  private class WriteOutputThread extends Thread {
    Socket f_socket;

    public WriteOutputThread(Socket socket) {
      f_socket = Objects.requireNonNull(socket, "socket cannot be null");
    }

    @Override
    public void run() {
      try (PrintWriter networkOutput = new PrintWriter(f_socket.getOutputStream(), true);) {
        while (!finished) {
          final String userCommand = userInput.poll(500, TimeUnit.MILLISECONDS);
          if (userCommand != null) {
            networkOutput.println(userCommand);
          }
        }
      } catch (final IOException e) {
        e.printStackTrace();
        finish();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private final String  address;
  private final int     port;
  BlockingQueue<String> userInput = new LinkedBlockingQueue<>();

  WriteOutputThread     outThread;

  ReadInputThread       inThread;

  private boolean       finished  = false;

  private final IModelObserver  f_worldObserver;

  private final IDisplayNotifier f_parserObserver;

  /**
   * Will attempt to connect to a server running on the localhost on the default port.
   *
   * @param parserObserver
   *          the Parser Observer from the View that wants a network connection
   * @param worldObserver
   *          the Universe Observer from the View that wants a network connection
   */
  public NetworkClient(IDisplayNotifier parserObserver, IModelObserver worldObserver) {
    this("localhost", parserObserver, worldObserver);
  }

  /**
   * Will attempt to connect to a server at the specified address and port.
   *
   * @param address
   *          Address running a multiplayer server to connect to
   * @param port
   *          Port to connect to
   * @param parserObserver
   *          the Parser Observer from the View that wants a network connection
   * @param worldObserver
   *          the Universe Observer from the View that wants a network connection
   */
  public NetworkClient(String address, int port, IDisplayNotifier parserObserver, IModelObserver worldObserver) {
    this.address = Objects.requireNonNull(address, "Address cannot be null");
    this.port = port;
    f_parserObserver = parserObserver;
    f_worldObserver = worldObserver;
  }
  /**
   * Will attempt to connect to a server at the specified address on the default port.
   *
   * @param address
   *          Address running a multiplayer server to connect to
   * @param parserObserver
   *          the Parser Observer from the View that wants a network connection
   * @param worldObserver
   *          the Universe Observer from the View that wants a network connection
   */
  public NetworkClient(String address, IDisplayNotifier parserObserver, IModelObserver worldObserver) {
    this(address, MultiplayerServerThread.getDefaultPort(), parserObserver, worldObserver);
  }

  /**
   * Disconnect from the multiplayer server
   */
  public void finish() {
    finished = true;
    try {
      if (outThread != null) {
        outThread.join(1000);
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    try {
      if (inThread != null) {
        inThread.join(1000);
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the next message pending to be displayed to the user or null if no messages are
   * pending.
   *
   * @return A string of the next message or null if there are no messages
   * @throws NetworkConnectionClosedException
   *           if there are no messages left and the connection to the server has been closed
   */
  // public String getOutputMessage() throws NetworkConnectionClosedException {
  // if (finished && userOutput.isEmpty()) {
  // throw new NetworkConnectionClosedException();
  // } else {
  // try {
  // return userOutput.poll(500, TimeUnit.MILLISECONDS);
  // } catch (final InterruptedException e) {
  // e.printStackTrace();
  // return null;
  // }
  // }
  // }

  /**
   * Pass a command across the wire to the connected server
   *
   * @param command
   *          The command input by the user
   * @throws NetworkConnectionClosedException
   *           if the connection to the server has been terminated
   */
  public void parseCommand(String command) throws NetworkConnectionClosedException {
    if (!finished) {
      try {
        userInput.put(command);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      throw new NetworkConnectionClosedException("Connection already finished");
    }
  }

  @Override
  public void run() {
    f_parserObserver.display("Connecting to '" + address + "' on port " + port);

    try (Socket socket = new Socket(address, port);) {
      try {
        final PrintWriter networkOutput = new PrintWriter(socket.getOutputStream(), true);
        networkOutput.println(MultiplayerServerThread.CONNECTION_STRING);
      } catch (final IOException e) {
        e.printStackTrace();
        finish();
      }
      inThread = new ReadInputThread(socket);
      outThread = new WriteOutputThread(socket);
      outThread.start();
      inThread.start();
      f_parserObserver.display("Connected");
      outThread.join();
      inThread.join();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    } catch (final UnknownHostException e1) {
      f_parserObserver.display("Unknown host: " + e1.getMessage());
    } catch (final IOException e1) {
      e1.printStackTrace();
    }
  }
}
