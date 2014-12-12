package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.TextUtilities;

/**
 *
 * @author John Pavlik
 *
 * @version 1.2
 */
public class Place implements Comparable<Place>, HasContainer, Serializable {

  /**
   *
   */
  private static final long            serialVersionUID           = 1L;

  /**
   * The immutable reference to the {@link Universe} instance this is contained within.
   */
  private final Universe                  f_world;

  /**
   * The immutable name of this thing, e.g., "Grand Hall" or "Rusty Key". This identifier uniquely
   * identifies this instance within the game.
   */
  private final String                 f_name;

  /**
   * The immutable indefinite article with which to prefix the name so as to form a proper short
   * description, e.g., "the" or "a".
   */
  private final String                 f_article;

  /**
   * A mapping of directions of travel to the neighboring places. The map is ragged in the sense
   * that if a direction is not legal to travel in a <code>null</code> will be returned. For
   * example, <code>null == f_directionOfTravelToplace.get(Navigation.NORTH)</code> will be
   * <code>true</code> if no place exists to the north of this one.
   */
  private final Map<Navigation, Place> f_directionOfTravelToPlace = new HashMap<Navigation, Place>();

  /**
   * The container of items stored at this location
   */
  private final Container              f_itemsHere;

  /**
   * All of the Characters here
   */
  private final Map<String, Character> f_charactersHere;

  /**
   * An immutable long description of this thing.
   */
  protected String                     f_description;

  /**
   * An immutable boolean that defines whether this place allows the player to win
   */
  protected Boolean                    f_winCondition;

  /**
   * The items required to enter this location
   */
  private final Set<Item>              f_itemsRequiredForEntry;

  private final String                 f_sound;

  /**
   * Constructs a new Place instance. Invoked by the {@link Universe} class. It has been overloaded to
   * work with old createPlace calls that do not use the arrivalWinsGame field. It is set to false
   * by default.
   *
   * @param world
   *          the {@link Universe} instance this exists within.
   * @param name
   *          a non-null unique name for the instance. The uniqueness of the name can't be dependent
   *          upon case, e.g., "Hall" is considered the same as "hall".
   * @param article
   *          the appropriate non-null indefinite article with which to prefix the name so as to
   *          form a proper short description, e.g., "the" or "a".
   * @param description
   *          a long, possibly mult-line, non-null description of this thing.
   * @param winCondition
   *          a Boolean that defines if this place allows the player to win.
   *
   * @since 1.1 updated to require a winCondition in order to instantiate
   * @throws NullPointerException
   *           if any parameter is null
   */
  Place(Universe world, String name, String article, String description, Boolean winCondition, String sound)
      throws NullPointerException {
    f_name = Objects.requireNonNull(name, "Place name cannot be null");
    f_world = Objects.requireNonNull(world, "Place " + name + "'s Universe cannot be null");
    f_article = Objects.requireNonNull(article, "Place " + name + "'s Article cannot be null");
    f_description = Objects.requireNonNull(description, "Place " + name + "'s Description cannot be null");
    f_winCondition = Objects.requireNonNull(winCondition, "Place " + name + "'s winCondition cannot be null");
    f_itemsRequiredForEntry = new HashSet<>();
    f_charactersHere = new HashMap<>();
    f_itemsHere = new Container();
    f_sound = sound;
  }

  /**
   * Add a new Character to this location
   *
   * @param character
   *          the {@link Character} to move here
   */
  public void addCharacter(Character character) {
    f_charactersHere.put(character.getName().toUpperCase(), character);
  }

  /**
   * Adds an item required to enter this location
   *
   * @param item
   *          The item required to enter here
   */
  public void addItemRequired(Item item) {
    if (item == null) {
      throw new NullPointerException("item cannot be null");
    }
    f_itemsRequiredForEntry.add(item);
  }

