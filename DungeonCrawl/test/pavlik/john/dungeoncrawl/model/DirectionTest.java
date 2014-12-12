package pavlik.john.dungeoncrawl.model;

import pavlik.john.dungeoncrawl.model.Navigation;
import junit.framework.TestCase;

/**
 * @author T.J. Halloran
 * @author Brian Woolley
 * @see Navigation
 */
public class DirectionTest extends TestCase {

  /**
   * Test for pavlik.john.dungeoncrawl.model.Navigation.getAbbreviation()
   */
  public void testGetAbbreviation() {
    assertTrue(Navigation.NORTH.getAbbreviation().equals("N"));
    assertTrue(Navigation.SOUTH.getAbbreviation().equals("S"));
    assertTrue(Navigation.EAST.getAbbreviation().equals("E"));
    assertTrue(Navigation.WEST.getAbbreviation().equals("W"));
  }

  /**
   * Test for pavlik.john.dungeoncrawl.model.Navigation enum expected values existence
   */
  public void testGetInstance() {
    try {
      Navigation.getInstance(null);
      fail();
    } catch (final NullPointerException e) {

    }
    assertEquals(false, Navigation.getInstance("NorT").isPresent());
    assertEquals(false, Navigation.getInstance("FooBar").isPresent());

    assertEquals(Navigation.NORTH, Navigation.getInstance("N").get());
    assertEquals(Navigation.NORTH, Navigation.getInstance("n").get());
    assertEquals(Navigation.NORTH, Navigation.getInstance("NORTH").get());
    assertEquals(Navigation.NORTH, Navigation.getInstance("north").get());

    assertEquals(Navigation.SOUTH, Navigation.getInstance("S").get());
    assertEquals(Navigation.SOUTH, Navigation.getInstance("s").get());
    assertEquals(Navigation.SOUTH, Navigation.getInstance("SOUTH").get());
    assertEquals(Navigation.SOUTH, Navigation.getInstance("south").get());

    assertEquals(Navigation.EAST, Navigation.getInstance("E").get());
    assertEquals(Navigation.EAST, Navigation.getInstance("e").get());
    assertEquals(Navigation.EAST, Navigation.getInstance("EAST").get());
    assertEquals(Navigation.EAST, Navigation.getInstance("east").get());

    assertEquals(Navigation.WEST, Navigation.getInstance("W").get());
    assertEquals(Navigation.WEST, Navigation.getInstance("w").get());
    assertEquals(Navigation.WEST, Navigation.getInstance("WEST").get());
    assertEquals(Navigation.WEST, Navigation.getInstance("west").get());
  }
}
