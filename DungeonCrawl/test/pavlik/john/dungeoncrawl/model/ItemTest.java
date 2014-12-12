package pavlik.john.dungeoncrawl.model;

import java.util.HashMap;
import java.util.Map;

import pavlik.john.dungeoncrawl.model.Container;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Universe;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Place
 */
public class ItemTest extends TestCase {

  Item item1, item2, item3;
  Place place, otherPlace;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final HashMap<Place, String> blocked = new HashMap<>();
    place = new Place(new Universe(), "Place", "A", "Here", false, null);
    otherPlace = new Place(new Universe(), "Other Place", "An", "Here", false, null);
    blocked.put(place, "Can't enter");
    final HashMap<Place, Long> points = new HashMap<>();
    points.put(otherPlace, 50L);
    item1 = new Item("Name1", "article", "description", 5, 5, new HashMap<>(), false, "Can't", blocked, null);
    item2 = new Item("Name2", "article", "description", 0, 0, points, true, "", new HashMap<>(), new Container());
    item3 = new Item("Name3", "article", "description", 0, 0, new HashMap<>(), false, "Can't", new HashMap<>(), null);
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.cantTakeMessage()
   */
  public void testcantTakeMessage() {
    assertEquals("Can't", item1.cantTakeMessage());
    assertEquals("", item2.cantTakeMessage());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.CompareTo()
   */
  public void testCompareTo() {
    assertTrue(item1.compareTo(item1) == 0);
    assertTrue(item1.compareTo(item2) < 0);
    assertTrue(item3.compareTo(item2) > 0);
    try {
      item1.compareTo(null);
      fail();
    } catch (final NullPointerException e) {

    }
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Container constructor
   */
  public void testConstructor() {
    // Completing setUp is sufficient
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getArticle()
   */
  public void testGetArticle() {
    assertEquals("article", item1.getArticle());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getContainer()
   */
  public void testGetContainer() {
    assertNotNull(item2.getContainer());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getDropAndZeroizePoints()
   */
  public void testGetDropAndZeroizePoints() {
    assertEquals(5L, item1.getDropAndZeroizePoints());
    assertEquals(0L, item1.getDropAndZeroizePoints());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getDropAndZeroizePoints(Place)
   */
  public void testGetDropAndZeroizePointsAtPlace() {
    assertEquals(50L, item2.getDropAndZeroizePoints(otherPlace));
    assertEquals(0L, item2.getDropAndZeroizePoints(otherPlace));
    assertEquals(0L, item1.getDropAndZeroizePoints(place));
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getDropPoints()
   */
  public void testGetDropPoints() {
    assertEquals(5L, item1.getDropPoints().longValue());
    assertEquals(5L, item1.getDropPoints().longValue());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getDropPointsPlaces()
   */
  public void testGetDropPointsPlaces() {
    Map<Place, Long> map = item1.getDropPointsPlaces();
    assertNotNull(map);
    map = item2.getDropPointsPlaces();
    assertTrue(map.containsKey(otherPlace));
    assertEquals(50L, map.get(otherPlace).longValue());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getName()
   */
  public void testGetName() {
    assertEquals("Name1", item1.getName());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getPlaceBlockedMessage(Place)
   */
  public void testGetPlaceBlockedMessage() {
    assertEquals("Can't enter", item1.getPlaceBlockedMessage(place));
    assertNull(item1.getPlaceBlockedMessage(otherPlace));
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getPlaceBlockedMessages()
   */
  public void testGetPlaceBlockedMessages() {
    Map<Place, String> map = item2.getPlaceBlockedMessages();
    assertNotNull(map);
    map = item1.getPlaceBlockedMessages();
    assertTrue(map.containsKey(place));
    assertEquals("Can't enter", map.get(place));
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getTakeAndZeroizePoints()
   */
  public void testGetTakeAndZeroizePoints() {
    assertEquals(5L, item1.getTakeAndZeroizePoints());
    assertEquals(0L, item1.getTakeAndZeroizePoints());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.getTakePoints()
   */
  public void testGetTakePoints() {
    assertEquals(5L, item1.getTakePoints().longValue());
    assertEquals(5L, item1.getTakePoints().longValue());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.isTakeable()
   */
  public void testIsTakeable() {
    assertFalse(item1.isTakeable());
    assertTrue(item2.isTakeable());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Item.toString()
   */
  public void testToString() {
    assertEquals("article Name1", item1.toString());
  }

}
