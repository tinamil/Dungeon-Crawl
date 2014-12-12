package pavlik.john.dungeoncrawl.controller;

import java.nio.file.InvalidPathException;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;

import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Consumable;
import pavlik.john.dungeoncrawl.model.Container;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.IModelObserver;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Weapon;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.Item.Action;
import pavlik.john.dungeoncrawl.model.events.Trigger;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.model.events.triggers.SightTrigger;
import pavlik.john.dungeoncrawl.persistence.GamePersistence;
import pavlik.john.dungeoncrawl.view.TextUtilities;

/**
 * 
 * @author John Pavlik
 *
 * @version 1.2
 * @see Universe
 */
public final class Controller {

  private Universe f_world;
  private Timer f_timer;

  /**
   * Creates a new instance of <code>Controller</code> for the default world.
   *
   * @throws PersistenceStateException
   *           if unable to load the default world
   *
   */
  public Controller() throws PersistenceStateException {
    this(GamePersistence.DEFAULT_WORLD);
  }

  /**
   * Creates a new instance of <code>Controller</code> for the specified world file, which it
   * loads.
   *
   * @param fileName
   *          the world file to load
   * @throws PersistenceStateException
   *           if anything goes wrong
   */
  public Controller(String fileName) throws PersistenceStateException {
    try {
      setWorld(GamePersistence.loadWorld(fileName), fileName);
    } catch (IllegalStateException | NullPointerException | InvalidPathException | PersistenceStateException e) {
      throw e;
    }
    // Updates periodic world values
  }

  /**
   * Player is attacking a character
   *
   * @param player
   *          The current player
   * @param command
   *          The name of the character to attack
   * @return A Result of CHARACTER_NOT_FOUND, CANT_USE_ITEM (if either is not combat capable),
   *         ALREADY_IN_COMBAT, or SUCCESS
   */
  public synchronized Result attack(Player player, String command) {
    final Character targetCharacter = getWorld().getCharacter(command);
    if (targetCharacter == null) {
      return Result.CHARACTER_NOT_FOUND;
    } else {
      if ((player.getCharacterClass() == null) || (targetCharacter.getCharacterClass() == null)) {
        return Result.CANT_USE_ITEM;
      } else if (targetCharacter.getLocation() == player.getLocation()) {
        if (player.inCombat() && player.getCurrentTarget() == targetCharacter) {
          return Result.ALREADY_IN_COMBAT;
        } else if (!targetCharacter.isConscious()) {
          return Result.UNCONSCIOUS_TARGET;
        } else {
          player.startAttack(targetCharacter);
          return Result.SUCCESS;
        }
      } else {
        return Result.CHARACTER_NOT_FOUND;
      }
    }
  }

  private boolean canTakeItem(Container source, Item item) {
    return source.isPresent(item) && item.isTakeable();
  }

  /**
   * Check to see if a player can travel in the specified direction
   *
   * @param player
   *          The current player
   * @param direction
   *          The direction to travel
   * @return true if travel is allowed and the player is not missing any needed items
   */
  public synchronized boolean canTravel(Player player, Navigation direction) {
    return player.getLocation().isTravelAllowedToward(direction) && !isMissingItems(player, direction);
  }

  /**
   * Equip a player with the specified weapon
   *
   * @param player
   *          The current player
   * @param weaponString
   *          The name of the weapon to equip
   * @return A Result of type SUCCESS, CANT_USE_ITEM, ITEM_NOT_TAKEABLE, or ITEM_NOT_FOUND
   */
  public synchronized Result equip(Player player, String weaponString) {
    if (weaponString == null) {
      player.unequip();
      return Result.SUCCESS;
    }
    final Optional<Item> item = getItemForPlayer(player, weaponString, true);
    if (item.isPresent()) {
      final boolean isWeapon = item.get() instanceof Weapon;
      Weapon weapon = null;
      if (isWeapon) {
        weapon = (Weapon) item.get();
      }
      if (!isWeapon || player.getCharacterClass() == null
          || !player.getCharacterClass().canUseWeaponType(weapon.getType())) {
        return Result.CANT_USE_ITEM;
      } else {
        player.setCurrentWeapon(weapon);
        return Result.SUCCESS;
      }
    } else {
      return Result.ITEM_NOT_FOUND;
    }
  }

