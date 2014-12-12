package pavlik.john.dungeoncrawl.model;

import java.util.HashMap;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Universe;
import junit.framework.TestCase;

/**
 * @author T.J. Halloran
 * @author Robert Graham
 * @author Brian Woolley
 * @see Universe
 */
public class WorldTest extends TestCase {

  private Universe   f_world;

  @Override
  protected void setUp() {
    f_world = new Universe();
    f_world.createPlayer("player", f_world.getNowherePlace(), "", "", null);
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Universe.isGameWon()
   */
  public void testGameWon() {
    assertFalse(f_world.isGameWon());
    f_world.setGameOver(false);
    assertFalse(f_world.isGameWon());
    f_world.setGameOver(true);
    assertTrue(f_world.isGameWon());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Universe.createItem() and getItem()
   */
  public void testItemCreation() {

    final Item item1 = f_world.createItem("Item1", "a", "description", -5L, 5L, new HashMap<>(), false, "",
        new HashMap<>(), true);
    assertEquals(item1, f_world.getItem("Item1"));
    assertEquals(item1, f_world.getItem("iTEM1"));
    assertEquals(item1, f_world.getItem("item1"));
    assertNull(f_world.getItem("exists"));

    f_world.createPlace("place", "a", "Here", false, null);

    assertTrue(f_world.isNameUsed("item1"));
    assertTrue(f_world.isNameUsed("place"));
    assertFalse(f_world.isNameUsed("UNKNOWN"));

    final Item item2 = f_world.createItem("Item2", "a", "description", -5L, 5L, new HashMap<>(), false, "",
        new HashMap<>(), false);

    final Set<Item> items = f_world.getItems();
    assertEquals(2, items.size());
    assertTrue(items.contains(item1));
    assertTrue(items.contains(item2));
    items.remove(item1);
    assertFalse(items.equals(f_world.getPlaces()));

    try {
      f_world.createItem("place", "a", "description", 5L, -5L, new HashMap<>(), false, "", new HashMap<>(), false);
      fail();
    } catch (final IllegalStateException e) {
      // ignore, the creation of a duplicate location should fail
    }
    try {
      f_world.createItem("item1", "a", "description", 5L, -5L, new HashMap<>(), false, "", new HashMap<>(), false);
      fail();
    } catch (final IllegalStateException e) {
      // ignore, the creation of a duplicate location should fail
    }
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Universe.getObservers(), addObserver(), and
   * remove Observer()
   */
  public void testObservers() {

  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Universe.createPlace()
   */
  public void testPlaceCreation() {
    String name, article, description;
    Boolean winCondition;
    name = "Hall";
    article = "the";
    description = "You are standing in a large hall";
    winCondition = false;
    final Place l1 = f_world.createPlace(name, article, description, winCondition, null);
    assertEquals(f_world, l1.getWorld());
    assertEquals(l1, f_world.getPlace("HALL"));
    assertEquals(l1, f_world.getPlace("hAll"));
    assertEquals(l1, f_world.getPlace("hall"));
    assertNull(f_world.getPlace("exists"));

    name = "Room";
    article = "the";
    description = "You are standing in a room";
    winCondition = false;
    final Place l2 = f_world.createPlace(name, article, description, winCondition, null);
    assertEquals(f_world, l2.getWorld());
    assertEquals(l2, f_world.getPlace("ROOM"));
    assertEquals(l2, f_world.getPlace("room"));
    assertEquals(l2, f_world.getPlace("room"));

    assertTrue(f_world.isNameUsed("ROOM"));
    assertTrue(f_world.isNameUsed("HALL"));
    assertFalse(f_world.isNameUsed("UNKNOWN"));

    final Set<Place> places = f_world.getPlaces();
    assertEquals(3, places.size());
    assertTrue(places.contains(l1));
    assertTrue(places.contains(l2));
    places.remove(l1);
    assertFalse(places.equals(f_world.getPlaces()));

    try {
      f_world.createPlace("room", article, description, winCondition, null);
      fail();
    } catch (final IllegalStateException e) {
      // ignore, the creation of a duplicate location should fail
    }
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Universe constructor
   */
  public void testWorld() {
    assertEquals(1, f_world.getPlaces().size());
    assertNotNull(f_world.getNowherePlace());
    assertNotNull(f_world.getPlayer("player"));
    try {
      f_world.getPlace(null);
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      f_world.getPlace(null);
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      f_world.isNameUsed(null);
    } catch (final NullPointerException e) {
    }
  }
}
