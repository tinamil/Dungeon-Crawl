package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pavlik.john.dungeoncrawl.model.Consumable.Effect;
import pavlik.john.dungeoncrawl.model.Consumable.EffectTarget;
import pavlik.john.dungeoncrawl.model.Consumable.EffectType;
import pavlik.john.dungeoncrawl.model.events.State;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.view.TextUtilities;

/**
 * The simulated universe. Represents the core of the model, to which everything else is connected.
 *
 * @author John Pavlik
 *
 * @version 1.2
 */
public final class Universe implements Serializable {

  /**
   *
   */
  private static final long                     serialVersionUID        = 1L;

  private final Set<String>                     f_articles              = new TreeSet<>();

  /**
   * A map from an upper-case name (thus non-case sensitive) to a corresponding {@link Place}
   * instance. Typically the key to this map will be <code>name.toUpperCase()</code>.
   */
  private final Map<String, Place>              f_keyToPlace            = new HashMap<>();
  /**
   * A map from an upper-case name (thus non-case sensitive) to a corresponding {@link Item}
   * instance. Typically the key to this map will be <code>name.toUpperCase()</code>.
   */
  private final Map<String, Item>               f_keyToItem             = new HashMap<>();

  /**
   * A map from an upper-case name to a corresponding {@link NonPlayerCharacter} instance.
   */
  private final Map<String, NonPlayerCharacter> f_keyToNPCs             = new HashMap<>();

  private final Map<String, HasContainer>       f_keyToContainers       = new HashMap<>();
  /**
   * A place that always exists in every world. It represents nowhere.
   */
  private final Place                           f_nowhere               = createPlace("Very Remote Place", "a",
                                                                            "You are in a very remote place.", false,
                                                                            null);

  /**
   * A {@link Player} that is controlled by, and represents, the user of the game.
   */
  private final Map<String, Player>             f_keyToPlayers          = new TreeMap<String, Player>();

  /**
   * The set of observers for this world. Notified when this world has changed in some interesting
   * way.
   *
   * @see #addObserver(IModelObserver)
   * @see #removeObserver(IModelObserver)
   * @see #getObservers()
   * @see #notifyObservers()
   */
  private final transient Set<IModelObserver>   f_observers             = new HashSet<IModelObserver>();

  private final Map<String, CharacterClass>     f_keyToCharacterClasses = new HashMap<String, CharacterClass>();
  private long                                  f_tickRate              = 1000;
  private long                                  f_currentTick           = 0;
  private boolean                               f_allowCombat           = false;

  /**
   * A map that takes a set of items, and maps it to a new item to be created
   */
  Map<Set<Item>, Item>                          f_itemCombinations      = new HashMap<>();

  private String                                f_moneyName             = "gold pieces";

  private boolean                               f_gameWon               = false;

  /**
   * Add this item to the world
   *
   * @param item
   *          The item to add
   */
  public void addItem(Item item) {
    f_keyToItem.put(item.getName().toUpperCase(), item);
    if (item.getContainer() != null) {
      f_keyToContainers.put(item.getName().toUpperCase(), item);
    }
    f_articles.add(item.getArticle());
  }

  /**
   * Add a new recipe / item synthesis combination to the world. If all of the items in the set
   * items are combined, the newItem is retrieved from the nowhereplace and returned to the player.
   *
   * @param items
   *          A Set of Items to combine
   * @param newItem
   *          The new item to be created
   */
  public void addItemSynthesis(Set<Item> items, Item newItem) {
    f_itemCombinations.put(items, newItem);
  }

  /**
   * Adds an observer to be notified when the world has changed in some interesting way.
   *
   * @param observer
   *          the object to notify of changes to this world.
   * @throws NullPointerException
   *           if observer is null
   */
  public void addObserver(IModelObserver observer) throws NullPointerException {
    if (observer == null) {
      throw new NullPointerException("observer cannot be null");
    }
    f_observers.add(observer);
  }

  /**
   * A character is talking somewhere
   *
   * @param player
   *          The current player
   * @param speaker
   *          The character talking
   * @param command
   *          The message being spoken
   */
  public void broadcastMessage(Character player, String speaker, String command) {
    for (final IModelObserver observer : f_observers) {
      observer.broadcastMessage(player, speaker, command);
    }
  }