  /**
   * Builds an itemized list of available players
   *
   * @return a String ready for display
   */
  public synchronized String getAvailablePlayers() {
    final StringBuffer buffer = new StringBuffer();
    int count = 1;
    for (final Player player : f_world.getPlayers()) {
      if (!player.isOccupied()) {
        buffer.append(count++);
        buffer.append(". ");
        buffer.append(player.toString());
        buffer.append(TextUtilities.LINESEP);
      }
    }
    return buffer.toString();
  }

  private Item getItem(String itemName) {
    itemName = itemName.toUpperCase();
    Item item = getWorld().getItem(itemName);
    if (item == null) {
      /*
       * Perform a check to see if the user put the article (e.g. "a", "an", "the") in front of the
       * name of the item.
       */
      itemName = TextUtilities.trimArticles(itemName, getWorld().getValidArticles());
      item = getWorld().getItem(itemName);
    }
    return item;
  }

  /**
   * Attempt to lookup an item from all of the items the player has access to, primarily the
   * player's inventory and the place the player is currently located.
   *
   * @param player
   *          The current player looking for an item
   * @param itemName
   *          The name of the item to lookup
   * @param takeItem
   *          if true will move the item into the players inventory from wherever it was found
   * @return An Optional Item that will be empty if the player could not access the provided item or
   *         the item could not be identified.
   */
  public synchronized Optional<Item> getItemForPlayer(Player player, String itemName, boolean takeItem) {
    final Item item = getWorld().getItem(itemName);

    // If item not found in player inventory, check to see if it's located at this place in
    // the world.
    if (takeItem && !player.getContainer().isPresent(item) && player.getLocation().getContainer().isPresent(item)) {
      swapItem(player, player.getContainer(), player.getLocation().getContainer(), itemName, Item.Action.TAKE);
    }
    if (player.getContainer().isPresent(item) || player.getLocation().getContainer().isPresent(item)) {
      return Optional.ofNullable(item);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Gets the world (i.e., model) associated with this controller.
   *
   * @return the world associated with this controller.
   */
  public synchronized Universe getWorld() {
    return f_world;
  }

  /**
   * A player is giving an item away to another player.
   *
   * @param itemName
   *          the item to give away
   * @param fromPlayer
   *          The player who is giving away the item
   * @param toPlayerName
   *          The player getting the item
   * @return a Result, with the reason set as the item name or player name if either could not be
   *         found
   */
  public synchronized Result giveItem(String itemName, Player fromPlayer, String toPlayerName) {
    final Player toPlayer = getWorld().getPlayer(toPlayerName);
    final Optional<Item> item = getItemForPlayer(fromPlayer, itemName, true);
    if (!item.isPresent()) {
      final Result retVal = Result.ITEM_NOT_FOUND;
      retVal.setReason(itemName);
      return retVal;
    }

    if (toPlayer == null || !fromPlayer.sameLocationAs(toPlayer)) {
      final Result retVal = Result.CHARACTER_NOT_FOUND;
      retVal.setReason(toPlayerName);
      return retVal;
    }
    Result retVal;
    if ((retVal = swapItem(fromPlayer, toPlayer.getContainer(), fromPlayer.getContainer(), item.get().getName(),
        Action.GIVE)).equals(Result.SUCCESS)) {
      f_world.playerLosesItem(fromPlayer, item.get());
      f_world.playerGainsItem(toPlayer, item.get());
      return Result.SUCCESS;
    } else {
      return retVal;
    }
  }

  /**
   * Allow one player to give money to another player
   *
   * @param moneyString
   *          Must be a string that can be parsed as an integer that is greater than 1
   * @param fromPlayer
   *          The player giving money
   * @param toPlayerName
   *          The player receiving money
   * @return An
   */
  public synchronized Result giveMoney(String moneyString, Player fromPlayer, String toPlayerName) {
    final Player toPlayer = getWorld().getPlayer(toPlayerName);
    final int money = Integer.parseInt(moneyString);
    if (fromPlayer.getMoney() < money || money < 1) {
      return Result.INSUFFICIENT_MONEY;
    } else if (toPlayer == null || !fromPlayer.sameLocationAs(toPlayer)) {
      final Result retVal = Result.CHARACTER_NOT_FOUND;
      retVal.setReason(toPlayerName);
      return retVal;
    } else {
      fromPlayer.changeMoney(-money);
      f_world.moneyChanged(fromPlayer, -money);
      toPlayer.changeMoney(money);
      f_world.moneyChanged(toPlayer, money);
      return Result.SUCCESS;
    }
  }

  /**
   * Check if the Player is missing any items necessary to to the place in the specified direction
   *
   * @param player
   *          Current player
   * @param direction
   *          The direction to move
   * @return true if the player is missing items and cannot move there, false if the player has all
   *         the items
   */
  public synchronized boolean isMissingItems(Player player, Navigation direction) {
    if (player.getLocation().isTravelAllowedToward(direction)) {
      final Place newPlayerLocation = player.getLocation().getTravelDestinationToward(direction);
      final Set<Item> missingItems = newPlayerLocation.missingItems(player.getContainer());
      return !missingItems.isEmpty();
    } else {
      return false;
    }
  }

  /**
   * Loads a previously saved {@link Universe} from a file.
   *
   * @param fileName
   *          The name of the file to load
   * @throws PersistenceStateException
   *           If anything goes wrong with the loading process
   */
  public synchronized void loadWorld(String fileName) throws PersistenceStateException {
    final Universe newWorld = GamePersistence.loadWorld(fileName);
    setWorld(newWorld, fileName);
  }

  /**
   * Attempt to claim a player for the user to play as
   *
   * @param name
   *          The string unique name of the player to play as
   * @return a Player if available, or null if the player could not be identified by name or was
   *         currently occupied by a different user
   */
  public synchronized Player playAs(String name) {
    Player player = f_world.getPlayer(name);
    if (player == null && name.matches("\\d+")) {
      final int chosen = Integer.parseInt(name);
      int index = 1;
      for (final Player checkPlayer : f_world.getPlayers()) {
        if (!checkPlayer.isOccupied()) {
          if (index == chosen) {
            player = checkPlayer;
            break;
          } else {
            index++;
          }
        }
      }
    }
    if (player != null) {
      if (player.isOccupied()) {
        player = null;
      } else {
        player.setOccupied(true);
        f_world.playerJoined(player);
      }
    }
    return player;
  }

  /**
   * Move an item from the player's inventory to another item that is a Container
   *
   * @param player
   *          The current player
   * @param itemToPut
   *          The name of the item to be stored
   * @param itemContainerString
   *          The name of the item that is a container to store it in
   * @return true if the moving the item was successful, false otherwise
   */
  public synchronized boolean putItemInItemContainer(Player player, String itemToPut, String itemContainerString) {
    final Optional<Item> item = getItemForPlayer(player, itemToPut, true);
    final Optional<Item> itemContainer = getItemForPlayer(player, itemContainerString, false);

    if (item != null
        && itemContainer.isPresent()
        && itemContainer.get().getContainer() != null
        && swapItem(player, itemContainer.get().getContainer(), player.getContainer(), itemToPut, Action.GIVE).equals(
            Result.SUCCESS)) {
      getWorld().putItemInItem(player, item.get(), itemContainer.get());
      return true;
    } else {
      return false;
    }
  }

  /**
   * Release a player character so that someone else can use it
   *
   * @param player
   *          The player to release
   */
  public synchronized void releasePlayer(Player player) {
    if (player != null && player.isOccupied()) {
      player.setOccupied(false);
      f_world.playerQuit(player);
    }
  }

  /**
   * Saves the current state of the {@link Universe} to a file. This world can be loaded and game play
   * resumed using {@link #loadWorld(String)}.
   *
   * @param fileName
   *          The name of the file to create
   * @throws PersistenceStateException
   *           if anything goes wrong during saving
   */
  public synchronized void saveWorld(String fileName) throws PersistenceStateException {
    GamePersistence.saveWorld(f_world, fileName);
  }

  /**
   * Sets the world (i.e., model) associated with this controller. The set of observers of the old
   * world are setup to observer the new world.
   *
   * @param world
   *          a non-null game world.
   * @param fileName
   *          The filename of the new world
   *
   * @throws NullPointerException
   *           if world is null
   */
  private void setWorld(Universe world, String fileName) throws NullPointerException {
    if (world == null) {
      final Universe tempWorld = new Universe();
      for (final IModelObserver o : f_world.getObservers()) {
        tempWorld.addObserver(o);
      }
      f_world = tempWorld;
      f_world.worldLoaded("Unable to load world, loaded a trivial world instead.");
      return;
    } else if (f_world != null) {
      /*
       * Transfer all observers of the old world to the new world.
       */
      for (final IModelObserver o : f_world.getObservers()) {
        world.addObserver(o);
        f_world.removeObserver(o);
      }
    }
    f_world = world;
    world.worldLoaded(fileName);
    if (f_timer != null) {
      f_timer.cancel();
    }
    f_timer = new Timer(true);
    f_timer.schedule(new ControllerTimer(this), f_world.getTickRate(), f_world.getTickRate());
  }

  /**
   * Stop the worldtimer and let it gracefully shut down at the conclusion of the current tick if
   * running.
   */
  public void stopWorld() {
    f_timer.cancel();
  }

  /**
   * Swap an item between the source and destination containers.
   *
   * @param f_player
   *          The current player
   * @param destination
   *          A Container to move the specified item to.
   * @param source
   *          A container that currently contains the item.
   * @param itemName
   *          The string unique identifier/name of the item.
   * @param itemAction
   *          The type of action performed with this swap, either a drop or pickup
   * @return An Optional of Item if the item was successfully moved, empty otherwise.
   */
  public synchronized Result swapItem(Player f_player, Container destination, Container source, String itemName,
      Action itemAction) {
    final Item worldItem = getItem(itemName);

    if (worldItem != null && source.isPresent(worldItem)) {
      if (canTakeItem(source, worldItem)) {
        if (source.moveItem(destination, worldItem)) {
          switch (itemAction) {
            case DROP:
              final long dropPoints = worldItem.getDropAndZeroizePoints();
              final long dropPlacePoints = worldItem.getDropAndZeroizePoints(f_player.getLocation());
              final long totalPoints = dropPoints + dropPlacePoints;
              f_player.addPoints(totalPoints);
              if (f_player.getCurrentWeapon().equals(worldItem)) {
                equip(f_player, null);
              }
              getWorld().characterDroppedItem(f_player, worldItem, totalPoints);
              break;
            case TAKE:
              final long points = worldItem.getTakeAndZeroizePoints();
              f_player.addPoints(points);
              getWorld().playerTookItem(f_player, worldItem, points);
              break;
            case GIVE:
              // Do nothing
          }
          return Result.SUCCESS;
        } else {
          return Result.UNKNOWN_FAILURE;
        }
      } else {
        final Result retVal = Result.ITEM_NOT_TAKEABLE;
        retVal.setReason(worldItem.cantTakeMessage());
        return retVal;
      }
    } else {
      return Result.ITEM_NOT_FOUND;
    }
  }

  /**
   * Attempt to synthesize items for the player. Will destroy (move to nowhere place) old items and
   * give new item to the player.
   *
   * @param player
   *          The current player
   * @param itemNames
   *          The items that the player is attempting to synthesize
   * @return a {@link Result} result, if Synthesis result is item not found, the reason will be set
   */
  public synchronized Result synthesizeItems(Player player, Set<String> itemNames) {
    final Set<Item> items = new HashSet<>(itemNames.size());
    for (final String name : itemNames) {
      final Optional<Item> item = getItemForPlayer(player, name, true);
      if (item.isPresent()) {
        items.add(item.get());
      } else {
        return Result.ITEM_NOT_FOUND.setReason(name);
      }
    }
    final Item newItem = getWorld().synthesizeItems(items);
    if (newItem != null) {
      for (final Item item : items) {
        Result result;
        if ((result = swapItem(player, f_world.getNowherePlace().getContainer(), player.getContainer(), item.getName(),
            Action.GIVE)).equals(Result.SUCCESS)) {
          f_world.playerLosesItem(player, item);
        } else {
          return result;
        }
      }
      swapItem(player, player.getContainer(), f_world.getNowherePlace().getContainer(), newItem.getName(), Action.GIVE);
      f_world.playerGainsItem(player, newItem);
      return Result.SUCCESS;
    } else {
      return Result.NO_ITEM_CREATED;
    }
  }

  /**
   * Move an item from the inside of another item that is a Container to the Player's inventory
   *
   * @param player
   *          The current player
   * @param itemToTake
   *          The name of the item to be stored
   * @param itemContainerString
   *          The name of the item that is a container to store it in
   * @return true if the moving the item was successful, false otherwise
   */
  public synchronized boolean takeItemFromItemContainer(Player player, String itemToTake, String itemContainerString) {
    final Item item = getWorld().getItem(itemToTake);
    final Optional<Item> itemContainer = getItemForPlayer(player, itemContainerString, false);
    if (item != null
        && itemContainer.isPresent()
        && itemContainer.get().getContainer() != null
        && swapItem(player, player.getContainer(), itemContainer.get().getContainer(), itemToTake, Action.GIVE).equals(
            Result.SUCCESS)) {
      getWorld().takeItemfromItem(player, item, itemContainer.get());
      return true;
    } else {
      return false;
    }
  }

  /**
   * Initiate a conversation with an NPC
   *
   * @param player
   *          The current player
   * @param npc
   *          The NPC the player is talking to
   * @return A list of Events that represent what the Player can say to the NPC
   */
  public synchronized List<SayTrigger> talkToNPC(Player player, NonPlayerCharacter npc) {
    final List<SayTrigger> currentEvents = npc.getSayTriggers(player);
    getWorld().talkToNPC(player, npc, currentEvents);
    return currentEvents;
  }

  /**
   * Tick the world forward
   *
   * @param numTicks
   *          the number of ticks since this world started
   */
  public synchronized void tick(long numTicks) {
    try {
      f_world.tick(numTicks);
    } catch (final Exception e) {
      e.printStackTrace();
      f_world.notifyException("Exception " + e.toString());
    }
  }

  /**
   * Moves the player in the direction indicated.
   *
   * @param player
   *          The current player
   * @param direction
   *          The direction the user wants the player to travel.
   * @throws NullPointerException
   *           if direction is null
   */
  public synchronized void travel(Player player, Navigation direction) throws NullPointerException {
    if (direction == null) {
      throw new NullPointerException("direction cannot be null");
    }
    final Place playerLocation = player.getLocation();
    if (canTravel(player, direction)) {
      final Place newPlayerLocation = playerLocation.getTravelDestinationToward(direction);
      final Place startLocation = player.getLocation();
      player.disengageCombat();
      /*
       * Move the player
       */
      player.setLocation(newPlayerLocation);
      getWorld().playerMoved(player, startLocation, player.getLocation());
      /*
       * Check to see if the new location ends the game.
       */
      if (newPlayerLocation.getWinCondition()) {
        getWorld().setGameOver(true);
      } else {
        for (final Character character : newPlayerLocation.getAllCharacters()) {
          if (character instanceof NonPlayerCharacter) {
            final NonPlayerCharacter npc = (NonPlayerCharacter) character;
            for (final Trigger trigger : npc.getCurrentState().getEventTriggers()) {
              if (trigger instanceof SightTrigger) {
                trigger.execute(player);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Allow a player to use an item on the specified target character
   *
   * @param player
   *          The current player
   * @param itemName
   *          The name of the item to use
   * @param targetName
   *          The name of the character to target
   * @return a Result CANT_USE_ITEM if it's not consumable, a SUCCESS, an ALREADY_IN_COMBAT if the
   *         item is on cooldown, or an ITEM_NOT_FOUND
   */
  public synchronized Result useItem(Player player, String itemName, String targetName) {
    final Character character = player.getLocation().getCharacter(targetName);
    if (character == null) {
      return Result.CHARACTER_NOT_FOUND;
    }
    try {
      final Item item = getItem(itemName);
      if (player.getContainer().isPresent(item)) {
        if (!(item instanceof Consumable)) {
          return Result.CANT_USE_ITEM;
        } else {
          final Consumable consumable = (Consumable) item;
          if (consumable.canUseItem(getWorld().getCurrentTick())) {
            player.useItem(consumable, character);
            return Result.SUCCESS;
          } else if (!consumable.hasUsesLeft()) {
            return Result.ITEM_CONSUMED;
          } else {
            final Result retVal = Result.ALREADY_IN_COMBAT;
            retVal.setReason(Double.toString(getWorld().getTickRate() / 1000.0
                * consumable.ticksRemaining(getWorld().getCurrentTick())));
            return retVal;
          }
        }
      } else {
        return Result.ITEM_NOT_FOUND;
      }
    } catch (final NoSuchElementException e) {
      return Result.ITEM_NOT_FOUND;
    }
  }
}
