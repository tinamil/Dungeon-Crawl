package pavlik.john.dungeoncrawl.model.events.actions;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.events.actions.AdjustMoneyAction;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see AdjustMoneyAction
 */
public class AdjustMoneyActionTest extends TestCase {

  AdjustMoneyAction actionGive, actionTake;
  Player            player;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final Controller wc = new Controller(TestConstants.v3TESTFILE);
    player = wc.getWorld().getPlayer("player");
    actionGive = new AdjustMoneyAction("50", true, "GiveMoney");
    actionTake = new AdjustMoneyAction("50", false, "GiveMoney");
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.events.actions.AdjustMoneyAction constructor
   */
  public void testConstructor() {
    // Setup is sufficient
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.actions.AdjustMoneyAction#performAction()
   */
  public void testPerformAction() {
    assertEquals(0, player.getMoney());
    assertTrue(actionGive.performAction(player));
    assertEquals(50, player.getMoney());
    assertTrue(actionTake.performAction(player));
    assertEquals(0, player.getMoney());
  }
}