  /**
   * Compares this two places together, ordering them by
   * <code>this.getName().compareTo(otherPlace.getName())</code>;
   */
  @Override
  public int compareTo(Place o) {
    return getName().compareTo(o.getName());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Place) {
      return f_name.equals(((Place) obj).f_name);
    } else {
      return super.equals(obj);
    }
  }

  /**
   * Get all of the Characters at this place
   *
   * @return a Collection of NonPlayerCharacters at this place
   */
  public Collection<Character> getAllCharacters() {
    return f_charactersHere.values();
  }

  /**
   * Gets the article of this. This refers to the appropriate indefinite article with which to
   * prefix the name so as to form a proper short description, e.g., "the" or "a".
   *
   * @return a string contaning the article of this.
   */
  public final String getArticle() {
    return f_article;
  }

  /**
   * Get the Character at this location identified by CharacterName
   *
   * @param characterName
   *          a {@link String} of the Character's name
   * @return a {@link NonPlayerCharacter}
   */
  public Character getCharacter(String characterName) {
    Character Character = f_charactersHere.get(characterName.toUpperCase());
    if (Character == null) {
      Character = f_charactersHere.get(TextUtilities.trimArticles(characterName.toUpperCase(), f_world
          .getValidArticles()));
    }
    return Character;
  }

  /**
   * Access all of the items located here
   *
   * @return the Container for that contains everything located in this place
   */
  @Override
  public Container getContainer() {
    return f_itemsHere;
  }

  /**
   * Gets the long description of this.
   *
   * @return the long description for this.
   */
  public final String getDescription() {
    return f_description;
  }

  /**
   * Gets several lines of directions about the possible travel destinations from a place. These are
   * in the form "To the north you see the Hall."
   *
   * @param messages
   *          the ResourceBundle containing all of the messages for the MainConsole game
   * @return directions and travel destinations from this place.
   * @throws NullPointerException
   *           if place is null
   */
  public String getDirectionsFrom(ResourceBundle messages) throws NullPointerException {
    final StringBuilder result = new StringBuilder();
    for (final Navigation possibleDirection : Navigation.values()) {
      if (isTravelAllowedToward(possibleDirection)) {
        result.append(messages.getString(Messages.TRAVEL_DIRECTION).replace(Messages.DIRECTION_TAG,
            possibleDirection.toString().toLowerCase()).replace(Messages.PLACE_TAG,
            getTravelDestinationToward(possibleDirection).getShortDescription()));
      }
    }
    return result.toString().trim();
  }

  /**
   * Gets a full description of the specified place, including possible destinations for travel from
   * there.
   *
   * @param player
   *          The current player
   *
   * @param messages
   *          the ResourceBundle containing all of the messages for the MainConsole game
   * @return a String that consists of the short description modified to capitalize the first
   *         character, with a period and the regular description appended, followed by 2 line
   *         breaks and the possible travel locations from here.
   * @throws NullPointerException
   *           if place is null
   */
  public String getFullLocationDescription(Player player, ResourceBundle messages) throws NullPointerException {
    final StringBuilder msg = new StringBuilder(getShortDescription());
    // Capitalize the first letter
    msg.replace(0, 1, msg.substring(0, 1).toUpperCase());
    msg.append(". ");
    msg.append(getDescription());
    // msg.append(". ");
    /*
     * Display items present at this location
     */
    if (!getContainer().isEmpty()) {
      msg.append(TextUtilities.LINESEP);
      msg.append(messages.getString(Messages.SEE_ITEM).replace(Messages.ITEM_TAG,
          TextUtilities.describeItems(getContainer().getItems(), messages)));
    }
    if (!f_charactersHere.isEmpty()) {
      final Collection<Character> charactersHereCopy = new HashSet<>(f_charactersHere.values());
      charactersHereCopy.remove(player);
      if (!charactersHereCopy.isEmpty()) {
        msg.append(TextUtilities.LINESEP);
        msg.append(messages.getString(Messages.SEE_NPC).replace(Messages.NPC_TAG,
            TextUtilities.describeItems(charactersHereCopy, messages)));
      }
    }
    if (!f_winCondition) {
      msg.append(TextUtilities.LINESEP2);
      msg.append(getDirectionsFrom(messages));
    }
    return msg.toString();
  }

  /**
   * Gets the unique identifier of this.
   *
   * @return a string containing the name of this.
   */
  public final String getName() {
    return f_name;
  }

  /**
   * Gets the short description, e.g., "the Grande Hall", for this.
   *
   * @return the short description for this.
   */
  public final String getShortDescription() {
    return (f_article.trim().length() == 0 ? "" : (f_article + " ")) + f_name;
  }

  /**
   * Access the background sound for this place
   *
   * @return the string path of the sound effect
   */
  public String getSound() {
    return f_sound;
  }

  /**
   * Gets the destination of travel in the specified direction.
   *
   * @param direction
   *          the non-null direction of travel.
   * @return the destination of travel in the specified direction, or <code>null</code> if travel is
   *         not allowed in that direction.
   * @throws NullPointerException
   *           if direction is null
   * @see #isTravelAllowedToward(Navigation)
   */
  public Place getTravelDestinationToward(Navigation direction) throws NullPointerException {
    if (direction == null) {
      throw new NullPointerException("direction cannot be null");
    }
    return f_directionOfTravelToPlace.get(direction);
  }

  /**
   * Check this to see if arriving at this place should end the game with a victory.
   *
   * @return <code>true</code> if this Place is a winning location <br>
   *         <code>false</code> otherwise.
   * @since 1.1
   */
  public Boolean getWinCondition() {
    return f_winCondition;
  }

  /**
   * Gets the world this exists in.
   *
   * @return a reference to the {@link Universe} instance containing this.
   */
  public final Universe getWorld() {
    return f_world;
  }

  /**
   * Returns <code>true</code> if travel is allowed in the specified direction from this place,
   * <code>false</code> otherwise.
   *
   * @param direction
   *          the non-null direction of travel.
   * @return <code>true</code> if travel is allowed in the specified direction from this place,
   *         <code>false</code> otherwise.
   * @throws NullPointerException
   *           if direction is null
   */
  public boolean isTravelAllowedToward(Navigation direction) throws NullPointerException {
    if (direction == null) {
      throw new NullPointerException("direction cannot be null");
    }
    return f_directionOfTravelToPlace.containsKey(direction);
  }

  /**
   * Use this to see if a given container has the items required to enter this location.
   *
   * @param container
   *          The Container object that is trying to enter this place, expected but not required to
   *          be a player's inventory
   * @return A Set of Item containing every missing item required to enter this place, will be empty
   *         if entry allowed
   */
  public Set<Item> missingItems(Container container) {
    final Set<Item> missingItems = new TreeSet<>();
    for (final Item item : f_itemsRequiredForEntry) {
      if (!container.isPresent(item)) {
        missingItems.add(item);
      }
    }
    return missingItems;
  }

  /**
   * Remove a Character from this location
   *
   * @param character
   *          the {@link Character} to remove from here
   */
  public void removeCharacter(Character character) {
    f_charactersHere.remove(character.getName().toUpperCase());
  }

  /**
   * Used by the EffectPlace class to alter the description of this.
   *
   * @param newDescription
   *          A string the will replace the current description field.
   * @throws NullPointerException
   *           if newDescription is null
   */
  public final void setDescription(String newDescription) throws NullPointerException {
    f_description = Objects.requireNonNull(newDescription, "newDescription cannot be null");
  }

  /**
   * Sets the destination of travel in the specified direction for this place.
   *
   * @param direction
   *          the non-null direction of travel.
   * @param place
   *          the non-null destination of travel in the specified direction.
   * @throws NullPointerException
   *           if direction or place is null
   */
  public void setTravelDestination(Navigation direction, Place place) throws NullPointerException {
    if (direction == null) {
      throw new NullPointerException("direction cannot be null");
    }
    if (place == null) {
      throw new NullPointerException("place cannot be null");
    }
    f_directionOfTravelToPlace.put(direction, place);
  }

  @Override
  public String toString() {
    return getShortDescription();
  }
}
