package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A Container that holds {@link Item} instances. Essentially a wrapper around a Map&lt;String,
 * Item&gt; that manages the item storage. Will store and return items in alphabetical order by
 * name. No duplicate named items are allowed.
 *
 * @author John
 * @version 1.2
 * @since 1.2
 */
public class Container implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private Set<Item> f_items = null;

  /**
   * Public constructor that initializes the Map f_items to an empty TreeMap.
   */
  public Container() {
    f_items = new TreeSet<>();
  }

  /**
   * Add the item to this container
   *
   * @param item
   *          Any non-null item
   */
  public void addItem(Item item) {
    if (item == null) {
      throw new NullPointerException("item cannot be null");
    }
    f_items.add(item);
  }

  /**
   * Use this to access all of the items inside this container.
   *
   * @return a Collection containing all of the items inside this container
   */
  public Collection<Item> getItems() {
    return f_items;
  }

  /**
   * Check to see if the container has any items.
   *
   * @return true if this container has no items
   */
  public boolean isEmpty() {
    return f_items.isEmpty();
  }

  /**
   * Get the specified item from this container
   *
   * @param optional
   *          The item to get
   * @return The item if present, or null if not
   */
  public boolean isPresent(Item optional) {
    if (optional == null) {
      return false;
    }
    return f_items.contains(optional);
  }

  /**
   * Move an item from this container to destination container
   *
   * @param destination
   *          a HasContainer to move the item to
   * @param item
   *          the item to be moved
   * @return true if the move was successful, false if the item could not be found in the source
   * @since 1.2
   */
  public boolean moveItem(Container destination, Item item) {
    if (isPresent(item)) {
      destination.addItem(removeItem(item));
      return true;
    } else {
      return false;
    }
  }

  /**
   * Remove the item from this container
   *
   * @param item
   *          Any non-null item located in this container
   * @return The item removed if an element was removed as a result of this call, null otherwise
   */
  public Item removeItem(Item item) {
    if (item == null) {
      throw new NullPointerException("item cannot be null");
    }
    return (f_items.remove(item) ? item : null);
  }
}
