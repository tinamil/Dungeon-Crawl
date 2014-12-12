package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A representation of an Item in the game. Must be stored inside of a {@link Container}
 * class. Item's with identical names are considered identical items (name.equal(name) returns
 * true).
 *
 * @author John
 * @version 1.2
 * @since 1.2
 */
public class Item implements Comparable<Item>, HasContainer, Serializable {

  /**
   * Used to distinguish actions performed with items
   *
   * @author John
   *
   */
  public static enum Action {
    /**
     * The item was dropped
     */
    DROP,
    /**
     * The item was picked up (taken)
     */
    TAKE,
    /**
     * The item was passed directly to another player
     */
    GIVE;
  }

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * The item's name, should be a unique identifier among all items.
   */
  private final String             f_name;
  private final String             f_description;
  /**
   * The article for describing this item.
   */
  private final String             f_article;
  /**
   * The number of points this item is worth when picked up. Can be positive or negative.
   */
  private Long                     f_takePoints;
  /**
   * The number of points this item is worth when dropped. Can be positive or negative.
   */
  private Long                     f_dropPoints;
  /**
   * A mapping of every place this item gives points for being dropped at.
   */
  private final Map<Place, Long>   f_dropPointsMap;
  /**
   * Can this item be picked up by a player
   */
  private final Boolean            f_takeable;
  /**
   * If it can't be picked up, then display this when the user tries to pick it up
   */
  private final String             f_cantTakeMessage;
  /**
   * A list of places that require this item to enter, and the string to display at that time.
   */
  private final Map<Place, String> f_blockMessagesMap;

  /**
   * If this item contains multiple items, e.g. this is a chest, store those items in here.
   */
  private final Container          f_storedItems;

  /**
   * Public constructor for an Item
   *
   * @param name
   *          The name of the Item, must be unique among all instances of Item
   * @param article
   *          The article for referring to this place if in English the only valid articles are a,
   *          an and the.
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
   */
  Item(String name, String article, String description, long takePoints, long dropPointsNowhere,
      Map<Place, Long> dropPointsMap, Boolean takeable, String cantTake, Map<Place, String> blockMessages,
      Container container) {

    f_name = Objects.requireNonNull(name, "Item name cannot be null");
    f_article = Objects.requireNonNull(article, "Item " + name + "'s article cannot be null");
    f_takePoints = Objects.requireNonNull(takePoints, "Item " + name + "'s takePoints cannot be null");
    f_dropPoints = Objects.requireNonNull(dropPointsNowhere, "Item " + name + "'s dropPointsNowhere cannot be null");
    f_dropPointsMap = Objects.requireNonNull(dropPointsMap, "Item " + name + "'s dropPointsMap cannot be null");
    f_takeable = Objects.requireNonNull(takeable, "Item " + name + "'s takeable cannot be null");
    f_cantTakeMessage = Objects.requireNonNull(cantTake, "Item " + name + "'s cantTake cannot be null");
    f_blockMessagesMap = Objects.requireNonNull(blockMessages, "Item " + name + "'s blockMessages cannot be null");
    f_storedItems = container;
    f_description = Objects.requireNonNull(description, "Item " + name + "'s description cannot be null");
  }

  /**
   * @return a String describing why the item cannot be taken
   */
  public String cantTakeMessage() {
    return f_cantTakeMessage;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Item o) {
    return f_name.toUpperCase().compareTo(o.f_name.toUpperCase());
  }

  /**
   * Getter for f_article field.
   *
   * @return the article to refer to this item with for proper syntax
   */
  public String getArticle() {
    return f_article;
  }

  /**
   *
   * @return Any items contained inside of this item
   */
  @Override
  public Container getContainer() {
    return f_storedItems;
  }

  /**
   * Get the items description
   *
   * @return The String description
   */
  public String getDescription() {
    return f_description;
  }

  /**
   * The number of points to award to the player for dropping this item. Zeroizes itself after being
   * called as points can only be awarded once.
   *
   * @return the number of points to award to the player for dropping this item
   */
  public long getDropAndZeroizePoints() {
    final long points = f_dropPoints;
    f_dropPoints = 0L;
    return points;
  }

  /**
   * The number of points to award to the player for dropping this item in the specified place.
   * Zeroizes itself after being called as points can only be awarded once per location.
   *
   * @param place
   *          The location the player is currently located
   *
   * @return the number of points to award to the player for dropping this item
   */
  public long getDropAndZeroizePoints(Place place) {
    if (place == null) {
      throw new NullPointerException("plcae cannot be null");
    }
    final Long points = f_dropPointsMap.remove(place);
    if (points == null) {
      return 0L;
    }
    return points.longValue();
  }

  /**
   * Getter for f_takePoints field. Use this for saving the state, not for awarding points to the
   * player.
   *
   * @return the number of points to award the player.
   * @see Item#getDropAndZeroizePoints()
   */
  public Long getDropPoints() {
    return f_dropPoints;
  }

  /**
   * Use for determining all of the values of the drop points of this item
   *
   * @return a <code>Map&lt;Place,Long&gt;</code> copy of the dropPointsMap
   */
  public Map<Place, Long> getDropPointsPlaces() {
    return new TreeMap<>(f_dropPointsMap);
  }

  /**
   * Get a description including the name of the item for display to the user.
   *
   * @return A string full description
   */
  public String getFullDescription() {
    return toString() + ": " + getDescription();
  }

  /**
   * Use {@link #toString()} if you want a unique name of the item
   *
   * @return a unique identifier for the item
   */
  public String getName() {
    return f_name;
  }

  /**
   * An access method for places to get a message to pass back to the user if the player lacks an
   * item required for entry.
   *
   * @param place
   *          The place that the player is trying enter but cannot because they lack this item
   * @return A string that describes what item they need to enter
   */
  public String getPlaceBlockedMessage(Place place) {
    if (place == null) {
      throw new NullPointerException("place cannot be null");
    }
    return f_blockMessagesMap.get(place);
  }

  /**
   * Use for determining all of the places requiring this item to enter
   *
   * @return a Map&lt;Place,String&gt; copy of the f_blockMessagesMap
   */
  public Map<Place, String> getPlaceBlockedMessages() {
    return new TreeMap<>(f_blockMessagesMap);
  }

  /**
   * The number of points to award to the player for taking this item Zeroizes itself after being
   * called as points can only be awarded once per location.
   *
   * @return the number of points to award to the player
   */
  public long getTakeAndZeroizePoints() {
    final long points = f_takePoints;
    f_takePoints = 0L;
    return points;
  }

  /**
   * Getter for f_takePoints field. Use this for saving the state, not for awarding points to the
   * player.
   *
   * @return the number of points to award the player.
   * @see Item#getTakeAndZeroizePoints()
   */
  public Long getTakePoints() {
    return f_takePoints;
  }

  /**
   * @return true if the Item can be picked up
   */
  public boolean isTakeable() {
    return f_takeable;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (f_article.trim().length() > 0 ? (f_article + " ") : "") + f_name;
  }

}
