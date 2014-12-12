package pavlik.john.dungeoncrawl.model;

import java.util.List;

import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;

/**
 * A class can implement this interface when it wants to be informed of changes in {@link Universe}
 * objects.
 *
 * @author T.J. Halloran
 *
 * @version 1.0
 */
public interface IModelObserver {

  /**
   * Someone is talking
   *
   * @param player
   *          The current player who initiated the action
   * @param speaker
   *          The speaker who is talking
   * @param command
   *          The message that is being spoken
   */
  void broadcastMessage(Character player, String speaker, String command);

  /**
   * A character equipped a weapon.
   *
   * @param character
   *          The character
   * @param weapon
   *          The weapon
   */
  void characterEquippedWeapon(Character character, Weapon weapon);

  /**
   * A character has been healed
   *
   * @param character
   *          The character healed
   * @param heal
   *          The amount of healing performed
   */
  void characterHeal(Character character, int heal);

  /**
   * A character was attacked successfully
   *
   * @param attacker
   *          The attacker
   * @param combatMsg
   *          The type of attack
   * @param target
   *          The attackee
   * @param damage
   *          The amount of damage done
   */
  void characterHitsCharacterFor(Character attacker, String combatMsg, Character target, int damage);

  /**
   * A character was attacked but missed
   *
   * @param character
   *          The attacker
   * @param combatMsg
   *          The type of attack
   * @param target
   *          The defender
   */
  void characterMissedCharacter(Character character, String combatMsg, Character target);

  /**
   * A character gained or lost some money
   *
   * @param character
   *          The current character
   * @param money
   *          The money gained/lost (will be negative [&lt;0] if lost).
   * @param moneyName
   *          The string to display money as
   */
  void characterMoneyChanged(Character character, int money, String moneyName);

  /**
   * Character dropped an item on the ground
   *
   * @param character
   *          The current character
   * @param item
   *          The item dropped
   * @param totalPoints
   *          The number of points that player earned
   */
  void characterPutItemOnGround(Character character, Item item, Long totalPoints);

  /**
   * Notify viewers that a character is on a respawn timer
   *
   * @param character
   *          The current character
   * @param ticksRemaining
   *          The number of ticks remaining until the character wakes up
   */
  void characterRespawnCountdown(Character character, int ticksRemaining);

  /**
   * A character has respawned and moved from the previous to the new location
   *
   * @param character
   *          The character who respawned
   * @param previousLocation
   *          The previous location
   * @param newLocation
   *          the new location
   */
  void characterRespawned(Character character, Place previousLocation, Place newLocation);

  /**
   * A character transferred money to another character
   *
   * @param fromCharacter
   *          The character who lost money
   * @param toCharacter
   *          The character who gained money
   * @param money
   *          The amount of money transferred
   * @param moneyName
   *          The name to refer to money as
   */
  void characterTookMoney(Character fromCharacter, Character toCharacter, int money, String moneyName);

  /**
   * A character unequipped their weapon
   *
   * @param character
   *          The character
   */
  void characterUnequippedWeapon(Character character);

  /**
   * A character woke up from an unconscious state
   *
   * @param character
   *          The character who woke up
   */
  void characterWokeUp(Character character);

  /**
   * A character knocked another character unconscious in a fight
   *
   * @param attacker
   *          The attacker
   * @param target
   *          The loser
   */
  void characterWonFight(Character attacker, Character target);

  /**
   * The game is over. Either someone reached the victory condition or the host quit the game.
   *
   * @param world
   *          The current state of the world.
   */
  void gameOver(Universe world);

  /**
   * A player gained an item
   *
   * @param player
   *          The current player
   * @param f_itemName
   *          The item gained
   */
  void playerGainsItem(Character player, Item f_itemName);

  /**
   * A user joined the game as the specified Player
   *
   * @param player
   *          The player that is currently being played
   */
  void playerJoined(Player player);

  /**
   * A player lost an item
   *
   * @param player
   *          The current player
   * @param f_itemName
   *          The item lost
   *
   */
  void playerLosesItem(Character player, Item f_itemName);

  /**
   * The specified player has moved from the startLocation to the finishLocation
   *
   * @param player
   *          The current player
   * @param startLocation
   *          The starting Place
   * @param finishLocation
   *          The finishing Place
   */
  void playerMoved(Player player, Place startLocation, Place finishLocation);

  /**
   * A player moved an item from his/her inventory into an item container, such as a chest.
   *
   * @param player
   *          The current player
   * @param itemMoved
   *          The item that was moved
   * @param itemContainer
   *          The container the itemMoved was stored inside
   */
  void playerPutItemInItem(Player player, Item itemMoved, Item itemContainer);

  /**
   * A user quit the game while playing the specified Player
   *
   * @param player
   *          The player that was being played by the user who quit
   */
  void playerQuit(Player player);

  /**
   * A player is talking to an NPC
   *
   * @param player
   *          The current layer
   * @param npc
   *          The NPC being talked to
   * @param currentEvents
   *          The list of events that represents what the player can say to the NPC
   */
  void playerTalkedToNPC(Player player, NonPlayerCharacter npc, List<SayTrigger> currentEvents);

  /**
   * Player picked up an item from the ground
   *
   * @param player
   *          The current player
   * @param item
   *          The item picked up
   * @param pointsAdded
   *          The number of points that player earned
   */
  void playerTookItemFromGround(Player player, Item item, Long pointsAdded);

  /**
   * Notify everyone in the region of cause to play the appropriate sound file
   *
   * @param cause
   *          The character who triggered the sound effect
   * @param soundPath
   *          The path to the sound effect to play
   */
  void playSound(Character cause, String soundPath);

  /**
   * A player moved an item from an item container, such as a chest, to his/her inventory.
   *
   * @param player
   *          The current player
   * @param itemMoved
   *          The item that was moved
   * @param itemContainer
   *          The container that the item was stored inside before moving
   */
  void takeitemFromItem(Player player, Item itemMoved, Item itemContainer);

  /**
   * A new world was loaded
   *
   * @param world
   *          The new world
   * @param fileName
   *          The fileName of the XML world that was loaded
   */
  void worldLoaded(Universe world, String fileName);

}
