package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.Consumable.EffectType;
import pavlik.john.dungeoncrawl.model.events.State;
import pavlik.john.dungeoncrawl.model.events.Trigger;
import pavlik.john.dungeoncrawl.model.events.triggers.AttackedTrigger;
import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.TextUtilities;

/**
 * A character within the game, could be a player or an NPC or something else.
 *
 * @author John Pavlik
 *
 * @version 1.3
 * @since 1.3
 */
public abstract class Character implements HasContainer, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  /**
   *
   */
  private final Universe       f_world;
  /**
   * The place where the Character currently is.
   */
  private Place             f_location;

  /**
   * The character's inventory
   */
  private final Container   f_inventory;

  /**
   * The character's name
   */
  private final String      f_name;
  /**
   * The character's article
   */
  private final String      f_article;
  /**
   * The character's description
   */
  private final String      f_description;

  /**
   * Amount of money the character has
   */
  private int               f_money          = 0;
  private CharacterClass    f_class;

  private boolean           f_inCombat       = false;
  private Weapon            f_currentWeapon;
  private Character         f_currentTarget;
  protected int             f_currentHealth;
  private boolean           f_isConscious    = true;
  private int               unconsciousTime;
  private final Place       f_respawnLocation;

  /**
   * Constructs a new character.
   *
   * @param world
   *          The current world this character resides in
   * @param name
   *          The character's unique name
   * @param article
   *          The character's article, usually "A", "AN", or "THE"
   * @param description
   *          The description of this character
   * @param inventory
   *          The items this character is carrying
   * @param location
   *          The location of this character within a Universe.
   * @param respawn
   *          The place where this character will respawn after being killed in combat, set to null
   *          for permadeath and the nowhere place to make the corpse disappear after death.
   *
   * @throws NullPointerException
   *           if any parameter is null
   */
  Character(Universe world, String name, String article, String description, Container inventory, Place location,
      Place respawn) {
    f_name = Objects.requireNonNull(name, "Character name cannot be null");
    f_article = Objects.requireNonNull(article, f_name + "'s character article cannot be null");
    f_description = Objects.requireNonNull(description, f_name + "'s character description cannot be null");
    f_inventory = Objects.requireNonNull(inventory, f_name + "'s character inventory cannot be null");
    f_location = Objects.requireNonNull(location, f_name + "'s character location cannot be null");
    f_respawnLocation = respawn;
    f_world = Objects.requireNonNull(world, f_name + "'s world cannot be null");
  }

  /**
   * Protected in order to allow NPCs to override this method to execute onHealth triggers.
   */
  protected void changeHealth(Character cause, int change) {
    if (change + getCurrentHealth() > f_class.getMaxHealth()) {
      change = f_class.getMaxHealth() - getCurrentHealth();
    }
    if (change > 0) {
      f_world.characterHeal(this, change);
    }
    f_currentHealth += change;
    if (f_currentHealth <= 0) {
      f_currentHealth = 0;
      setUnconscious(cause);
    }
  }

  /**
   * Changes the character's amount of money
   *
   * @param coins
   *          The {@link Integer} amount of money to give to or take (if negative) from the
   *          character.
   */
  public void changeMoney(int coins) {
    f_money += coins;
  }

  /**
   * Clear the combat flags and current target
   */
  public void disengageCombat() {
    f_inCombat = false;
    f_currentTarget = null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Character) {
      return f_name.equals(((Character) obj).f_name);
    }
    return super.equals(obj);
  }

  /**
   * Gets the article of the character. This refers to the appropriate indefinite article with which
   * to prefix the name so as to form a proper short description, e.g., "the Player".
   *
   * @return a string containing the article of the character.
   */
  public String getArticle() {
    return f_article;
  }

  /**
   * Get the character class for this character
   *
   * @return A CharacterClass
   */
  public CharacterClass getCharacterClass() {
    return f_class;
  }

  private Collection<Character> getCharactersTargeting(Character target) {
    final Set<Character> targetList = new HashSet<>();
    for (final Character character : getLocation().getAllCharacters()) {
      final Character nextTarget = character.getCurrentTarget();
      if (nextTarget != null && nextTarget.equals(target)) {
        targetList.add(character);
      }
    }
    return targetList;
  }

  /**
   * Access the character's inventory
   *
   * @return the character's inventory
   * @since 1.2
   */
  @Override
  public Container getContainer() {
    return f_inventory;
  }

  /**
   * Get the character's current health
   *
   * @return an int value of the health ranging from 0 to class.max_health
   */
  public int getCurrentHealth() {
    return f_currentHealth;
  }

  /**
   * Get this characters current target
   *
   * @return the character being targeted
   */
  public Character getCurrentTarget() {
    return f_currentTarget;
  }

  /**
   * Get this character's currently equipped weapon
   *
   * @return The current weapon
   */
  public Weapon getCurrentWeapon() {
    if (f_currentWeapon == null && getCharacterClass() != null) {
      return getCharacterClass().getDefaultWeapon();
    }
    return f_currentWeapon;
  }

  private String getCurrentWeaponDescription(ResourceBundle messages) {
    String equippedWeaponMessage = "";
    final Item currentWeapon = getCurrentWeapon();
    if (currentWeapon != null) {
      equippedWeaponMessage = messages.getString(Messages.CURRENT_EQUIPPED).replace(Messages.ITEM_TAG,
          currentWeapon.toString()).replace(Messages.PLAYER_TAG, toString());
    }
    return equippedWeaponMessage;
  }

  /**
   * Get the description field for this character
   *
   * @return the short description
   */
  public String getDescription() {
    return f_description;
  }

  private Collection<Character> getEffectTargets(Consumable item, Character targetCharacter) {
    final Consumable.EffectTarget effectTargets = item.getEffectTarget();
    final Set<Character> targetList = new HashSet<>();
    switch (effectTargets) {
      case SINGLE:
        targetList.add(targetCharacter);
        break;
      case SELF:
        targetList.add(this);
        break;
      case GROUP:
        targetList.add(targetCharacter);
        boolean done = false;
        while (!done) {
          done = true;
          final Character[] currentList = targetList.toArray(new Character[0]);
          for (final Character target : currentList) {
            if (targetList.addAll(getCharactersTargeting(target))) {
              done = false;
            }
            if (target.getCurrentTarget() != null && targetList.add(target.getCurrentTarget())) {
              done = false;
            }
          }
        }

        if (item.getEffectType().equals(EffectType.DAMAGE)) {
          targetList.remove(this);
        }
        break;
      case PLACE:
        targetList.addAll(getLocation().getAllCharacters());
        if (item.getEffectType().equals(EffectType.DAMAGE)) {
          targetList.remove(this);
        }
    }
    return targetList;
  }

  /**
   * Gets the long description of the character, including health and class.
   *
   * @param messages
   *          The resourcebundle of strings
   *
   * @return the long description for the character.
   */
  public String getFullDescription(ResourceBundle messages) {
    return toString() + ": "
        + messages.getString(Messages.HEALTH_PROMPT).replace(Messages.HEALTH_TAG, Integer.toString(f_currentHealth))
        + TextUtilities.LINESEP + getDescription() + TextUtilities.LINESEP + getCurrentWeaponDescription(messages);
  }

  /**
   * Get the inventory listing for this character
   *
   * @param messages
   *          The ResourceBundle to build the strings to display from
   * @return A string ready to display to the user of this character's inventory
   */
  public String getInventory(ResourceBundle messages) {
    final String inventory = TextUtilities.describeItems(getContainer().getItems(), messages);
    final int money = getMoney();
    if (inventory.length() > 0 || money > 0) {
      final String moneyMessage = messages.getString(Messages.MONEY).replace(Messages.MONEY_TAG,
          Integer.toString(money)).replace(Messages.XML_MONEY_NAME, f_world.getMoneyName());
      final String inventoryMessage = messages.getString(Messages.INVENTORY_NOTEMPTY).replace(Messages.ITEM_TAG,
          inventory);
      final String equippedWeaponMessage = getCurrentWeaponDescription(messages);
      return (moneyMessage + (inventory.length() > 0 ? (TextUtilities.LINESEP + inventoryMessage) : "")
          + (equippedWeaponMessage.length() > 0 ? TextUtilities.LINESEP : "") + equippedWeaponMessage);
    } else {
      return (messages.getString(Messages.INVENTORY_EMPTY));
    }
  }

  /**
   *
   * @return A {@link Place} representing the characters current location
   */
  public Place getLocation() {
    return f_location;
  }

  /**
   * Check the character's wallet
   *
   * @return the {@link Integer} amount of Money the character has
   */
  public int getMoney() {
    return f_money;
  }

  /**
   * Gets the unique identifier of the character.
   *
   * @return a string containing the name of the character.
   */
  public String getName() {
    return f_name;
  }

  /**
   * Get the respawn location for this character
   *
   * @return The place where this character should respawn after being killed
   */
  public Place getRespawnLocation() {
    return f_respawnLocation;
  }

  /**
   * Gets the short description, e.g., "the Grand Hall", for this.
   *
   * @return the short description for this.
   */
  public String getShortDescription() {
    // Only includes the space if the article isn't empty
    return getArticle() + (getArticle().length() > 0 ? " " : "") + getName();
  }

  @Override
  public int hashCode() {
    return f_name.hashCode();
  }

  /**
   * Check if this character is in combat
   *
   * @return true if in combat
   */
  public boolean inCombat() {
    return f_inCombat;
  }

  /**
   * Check if this character is conscious
   *
   * @return true if conscious
   */
  public boolean isConscious() {
    return f_isConscious;
  }

  /**
   * Simple utility check to see if another player is in the same place as this player.
   *
   * @param character
   *          The other player
   * @return true if both players are in the same location
   */
  public boolean sameLocationAs(Character character) {
    return getLocation().equals(character.getLocation());
  }

  /**
   * Set this character's class and initialize their current health
   *
   * @param characterClass
   *          The class to setup
   */
  public void setCharacterClass(CharacterClass characterClass) {
    f_class = new CharacterClass(characterClass);
    f_currentHealth = f_class.getMaxHealth();
  }

  private void setConscious() {
    f_isConscious = true;
    if (getRespawnLocation() != null) {
      final Place previousLocation = getLocation();
      setLocation(getRespawnLocation());
      f_world.characterRespawned(this, previousLocation, getLocation());
    }
    f_world.characterWokeUp(this);
  }

  private void setCurrentTarget(Character target) {
    if (this != target) {
      f_currentTarget = target;
    }
  }

  /**
   * Set the current weapon for the user. Can be null if no weapon is equipped.
   *
   * @param weapon
   *          the current weapon item for the user.
   */
  public void setCurrentWeapon(Weapon weapon) {
    f_currentWeapon = weapon;
    f_world.characterEquippedWeapon(this, weapon);
  }

  private void setInCombat(boolean b) {
    f_inCombat = b;
  }

  /**
   * Used to update this character's current location
   *
   * @param place
   *          A {@link Place} that represents the character's new location.
   * @throws NullPointerException
   *           if place is null
   */
  public void setLocation(Place place) throws NullPointerException {
    if (place == null) {
      throw new NullPointerException("place cannot be null");
    }
    f_location.removeCharacter(this);
    place.addCharacter(this);
    f_location = place;
  }

  /**
   * Set this character unconscious, drop all their items, and give all their money to the
   * causeCharacter
   *
   * @param causeCharacter
   *          The character who hit the final blow
   */
  public void setUnconscious(Character causeCharacter) {
    f_isConscious = false;

    final LinkedList<String> itemNames = new LinkedList<String>();
    final Item[] items = getContainer().getItems().toArray(new Item[0]);
    for (final Item item : items) {
      itemNames.add(item.getName());
      getContainer().moveItem(getLocation().getContainer(), item);
      f_world.characterDroppedItem(this, item, 0);
    }
    f_currentWeapon = null;
    final int money = getMoney();
    if (money > 0) {
      causeCharacter.changeMoney(money);
      changeMoney(-money);
      f_world.characterTookMoney(this, causeCharacter, money);
    }
    f_world.characterKOCharacter(getCurrentTarget(), this);

    causeCharacter.disengageCombat();
  }

  /**
   * Start attacking the specified target
   *
   * @param target
   *          The Character to attack
   */
  public void startAttack(Character target) {
    setCurrentTarget(target);
    setInCombat(true);
  }

  /**
   * Notify the character that time has passed in the world
   *
   * @param numTicks
   *          The number of ticks that have passed since the world was started
   */
  public void tickAction(long numTicks) {
    if (inCombat() && getCurrentWeapon() != null && getCurrentWeapon().canUseItem(numTicks)) {
      useItem(getCurrentWeapon(), getCurrentTarget());
    }
    if (isConscious() && (numTicks % 15) == 0 && getCharacterClass() != null
        && getCurrentHealth() < getCharacterClass().getMaxHealth()) {
      changeHealth(this, getCharacterClass().getHealthRegen());
    }
    if (!isConscious() && f_class.getKORecovery() >= 0) {
      unconsciousTime++;
      if (unconsciousTime >= f_class.getKORecovery()) {
        unconsciousTime = 0;
        setConscious();
      } else if (f_class.getKORecovery() >= 0) {
        f_world.characterKOCountdown(this, f_class.getKORecovery() - unconsciousTime);
      }
    }
  }

  @Override
  public String toString() {
    return getShortDescription();
  }

  /**
   * Unequip the characters weapon
   */
  public void unequip() {
    f_currentWeapon = null;
    f_world.characterUnequippedWeapon(this);
  }

  /**
   * Allow a character to use the specified item on the target character
   *
   * @param item
   *          The Consumable or Weapon to be used
   * @param initialTarget
   *          The character being targeted
   */
  public void useItem(Consumable item, Character initialTarget) {
    if (isConscious() && initialTarget != null && item != null
        && (item.getEffectType() == EffectType.HEALING || initialTarget.isConscious())
        && item.canUseItem(f_world.getCurrentTick()) && sameLocationAs(initialTarget)) {
      final Collection<Character> targetList = getEffectTargets(item, initialTarget);
      final Character[] targetArray = targetList.toArray(new Character[0]);
      for (final Character target : targetArray) {
        if (target.getLocation() == f_location && target.getCharacterClass() != null) {
          if (item.hitTarget(target)) {
            final int effect = item.getEffect();
            switch (item.getEffectType()) {
              case DAMAGE:
                if (target.isConscious()) {
                  target.setInCombat(true);
                  if (!(target instanceof Player) || target.getCurrentTarget() == null) {
                    target.setCurrentTarget(this);
                  }
                  f_world.characterHitsCharacterFor(this, item.getUseString(), target, effect);
                  if (target instanceof NonPlayerCharacter) {
                    final State currentState = ((NonPlayerCharacter) target).getCurrentState();
                    for (final Trigger trigger : currentState.getEventTriggers()) {
                      if (trigger instanceof AttackedTrigger) {
                        trigger.execute(this);
                      }
                    }
                  }
                  target.changeHealth(this, -effect);
                }
                break;
              case HEALING:
                target.changeHealth(this, effect);
                break;
            }

          } else {

            switch (item.getEffectType()) {
              case DAMAGE:
                if (!target.inCombat()) {
                  target.setInCombat(true);
                  target.setCurrentTarget(this);
                  if (target instanceof NonPlayerCharacter) {
                    final State currentState = ((NonPlayerCharacter) target).getCurrentState();
                    for (final Trigger trigger : currentState.getEventTriggers()) {
                      if (trigger instanceof AttackedTrigger) {
                        trigger.execute(this);
                      }
                    }
                  }
                }
                break;
              case HEALING:
                break;
            }
            f_world.characterMissedCharacter(this, item.getUseString(), target);
          }
        }
      }
      item.setLastUseTick(f_world.getCurrentTick());
    } else {
      disengageCombat();
    }
  }
}
