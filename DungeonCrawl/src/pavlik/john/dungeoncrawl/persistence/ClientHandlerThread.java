package pavlik.john.dungeoncrawl.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.IModelObserver;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Weapon;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.view.parser.IDisplayNotifier;
import pavlik.john.dungeoncrawl.view.parser.Parser;

/**
 * A thread that handles client interactions with the world at the server side.
 *
 * @author John
 */
public class ClientHandlerThread extends Thread {
  private class Observer implements IDisplayNotifier, IModelObserver {

    private Player f_player;

    @Override
    public void broadcastMessage(Character player, String speaker, String command) {
      transmit("broadcastMessage");
      transmit(f_player);
      transmit(player);
      transmit(speaker);
      transmit(command);
    }

    @Override
    public void characterEquippedWeapon(Character character, Weapon weapon) {
      transmit("characterEquippedWeapon");
      transmit(f_player);
      transmit(character);
      transmit(weapon);
    }

    @Override
    public void characterHeal(Character character, int heal) {
      transmit("characterHeal");
      transmit(f_player);
      transmit(character);
      transmit(heal);
    }

    @Override
    public void characterHitsCharacterFor(Character attacker, String combatMsg, Character target, int damage) {
      transmit("characterHitsCharacterFor");
      transmit(f_player);
      transmit(attacker);
      transmit(combatMsg);
      transmit(target);
      transmit(damage);
    }

    @Override
    public void characterMissedCharacter(Character character, String combatMsg, Character target) {
      transmit("characterMissedCharacter");
      transmit(f_player);
      transmit(character);
      transmit(combatMsg);
      transmit(target);
    }

    @Override
    public void characterMoneyChanged(Character character, int money, String moneyName) {
      transmit("characterMoneyChanged");
      transmit(f_player);
      transmit(character);
      transmit(money);
      transmit(moneyName);
    }

    @Override
    public void characterPutItemOnGround(Character character, Item item, Long totalPoints) {
      transmit("characterPutItemOnGround");
      transmit(f_player);
      transmit(character);
      transmit(item);
      transmit(totalPoints);
    }

    @Override
    public void characterRespawnCountdown(Character character, int ticksRemaining) {
      transmit("characterRespawnCountdown");
      transmit(f_player);
      transmit(character);
      transmit(ticksRemaining);
    }

    @Override
    public void characterRespawned(Character character, Place previousLocation, Place newLocation) {
      transmit("characterRespawned");
      transmit(f_player);
      transmit(character);
      transmit(previousLocation);
      transmit(newLocation);
    }

    @Override
    public void characterTookMoney(Character fromCharacter, Character toCharacter, int money, String moneyName) {
      transmit("characterTookMoney");
      transmit(f_player);
      transmit(fromCharacter);
      transmit(toCharacter);
      transmit(money);
      transmit(moneyName);
    }

    @Override
    public void characterUnequippedWeapon(Character character) {
      transmit("characterUnequippedWeapon");
      transmit(f_player);
      transmit(character);
    }

    @Override
    public void characterWokeUp(Character character) {
      transmit("characterWokeUp");
      transmit(f_player);
      transmit(character);
    }

    @Override
    public void characterWonFight(Character attacker, Character target) {
      transmit("characterWonFight");
      transmit(f_player);
      transmit(attacker);
      transmit(target);
    }

    @Override
    public void display(String message) throws NullPointerException {
      transmit(message);
    }

    @Override
    public void gameOver(Universe world) {
      transmit("gameOver");
      transmit(f_player);
      transmit(world);
      f_parser.shutdown();
    }

    @Override
    public void playerGainsItem(Character player, Item f_itemName) {
      transmit("playerGainsItem");
      transmit(f_player);
      transmit(player);
      transmit(f_itemName);
    }

    @Override
    public void playerJoined(Player player) {
      transmit("playerJoined");
      transmit(f_player);
      transmit(player);
    }

    @Override
    public void playerLosesItem(Character player, Item f_itemName) {
      transmit("playerLosesItem");
      transmit(f_player);
      transmit(player);
      transmit(f_itemName);
    }

    @Override
    public void playerMoved(Player player, Place startLocation, Place finishLocation) {
      transmit("playerMoved");
      transmit(f_player);
      transmit(player);
      transmit(startLocation);
      transmit(finishLocation);
    }

    @Override
    public void playerPutItemInItem(Player player, Item itemMoved, Item itemContainer) {
      transmit("playerPutItemInItem");
      transmit(f_player);
      transmit(player);
      transmit(itemMoved);
      transmit(itemContainer);
    }

    @Override
    public void playerQuit(Player player) {
      transmit("playerQuit");
      transmit(f_player);
      transmit(player);
    }

