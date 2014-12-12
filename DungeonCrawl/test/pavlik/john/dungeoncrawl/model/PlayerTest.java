package pavlik.john.dungeoncrawl.model;

import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Universe;
import junit.framework.TestCase;

/**
 * @author Robert Graham
 * @author Brian Woolley
 * @see Player
 */
public class PlayerTest extends TestCase {

  private Universe  f_world;

  private Player f_player;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    f_world = new Universe();
    final Place place = f_world.createPlace("city of Dayton", "the",
        "You are in the glamorous mid-west city of Dayton", false, null);
    f_world.createPlayer("player", place, "", "", null);
    f_player = f_world.getPlayer("player");
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Player.getMoney()' and
   * 'pavlik.john.dungeoncrawl.model.Player.addMoney()'
   */
  public void testGetAddMoney() {
    assertEquals(0, f_player.getMoney());
    f_player.changeMoney(5);
    assertEquals(5, f_player.getMoney());
    f_player.changeMoney(-5);
    assertEquals(0, f_player.getMoney());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Player.getScore()' and
   * 'pavlik.john.dungeoncrawl.model.Player.addScore()'
   */
  public void testGetAddScore() {
    assertEquals(0L, f_player.getScore().longValue());
    f_player.addPoints(5L);
    assertEquals(5L, f_player.getScore().longValue());
    f_player.addPoints(-5L);
    assertEquals(0L, f_player.getScore().longValue());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Player.isOccupied()'
   */
  public void testIsOccupied() {
    assertFalse(f_player.isOccupied());
    f_player.setOccupied(true);
    assertTrue(f_player.isOccupied());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Player constructor
   */
  public void testPlayer() {
    try {
      new Player(null, null, "Player", null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }
  }
}
