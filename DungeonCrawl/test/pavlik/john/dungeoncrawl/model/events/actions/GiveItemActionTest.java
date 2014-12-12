package pavlik.john.dungeoncrawl.model.events.actions;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.events.actions.GiveItemAction;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see GiveItemAction
 */
public class GiveItemActionTest extends TestCase {

  GiveItemAction  actionGive;
  Player          player;
  Controller wc;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    wc = new Controller(TestConstants.v3TESTFILE);
    player = wc.getWorld().getPlayer("player");
    wc.travel(player, Navigation.WEST);
    actionGive = new GiveItemAction("Old Man", "penny", "GiveItem");
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
    assertTrue(player.getContainer().isPresent(wc.getWorld().getItem("penny")));
    assertTrue(actionGive.performAction(player));
    assertFalse(player.getContainer().isPresent(wc.getWorld().getItem("penny")));
  }
}