  /**
   * Check if this character class has already been defined
   *
   * @param name
   *          The character class
   * @return true if so
   */
  public boolean characterClassExists(String name) {
    if (f_keyToCharacterClasses.containsKey(name.toUpperCase())) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * A character dropped an item on the ground
   *
   * @param character
   *          The current character
   * @param item
   *          The item taken
   * @param totalPoints
   *          The amount of points the player gained
   */
  public void characterDroppedItem(Character character, Item item, long totalPoints) {
    for (final IModelObserver observer : f_observers) {
      observer.characterPutItemOnGround(character, item, totalPoints);
    }
  }

  /**
   * A character equipped a weapon.
   *
   * @param character
   *          The character
   * @param weapon
   *          The weapon
   */
  public void characterEquippedWeapon(Character character, Weapon weapon) {
    for (final IModelObserver observer : f_observers) {
      observer.characterEquippedWeapon(character, weapon);
    }
  }

  /**
   * A character was healed
   *
   * @param character
   *          The character
   * @param heal
   *          The amount of healing
   */
  public void characterHeal(Character character, int heal) {
    for (final IModelObserver observer : f_observers) {
      observer.characterHeal(character, heal);
    }
  }

  /**
   * A character swung and hit another character
   *
   * @param attacker
   *          The character performing the action
   * @param combatMsg
   *          The type of attack performed
   * @param target
   *          The target of the attack
   * @param damage
   *          The amount of damage done
   */
  public void characterHitsCharacterFor(Character attacker, String combatMsg, Character target, int damage) {
    if (target.getCurrentHealth() - damage < 0) {
      damage = target.getCurrentHealth();
    }
    for (final IModelObserver observer : f_observers) {
      observer.characterHitsCharacterFor(attacker, combatMsg, target, damage);
    }
  }

  /**
   * A character was knocked out.
   *
   * @param attacker
   *          Who performed the action
   * @param target
   *          Who was knocked out.
   */
  public void characterKOCharacter(Character attacker, Character target) {
    for (final IModelObserver observer : f_observers) {
      observer.characterWonFight(attacker, target);
    }
  }

  /**
   * A character is on a respawn countdown
   *
   * @param character
   *          The current character
   * @param ticksRemaining
   *          Ticks remaining to respawn
   */
  public void characterKOCountdown(Character character, int ticksRemaining) {
    for (final IModelObserver observer : f_observers) {
      observer.characterRespawnCountdown(character, ticksRemaining);
    }
  }

  /**
   * A character swung and missed at another character
   *
   * @param character
   *          The character performing the action
   * @param combatMsg
   *          The type of attack performed
   * @param target
   *          The target of the attack
   */
  public void characterMissedCharacter(Character character, String combatMsg, Character target) {
    for (final IModelObserver observer : f_observers) {
      observer.characterMissedCharacter(character, combatMsg, target);
    }
  }

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
  public void characterRespawned(Character character, Place previousLocation, Place newLocation) {
    for (final IModelObserver observer : f_observers) {
      observer.characterRespawned(character, previousLocation, newLocation);
    }
  }

  /**
   * A character transferred money to another character
   *
   * @param fromCharacter
   *          The character who lost money
   * @param toCharacter
   *          The character who gained money
   * @param money
   *          The amount of money transferred
   */
  public void characterTookMoney(Character fromCharacter, Character toCharacter, int money) {
    for (final IModelObserver observer : f_observers) {
      observer.characterTookMoney(fromCharacter, toCharacter, money, getMoneyName());
    }
  }

  /**
   * A character unequipped their weapon
   *
   * @param character
   *          The character
   */
  public void characterUnequippedWeapon(Character character) {
    for (final IModelObserver observer : f_observers) {
      observer.characterUnequippedWeapon(character);
    }
  }

  /**
   * A character woke up from unconsciousness
   *
   * @param character
   *          The character
   */
  public void characterWokeUp(Character character) {
    for (final IModelObserver observer : f_observers) {
      observer.characterWokeUp(character);
    }
  }

  /**
   * Create a character class
   *
   * @param name
   *          The name of the class
   * @param maxHealth
   *          The max health of the character
   * @param healthRegenPer5
   *          The amount of health to regen every 5 ticks
   * @param useableWeaponTypes
   *          The collection of usable weapon types
   * @param attackMsg
   *          The attack message to display when this class attacks
   * @param KORecovery
   *          The amount of time this class is unconscious
   * @param attackNumDice
   *          The default damage dice this class has
   * @param attackNumSides
   *          The default number of to the dice this class has
   * @param hitChance
   *          The default hit chance of this class
   * @param cooldown
   *          The cooldown rate of the default attack
   * @param target
   *          The targeting range / area of the default attack
   * @param onHitSound
   *          The onHit sound for the default attack
   * @param onMissSound
   *          The onMiss sound for the default attack
   * @param defaultWeaponName
   *          The default weapon name
   *
   * @return The new character class
   */
  public CharacterClass createCharacterClass(String name, int maxHealth, int healthRegenPer5,
      Collection<String> useableWeaponTypes, String attackMsg, int KORecovery, int attackNumDice, int attackNumSides,
      int hitChance, int cooldown, EffectTarget target, String onHitSound, String onMissSound, String defaultWeaponName) {
    final CharacterClass newClass = new CharacterClass(name, maxHealth, healthRegenPer5, useableWeaponTypes, attackMsg,
        KORecovery, attackNumDice, attackNumSides, hitChance, cooldown, target, onHitSound, onMissSound,
        defaultWeaponName);
    f_keyToCharacterClasses.put(name.toUpperCase(), newClass);
    return newClass;
  }

  /**
   * Factory constructor
   *
   * @param name
   *          The name of the Item, must be unique among all instances of Item
   * @param article
   *          The article for referring to this place if in English the only valid articles are a,
   *          an and the.
   * @param description
   *          The description of the item.
   * @param takePoints
   *          The number of points to award the player for picking up this item
   * @param dropPointsNowhere
   *          The number of points to award the player for dropping the item anywhere
   * @param dropPointsMap
   *          A Map of places and points that identify how many points to award the player for
   *          dropping an item
   * @param takeable
   *          A boolean that is true if this item can be picked up by a player
   * @param cantTake
   *          A message that will be displayed if the item cannot be picked up but such is attempted
   * @param blockMessages
   *          A map of places and strings to display to the player if they attempt to enter one of
   *          the listed places without
   * @param container
   *          The container to store items inside of this item. Set to NULL if this should not store
   *          other items.
   * @param type
   *          The weapon type
   * @param effect
   *          The effect this weapon can have
   * @param effectType
   *          The EffectType of this consumable
   * @param cooldown
   *          The weapon's cooldown speed in world ticks
   * @param uses
   *          The number of uses this consumable has. If negative then assumed to be infinite.
   * @param useString
   *          The message displayed when this weapon attacks, e.g. Your "fire", "blast", "slash",
   *          "etc" hits target for x damage.
   * @param hitChance
   *          The chance this weapon will hit on a 1-100 point scale
   * @param targets
   *          The area of effect of this weapon
   * @param onMissSound
   *          The string path to the sound file to play on missing with this consumable
   * @param onHitSound
   *          The string path to the sound file to play on hitting with this consumable
   * @return The consumable created
   */
  public Consumable createConsumable(String name, String article, String description, long takePoints,
      long dropPointsNowhere, Map<Place, Long> dropPointsMap, Boolean takeable, String cantTake,
      Map<Place, String> blockMessages, Container container, Effect effect, EffectType effectType, int cooldown,
      int uses, String type, String useString, int hitChance, EffectTarget targets, String onHitSound,
      String onMissSound) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    if (isNameUsed(name)) {
      throw new IllegalStateException("Construction of a new item named \"" + name
          + "\" failed because the specified name already exists");
    }
    final Consumable newItem = new Consumable(name, article, description, takePoints, dropPointsNowhere, dropPointsMap,
        takeable, cantTake, blockMessages, container, effect, effectType, cooldown, uses, type, useString, hitChance,
        targets, onHitSound, onMissSound);
    addItem(newItem);
    return newItem;
  }