    @Override
    public void playerTalkedToNPC(Player player, NonPlayerCharacter npc, List<SayTrigger> currentEvents) {
      transmit("playerTalkedToNPC");
      transmit(f_player);
      transmit(player);
      transmit(npc);
      transmit(currentEvents.toArray(new SayTrigger[0]));
    }

    @Override
    public void playerTookItemFromGround(Player player, Item item, Long pointsAdded) {
      transmit("playerTookItemFromGround");
      transmit(f_player);
      transmit(player);
      transmit(item);
      transmit(pointsAdded);
    }

    @Override
    public void playSound(Character cause, String soundPath) {
      transmit("playSound");
      transmit(f_player);
      transmit(cause);
      transmit(soundPath);
    }

    @Override
    public void quit() {
      transmit("quit");
      f_parser.shutdown();
    }

    @Override
    public void setCurrentPlayer(Player player) {
      transmit("setCurrentPlayer");
      transmit(player);
      f_player = player;
    }

    @Override
    public void setSound(boolean enable) {
      transmit("setSound");
      transmit(enable);
    }

    @Override
    public void takeitemFromItem(Player player, Item itemMoved, Item itemContainer) {
      transmit("takeitemFromItem");
      transmit(f_player);
      transmit(player);
      transmit(itemMoved);
      transmit(itemContainer);
    }

    @Override
    public void toggleSound() {
      transmit("toggleSound");
    }

    private void transmit(Serializable object) {
      try {
        outputMessages.put(object);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void worldLoaded(Universe world, String fileName) {
      transmit("worldLoaded");
      transmit(f_player);
      transmit(world);
      transmit(fileName);
      f_parser.clearPlayer();
    }
  }

  private class ReadInputThread extends Thread {
    Socket f_socket;

    public ReadInputThread(Socket socket) {
      f_socket = Objects.requireNonNull(socket, "socket cannot be null");
    }

    @Override
    public void run() {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(f_socket.getInputStream()));) {

        while (!f_finished.get()) {
          final String input = in.readLine();
          if (input != null) {
            processInput(input);
          } else {
            f_finished.set(true);
          }
        }
      } catch (final IOException e) {
        finish();
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
      try (ObjectOutputStream out = new ObjectOutputStream(f_socket.getOutputStream());) {
        while (!f_finished.get()) {
          final Serializable outputMessage = outputMessages.poll(500, TimeUnit.MILLISECONDS);
          if (outputMessage != null) {
            out.writeObject(outputMessage);
            out.reset();
          }
        }
        while (!outputMessages.isEmpty()) {
          final Serializable outputMessage = outputMessages.poll(500, TimeUnit.MILLISECONDS);
          if (outputMessage != null) {
            out.writeObject(outputMessage);
            out.reset();
          }
        }
      } catch (final IOException e) {
        finish();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private final Socket                      f_socket;
  private final Controller             f_controller;
  private final Parser           f_parser;
  private final BlockingQueue<Serializable> outputMessages = new LinkedBlockingQueue<>();
  private final AtomicBoolean               f_finished     = new AtomicBoolean(false);

  private final Observer                    f_parserWorldObserver;

  private final ReadInputThread             f_readThread;

  private final WriteOutputThread           f_writeThread;

  /**
   * Public constructor
   *
   * @param worldController
   *          The worldController for the world.
   * @param messages
   *          The messages to display to the user
   * @param socket
   *          The socket the user is connected on
   */
  public ClientHandlerThread(Controller worldController, ResourceBundle messages, Socket socket) {
    super("ClientHandlerThread");

    f_socket = socket;
    f_controller = worldController;
    f_parserWorldObserver = new Observer();
    f_parser = new Parser(f_controller, f_parserWorldObserver, f_parserWorldObserver, messages, false);
    f_readThread = new ReadInputThread(f_socket);
    f_writeThread = new WriteOutputThread(f_socket);
  }

  /**
   * Terminate this client thread eventually
   */
  public void finish() {
    f_finished.set(true);
  }

  private void processInput(String inputLine) {
    f_parser.parse(inputLine);
  }

  @Override
  public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(f_socket.getInputStream()));) {
      final String input = in.readLine();
      if (input != null) {
        if (input.equals(MultiplayerServerThread.CONNECTION_STRING)) {
          startWorking();
        }
      }
    } catch (final IOException e) {
      finish();
    }
    try {
      f_socket.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    f_controller.releasePlayer(f_parser.getPlayer());
    f_controller.getWorld().removeObserver(f_parserWorldObserver);

  }

  private void startWorking() {
    f_readThread.start();
    f_writeThread.start();
    f_parser.parse("play as");
    try {
      f_readThread.join();
      f_writeThread.join();
    } catch (final InterruptedException e1) {
      e1.printStackTrace();
    }
  }
}
