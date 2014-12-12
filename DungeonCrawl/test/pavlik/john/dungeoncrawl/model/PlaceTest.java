package pavlik.john.dungeoncrawl.model;

import java.util.HashMap;

import pavlik.john.dungeoncrawl.model.Container;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Universe;
import junit.framework.TestCase;

/**
 * @author Robert Graham
 * @author Brian Woolley
 * @author John Pavlik
 * @see Place
 */
public class PlaceTest extends TestCase {

  private Universe f_world;

  private Place f_Dayton, f_Columbus, f_nowhere;

  private Item  item;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    f_world = new Universe();
    f_nowhere = f_world.getNowherePlace();
    f_Dayton = new Place(f_world, "city of Dayton", "the", "You are in the charming Midwest town of Dayton, Ohio.",
        false, null);
    f_Columbus = new Place(f_world, "city of Columbus", "the", "You are in Columbus, the capital of Ohio", true, null);
    f_Dayton.setTravelDestination(Navigation.EAST, f_Columbus);
    final HashMap<Place, String> blocked = new HashMap<>();
    blocked.put(f_Dayton, "Can't enter");
    item = new Item("Name1", "article", "description", 5, 5, new HashMap<>(), false, "Can't", blocked, null);
    f_Dayton.addItemRequired(item);
    f_Columbus.setTravelDestination(Navigation.WEST, f_Dayton);
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.addItemRequired(Item)'
   */
  public void testAddItemRequired() {
    // Done in setup, if testMissingItems completes then this was successful
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.compareTo(Place)'
   */
  public void testCompareTo() {
    assertEquals(0, f_Dayton.compareTo(f_Dayton));
    assertEquals(1, f_Dayton.compareTo(f_Columbus));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getArticle()'
   */
  public void testGetArticle() {
    assertEquals("a", f_nowhere.getArticle());
    assertEquals("the", f_Dayton.getArticle());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getContainer()'
   */
  public void testGetContainer() {
    assertNotNull(f_Columbus.getContainer());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getDescription()'
   */
  public void testGetDescription() {
    assertEquals("You are in a very remote place.", f_nowhere.getDescription());
    assertEquals("You are in the charming Midwest town of Dayton, Ohio.", f_Dayton.getDescription());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getName()'
   */
  public void testGetName() {
    assertEquals("Very Remote Place", f_nowhere.getName());
    assertEquals("city of Dayton", f_Dayton.getName());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getShortDescription()'
   */
  public void testGetShortDescription() {
    assertEquals("a Very Remote Place", f_nowhere.getShortDescription());
    assertEquals("the city of Dayton", f_Dayton.getShortDescription());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getTravelDestinationToward(Navigation)'
   */
  public void testGetTravelDestinationToward() {
    for (final Navigation d : Navigation.values()) {
      assertNull(f_nowhere.getTravelDestinationToward(d));
    }
    for (final Navigation d : Navigation.values()) {
      if (d == Navigation.EAST) {
        assertEquals(f_Dayton.getTravelDestinationToward(d), f_Columbus);
      } else {
        assertNull(f_Dayton.getTravelDestinationToward(d));
      }
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getWinCondition()'
   */
  public void testGetWinCondition() {
    assertEquals(false, f_nowhere.getWinCondition().booleanValue());
    assertEquals(true, f_Columbus.getWinCondition().booleanValue());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.getWorld()'
   */
  public void testGetWorld() {
    assertEquals(f_world, f_nowhere.getWorld());
    assertEquals(f_world, f_Dayton.getWorld());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.isTravelAllowedToward(Navigation)'
   */
  public void testIsTravelAllowedToward() {
    for (final Navigation d : Navigation.values()) {
      assertFalse(f_nowhere.isTravelAllowedToward(d));
    }
    for (final Navigation d : Navigation.values()) {
      if (d == Navigation.EAST) {
        assertTrue(f_Dayton.isTravelAllowedToward(d));
      } else {
        assertFalse(f_Dayton.isTravelAllowedToward(d));
      }
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.missingItems(Container)'
   */
  public void testMissingItems() {
    assertTrue(f_Dayton.missingItems(new Container()).contains(item));
    final Container container = new Container();
    container.addItem(item);
    assertTrue(f_Dayton.missingItems(container).isEmpty());
    try {
      f_Dayton.missingItems(null);
      fail();
    } catch (final NullPointerException e) {

    }
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Place constructor
   */
  public void testPlace() {
    try {
      new Place(null, "somewhere", "", "you are somewhere", false, null);
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      new Place(f_world, null, "", "you are somewhere", false, null);
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      new Place(f_world, "somewhere", null, "you are somewhere", false, null);
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      new Place(f_world, "somewhere", "", null, false, null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Place.setTravelDestination(Navigation,
   * Place)'
   */
  public void testSetTravelDestination() {
    f_Columbus.setTravelDestination(Navigation.EAST, f_nowhere);
    assertEquals(f_Columbus.getTravelDestinationToward(Navigation.EAST), f_nowhere);
    try {
      f_Columbus.setTravelDestination(null, f_Dayton);
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      f_Columbus.setTravelDestination(Navigation.NORTH, null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

}