  /**
   * Constructs a new Item within this world.
   *
   * @param name
   *          The name of the Item, must be unique among all instances of Item
   * @param article
   *          The article for referring to this place if in English the only valid articles are a,
   *          an and the.
   * @param description
   *          The description of the item
   * @param takePoints
   *          The number of points to award the player for picking up this item
   * @param dropPoints
   *          The number of points to award the player for dropping the item anywhere
   * @param dropPointsMap
   *          A Map of places and points that identify how many points to award the player for
   *          dropping an item
   * @param takeable
   *          A boolean that is true if this item can be picked up by a player
   * @param cantTakeMessage
   *          A message that will be displayed if the item cannot be picked up but such is attempted
   * @param blockedMessages
   *          A map of places and strings to display to the player if they attempt to enter one of
   *          the listed places without
   * @param storeItems
   *          A boolean that determines if this item can this item store other items.
   * @return the new {@link Item} instance.
   * @throws NullPointerException
   *           if any parameter is null
   * @throws IllegalStateException
   *           if the specified name already exists within this world.
   */
  public Item createItem(String name, String article, String description, Long takePoints, Long dropPoints,
      Map<Place, Long> dropPointsMap, Boolean takeable, String cantTakeMessage, Map<Place, String> blockedMessages,
      Boolean storeItems) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    if (article == null) {
      throw new NullPointerException("article cannot be null");
    }
    if (takePoints == null) {
      throw new NullPointerException("takePoints cannot be null");
    }
    if (dropPoints == null) {
      throw new NullPointerException("dropPoints cannot be null");
    }
    if (takeable == null) {
      throw new NullPointerException("takeable cannot be null");
    }
    if (cantTakeMessage == null) {
      throw new NullPointerException("cantTakeMessage cannot be null");
    }
    if (isNameUsed(name)) {
      throw new IllegalStateException("Construction of a new item named \"" + name
          + "\" failed because the specified name already exists");
    }
    final Item newItem = new Item(name, article, description, takePoints, dropPoints, dropPointsMap, takeable,
        cantTakeMessage, blockedMessages, storeItems ? new Container() : null);
    addItem(newItem);
    return newItem;
  }

  /**
   * Constructs a new NonPlayerCharacter within this world.
   *
   * @param name
   *          The name of the NonPlayerCharacter, must be unique among all instances of NPC
   * @param article
   *          The article for referring to this place if in English the only valid articles are a,
   *          an and the.
   * @param description
   *          A {@link String} description of the NPC
   * @param inventory
   *          The {@link Container} of any items the NPC is carrying
   * @param location
   *          A {@link Place} of the NPCs location
   * @param states
   *          A collection of all possible states this NPC could be in
   * @param currentState
   *          The current state the NPC is in
   * @param respawn
   *          The place where this NPC should respawn after death. Set to null for no respawn or the
   *          nowhereplace to make the corpse disappear.
   * @return the new {@link NonPlayerCharacter} instance.
   * @throws NullPointerException
   *           if any parameter is null
   * @throws IllegalStateException
   *           if the specified name already exists within this world.
   */
  public NonPlayerCharacter createNPC(String name, String article, String description, Container inventory,
      Place location, Set<State> states, State currentState, Place respawn) {
    if (isNameUsed(name)) {
      throw new IllegalStateException("Construction of a new item named \"" + name
          + "\" failed because the specified name already exists");
    }
    final NonPlayerCharacter newNPC = new NonPlayerCharacter(this, name, article, description, inventory, location,
        states, currentState, respawn);
    f_keyToNPCs.put(name.toUpperCase(), newNPC);
    f_keyToContainers.put(name.toUpperCase(), newNPC);
    f_articles.add(article);
    location.addCharacter(newNPC);
    return newNPC;
  }

  /**
   * Constructs a new place within this world.
   *
   * @param name
   *          a non-null unique name for the instance. The uniqueness of the name can't be dependent
   *          upon case, e.g., "Hall" is considered the same as "hall".
   * @param article
   *          the appropriate non-null indefinite article with which to prefix the name so as to
   *          form a proper short description, e.g., "the" or "a".
   * @param description
   *          a long, possibly mult-line, non-null description of this thing.
   * @param winCondition
   *          a non-null boolean that should be set to true if this place is a win condition when
   *          the player moves here
   * @param sound
   *          The path to the sound effect for this place
   * @return the new {@link Place} instance.
   * @throws NullPointerException
   *           if any parameter is null
   * @throws IllegalStateException
   *           if the specified name already exists within this world.
   */
  public Place createPlace(String name, String article, String description, Boolean winCondition, String sound)
      throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    if (article == null) {
      throw new NullPointerException("article cannot be null");
    }
    if (description == null) {
      throw new NullPointerException("description cannot be null");
    }
    if (winCondition == null) {
      throw new NullPointerException("winCondition cannot be null");
    }
    if (isNameUsed(name)) {
      throw new IllegalStateException("Construction of a new place named \"" + name
          + "\" failed because the specified name already exists");
    }
    final Place newPlace = new Place(this, name, article, description, winCondition, sound);
    f_keyToPlace.put(name.toUpperCase(), newPlace);
    f_keyToContainers.put(name.toUpperCase(), newPlace);
    f_articles.add(article);
    return newPlace;
  }

  /**
   * Constructs a new place within this world.
   *
   * @param name
   *          a non-null unique name for the instance. The uniqueness of the name can't be dependent
   *          upon case, e.g., "Hall" is considered the same as "hall".
   * @param place
   *          The place where the new player should be located
   * @param article
   *          The article to refer to this player. Can be empty "", but not null.
   * @param description
   *          The description of this player.
   * @param respawn
   *          The place where this player should respawn after death. Set to null for no respawn.
   * @return the new {@link Place} instance.
   * @throws NullPointerException
   *           if any parameter is null
   * @throws IllegalStateException
   *           if the specified name already exists within this world.
   */
  public Player createPlayer(String name, Place place, String article, String description, Place respawn)
      throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    if (place == null) {
      throw new NullPointerException("place cannot be null");
    }
    if (article == null) {
      throw new NullPointerException("article cannot be null");
    }
    if (description == null) {
      throw new NullPointerException("description cannot be null");
    }
    if (isNameUsed(name)) {
      throw new IllegalStateException("Construction of a new place named \"" + name
          + "\" failed because the specified name already exists");
    }
    final Player player = new Player(this, place, name, article, description, respawn);
    f_keyToPlayers.put(name.toUpperCase(), player);
    f_keyToContainers.put(name.toUpperCase(), player);
    f_articles.add(article);
    place.addCharacter(player);
    return player;
  }

  /**
   * Factory constructor
   *
   * @param name
   *          Weapon name
   * @param article
   *          "A", "AN", "THE" if necessary
   * @param description
   *          The description of the weapon
   * @param takePoints
   *          number of points to award for picking it up
   * @param dropPointsNowhere
   *          Number of points to award from dropping it
   * @param dropPointsMap
   *          Number of points to award for dropping in specific locations
   * @param blockMessages
   *          Places that cannot be entered without this weapon
   * @param container
   *          A container if this weapon functions as a storage device
   * @param type
   *          The weapon type
   * @param effect
   *          The effect this weapon can have
   * @param cooldown
   *          The weapon's cooldown speed in world ticks
   * @param useString
   *          The message displayed when this weapon attacks, e.g. Your "fire", "blast", "slash",
   *          "etc" hits target for x damage.
   * @param hitChance
   *          The chance this weapon will hit on a 1-100 point scale
   * @param targets
   *          The area of effect of this weapon
   * @param onMissSound
   *          The string path to the sound effect to play on missing with this weapon
   * @param onHitSound
   *          The string path to the sound effect to play on hitting with this weapon
   * @return The new Weapon
   */
  public Weapon createWeapon(String name, String article, String description, long takePoints, long dropPointsNowhere,
      Map<Place, Long> dropPointsMap, Map<Place, String> blockMessages, Container container, String type,
      Effect effect, int cooldown, String useString, int hitChance, EffectTarget targets, String onHitSound,
      String onMissSound) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    if (isNameUsed(name)) {
      throw new IllegalStateException("Construction of a new item named \"" + name
          + "\" failed because the specified name already exists");
    }
    final Weapon newItem = new Weapon(name, article, description, takePoints, dropPointsNowhere, dropPointsMap,
        blockMessages, container, type, effect, cooldown, useString, hitChance, targets, onHitSound, onMissSound);
    addItem(newItem);
    return newItem;
  }

  /**
   * Get a character in the world with the specified name
   *
   * @param characterName
   *          The name
   * @return The Character
   */
  public Character getCharacter(String characterName) {
    Character character = getNonPlayerCharacter(characterName);
    if (character == null) {
      character = getPlayer(characterName);
    }
    return character;
  }

  /**
   * Lookup a character class by name
   *
   * @param name
   *          The name of the class
   * @return The character class that was matched or null if none matched
   */
  public CharacterClass getCharacterClass(String name) {
    final String trimmedName = TextUtilities.trimArticles(name, getValidArticles());
    CharacterClass cClass = f_keyToCharacterClasses.get(name.toUpperCase());
    if (cClass == null) {
      cClass = f_keyToCharacterClasses.get(trimmedName.toUpperCase());
    }
    return cClass;
  }

  /**
   * Get the set of all character classes in this world
   *
   * @return A Set of CharacterClass
   */
  public Set<CharacterClass> getCharacterClasses() {
    return new HashSet<CharacterClass>(f_keyToCharacterClasses.values());
  }

  /**
   * Locate the container that matches this location string. Container could be inside of a place,
   * an item, an NPC's inventory, or a player's inventory.
   *
   * @param locationString
   *          the name in the form of a {@link String}
   * @return the Container matched or null if none were found
   */
  public Container getContainer(String locationString) {
    return getContainer(locationString, false);
  }

  private Container getContainer(String locationString, boolean trimmed) {
    locationString = locationString.toUpperCase();
    if (f_keyToPlace.containsKey(locationString)) {
      return f_keyToPlace.get(locationString).getContainer();
    } else if (f_keyToItem.containsKey(locationString)) {
      return f_keyToItem.get(locationString).getContainer();
    } else if (f_keyToNPCs.containsKey(locationString)) {
      return f_keyToNPCs.get(locationString).getContainer();
    } else if (f_keyToPlayers.containsKey(locationString)) {
      return f_keyToPlayers.get(locationString).getContainer();
    } else if (!trimmed) {
      return getContainer(TextUtilities.trimArticles(locationString, getValidArticles()), true);
    } else {
      return null;
    }
  }

  /**
   * Access all of the valid crafting combinations in this world.
   *
   * @return A Set of Map.Entry that contains a Set of Items required and the new Item.
   */
  public Set<Entry<Set<Item>, Item>> getCraftingObjects() {
    return f_itemCombinations.entrySet();
  }

  /**
   * Get the number of ticks since the world started
   *
   * @return The current tick of the world
   */
  public long getCurrentTick() {
    return f_currentTick;
  }

  /**
   * Gets the appropriate {@link Item} instance with the specified name.
   *
   * @param name
   *          the non-null non-case sensitive name of the desired {@link Item} instance.
   * @return the appropriate {@link Item} instance, or <code>null</code> if the specified name does
   *         not exist.
   * @throws NullPointerException
   *           if name is null
   */
  public Item getItem(String name) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    Item item = f_keyToItem.get(name.toUpperCase());
    if (item == null) {
      item = f_keyToItem.get(TextUtilities.trimArticles(name, getValidArticles()).toUpperCase());
    }
    return item;
  }

  /**
   * Returns a copy of all the Places in this world.
   *
   * @return a copy of the set of all Places in this world.
   */
  public Set<Item> getItems() {
    return new HashSet<Item>(f_keyToItem.values());
  }

  /**
   * Get the description of money in this world
   *
   * @return A string, e.g. "gold pieces" or "dollars".
   */
  public String getMoneyName() {
    return f_moneyName;
  }

  /**
   * Gets the appropriate {@link NonPlayerCharacter} instance with the specified name.
   *
   * @param name
   *          the non-null non-case sensitive name of the desired {@link NonPlayerCharacter}
   *          instance.
   * @return the appropriate {@link NonPlayerCharacter} instance, or <code>null</code> if the
   *         specified name does not exist.
   * @throws NullPointerException
   *           if name is null
   */
  public NonPlayerCharacter getNonPlayerCharacter(String name) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    NonPlayerCharacter npc = f_keyToNPCs.get(name.toUpperCase());
    if (npc == null) {
      npc = f_keyToNPCs.get(TextUtilities.trimArticles(name.toUpperCase(), getValidArticles()));
    }
    return npc;
  }

  /**
   * Returns a copy of all the NPCs in this world.
   *
   * @return a copy of the set of all NPCs in this world.
   */
  public Collection<NonPlayerCharacter> getNonPlayerCharacters() {
    return f_keyToNPCs.values();

  }

  /**
   * Gets the {@link Place} representing nowhere. This place always exists in every world.
   *
   * @return the place representing nowhere.
   */
  public Place getNowherePlace() {
    return f_nowhere;
  }

  /**
   * Gets a copy of the set of all observers of this world.
   *
   * @return a copy of the set of all observers of this world.
   */
  public Set<IModelObserver> getObservers() {
    return new HashSet<IModelObserver>(f_observers); // defensive copy
  }

  /**
   * Gets the appropriate {@link Place} instance with the specified name.
   *
   * @param name
   *          the non-null non-case sensitive name of the desired {@link Place} instance.
   * @return the appropriate {@link Place} instance, or <code>null</code> if the specified name does
   *         not exist.
   * @throws NullPointerException
   *           if name is null
   */
  public Place getPlace(String name) throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    Place place = f_keyToPlace.get(name.toUpperCase());
    if (place == null) {
      place = f_keyToPlace.get(TextUtilities.trimArticles(name, getValidArticles()).toUpperCase());
    }
    return place;
  }

  /**
   * @deprecated Use {@link Universe#getPlace(String)}
   * @since 1.1
   *
   *        Gets the appropriate {@link Place} instance with the specified name.
   *
   * @param name
   *          the non-null non-case sensitive name of the desired {@link Place} instance.
   * @return the appropriate {@link Place} instance, or <code>null</code> if the specified name does
   *         not exist.
   */
  @Deprecated
  public Place getPlaceByName(String name) {
    return getPlace(name);
  }

  /**
   * Returns a copy of all the Places in this world.
   *
   * @return a copy of the set of all Places in this world.
   */
  public Set<Place> getPlaces() {
    return new HashSet<Place>(f_keyToPlace.values());
  }

  /**
   * Gets a reference to a player interacting with this world.
   *
   * @param name
   *          The unique name of the player you want to access
   *
   * @return the sole player within this world.
   */
  public Player getPlayer(String name) {
    Player player = f_keyToPlayers.get(name.toUpperCase());
    if (player == null) {
      player = f_keyToPlayers.get(TextUtilities.trimArticles(name, getValidArticles()).toUpperCase());
    }
    return player;
  }

  /**
   * Access the list of players in the game
   *
   * @return A Collection of Player
   */
  public Player[] getPlayers() {
    return f_keyToPlayers.values().toArray(new Player[0]);
  }

  /**
   * Get the tick rate for this world
   *
   * @return The tick rate in milliseconds
   */
  public long getTickRate() {
    return f_tickRate;
  }

  /**
   * Get all the valid articles of this world
   *
   * @return A set of string articles
   */
  public Set<String> getValidArticles() {
    return f_articles;
  }

  /**
   * Does the world allow combat?
   *
   * @return true if so
   */
  public boolean isCombatAllowed() {
    return f_allowCombat;
  }

  /**
   * Check if any player has won the game
   *
   * @return true if any player won
   */
  public boolean isGameWon() {
    return f_gameWon;
  }

  /**
   * Returns <code>true</code> if the specified name is used for a {@link Place} within this world,
   * <code>false</code> otherwise. Names are non-case sensitive, so "NAME" is considered the same
   * name as "nAmE".
   *
   * @param name
   *          the non-null non-case sensitive name to check.
   * @return <code>true</code> if the specified name is used for a {@link Place} within this world,
   *         <code>false</code> otherwise.
   * @throws NullPointerException
   *           if name is null
   */
  public boolean isNameUsed(String name) throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    return f_keyToContainers.containsKey(name.toUpperCase());
  }

  /**
   * A player gained or lost money
   *
   * @param player
   *          The current player
   * @param money
   *          The amount of money gained or lost (will be {&lt;0} if lost).
   */
  public void moneyChanged(Character player, int money) {
    for (final IModelObserver observer : f_observers) {
      observer.characterMoneyChanged(player, money, getMoneyName());
    }
  }

  /**
   * Notify all observers of an exception in the game
   *
   * @param string
   *          The string to display
   */
  public void notifyException(String string) {
    for (final IModelObserver observer : f_observers) {
      observer.broadcastMessage(new Player(this, getNowherePlace(), "WORLD", "THE", "", null), "THE WORLD", string);
    }
  }

  /**
   * A player was given an item by an NPC
   *
   * @param player
   *          The current player
   * @param f_itemName
   *          The item gained
   */
  public void playerGainsItem(Character player, Item f_itemName) {
    for (final IModelObserver observer : f_observers) {
      observer.playerGainsItem(player, f_itemName);
    }
  }

  /**
   * A player joined the game
   *
   * @param player
   *          The player who joined
   */
  public void playerJoined(Player player) {
    for (final IModelObserver observer : f_observers) {
      observer.playerJoined(player);
    }
  }

  /**
   * A player lost an item because an NPC took it
   *
   * @param player
   *          The current player
   * @param f_itemName
   *          The item lost
   */
  public void playerLosesItem(Character player, Item f_itemName) {
    for (final IModelObserver observer : f_observers) {
      observer.playerLosesItem(player, f_itemName);
    }
  }

  /**
   * Notify all observers that a player has moved to a new location
   *
   * @param player
   *          The current player
   * @param startLocation
   *          The starting Place
   * @param endLocation
   *          The ending Place
   */
  public void playerMoved(Player player, Place startLocation, Place endLocation) {
    for (final IModelObserver observer : f_observers) {
      observer.playerMoved(player, startLocation, endLocation);
    }
  }

  /**
   * A player quit the game
   *
   * @param player
   *          The player who quit
   */
  public void playerQuit(Player player) {
    for (final IModelObserver observer : f_observers) {
      observer.playerQuit(player);
    }
  }

  /**
   * A player took an item from the ground
   *
   * @param player
   *          The current player
   * @param item
   *          The item taken
   * @param takePoints
   *          The amount of points the player gained
   */
  public void playerTookItem(Player player, Item item, long takePoints) {
    for (final IModelObserver observer : f_observers) {
      observer.playerTookItemFromGround(player, item, takePoints);
    }
  }

  /**
   * Play a sound file to everyone in the region of cause
   *
   * @param cause
   *          The character who triggered the sound
   * @param soundPath
   *          The path to the sound file
   */
  public void playSound(Character cause, String soundPath) {
    for (final IModelObserver observer : f_observers) {
      observer.playSound(cause, soundPath);
    }
  }

  /**
   * Notify all observers that a player has stored an item inside of another item.
   *
   * @param player
   *          The current player
   * @param itemMoved
   *          The item that was stored
   * @param itemContainer
   *          The container item that itemMoved was stored inside
   */
  public void putItemInItem(Player player, Item itemMoved, Item itemContainer) {
    for (final IModelObserver observer : f_observers) {
      observer.playerPutItemInItem(player, itemMoved, itemContainer);
    }
  }

  /**
   * Removes an observer from this world. Has no effect if the specified observer was not previously
   * added as an observer.
   *
   * @param observer
   *          the object to stop notifying of changes to this world.
   * @throws NullPointerException
   *           if observer is null
   */
  public void removeObserver(IModelObserver observer) throws NullPointerException {
    if (observer == null) {
      throw new NullPointerException("observer cannot be null");
    }
    f_observers.remove(observer);
  }

  /**
   * Enable or disable combat in the world
   *
   * @param allowCombat
   *          true if combat is allowed
   */
  public void setAllowCombat(boolean allowCombat) {
    f_allowCombat = allowCombat;
  }

  /**
   * Notifies this world that the game is over.
   *
   * @param win
   *          set to <code>true</code> if the user won the game. False if they quit or lost.
   */
  public void setGameOver(Boolean win) {
    if (win == null) {
      throw new NullPointerException("win parameter cannot be null");
    }
    if (win) {
      f_gameWon = true;
    }
    for (final IModelObserver observer : f_observers) {
      observer.gameOver(this);
    }
  }

  /**
   * Set the description of money in this world.
   *
   * @param moneyName
   *          A string, e.g. "gold pieces" or "dollars".
   */
  public void setMoneyName(String moneyName) {
    f_moneyName = moneyName;
  }

  /**
   * The rate at which ticks occur in milliseconds
   *
   * @param tickRate
   *          the tick rate long in milliseconds
   */
  public void setTickRate(long tickRate) {
    f_tickRate = tickRate;
  }

  /**
   * Combine all of the items and see if a new item is created
   *
   * @param items
   *          The items to combine
   * @return the new item
   */
  public Item synthesizeItems(Set<Item> items) {
    return f_itemCombinations.remove(items);
  }

  /**
   * Notify all observers that a player has retrieved an item from inside of another item
   *
   * @param player
   *          The current player
   * @param itemMoved
   *          The item that was retrieved
   * @param itemContainer
   *          The container item that itemMoved was retrieved from
   */
  public void takeItemfromItem(Player player, Item itemMoved, Item itemContainer) {
    for (final IModelObserver observer : f_observers) {
      observer.takeitemFromItem(player, itemMoved, itemContainer);
    }
  }

  /**
   * A player is talking to an NPC
   *
   * @param player
   *          The current player
   * @param npc
   *          The NPC being talked to
   * @param currentEvents
   *          The list of things the player can say to the NPC
   */
  public void talkToNPC(Player player, NonPlayerCharacter npc, List<SayTrigger> currentEvents) {
    for (final IModelObserver observer : f_observers) {
      observer.playerTalkedToNPC(player, npc, currentEvents);
    }
  }

  /**
   * Push the world forward in time
   *
   * @param numTicks
   *          The number of ticks since the world started
   */
  public void tick(long numTicks) {
    f_currentTick = numTicks;
    for (final Character character : getNonPlayerCharacters()) {
      character.tickAction(numTicks);
    }
    for (final Character character : getPlayers()) {
      character.tickAction(numTicks);
    }
  }

  /**
   * A new world was loaded
   *
   * @param fileName
   *          The filename of the world.xml file
   */
  public void worldLoaded(String fileName) {
    for (final IModelObserver observer : f_observers) {
      observer.worldLoaded(this, fileName);
    }
  }
}
